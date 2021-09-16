/**
 * 
 */
package com.appdynamics.cloud.prometheus.analytics;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

import com.appdynamics.cloud.prometheus.ApplicationConstants;
import com.appdynamics.cloud.prometheus.Logger;
import com.appdynamics.cloud.prometheus.awssigv4.Sigv4Client;
import com.appdynamics.cloud.prometheus.config.AnalyticsEventsSourceConfig;
import com.appdynamics.cloud.prometheus.config.ServiceConfig;
import com.appdynamics.cloud.prometheus.utils.StringUtils;

/**
 * @author James Schneider
 *
 */
public class PrometheusEventsSource implements AnalyticsEventsSource, ApplicationConstants {

	private ServiceConfig serviceConfig;
	private AnalyticsEventsSourceConfig eventsSourceConfig;
	private int executionInterval = 1;
	private static Logger logr;
	
	/**
	 * 
	 */
	public PrometheusEventsSource() {
		
	}

	@Override
	public void initialize(ServiceConfig serviceConfig, AnalyticsEventsSourceConfig analyticsEventsSourceConfig) throws Throwable {
		
		logr = new Logger(PrometheusEventsSource.class.getSimpleName(), serviceConfig.isDebugLogging());
		this.serviceConfig = serviceConfig;
		this.eventsSourceConfig = analyticsEventsSourceConfig;
		this.executionInterval = Integer.parseInt(this.eventsSourceConfig.getExecutionInterval());
		
		logr.info("##############################  Initializing Prometheus Events Source for schema '" + this.eventsSourceConfig.getSchemaName() + "'");
	}

	@Override
	public String getSchemaName() throws Throwable {
		
		return this.eventsSourceConfig.getSchemaName();
	}

	@Override
	public String getSchemaDefinitionFilePath() throws Throwable {
		return this.eventsSourceConfig.getSchemaDefinitionFilePath();
	}

	@Override
	public int getExecutionInterval() throws Throwable {
		return this.executionInterval;
	}

	
	@Override
	public String getEvents2PublishJson() throws Throwable {
		
		
		String[] promQueries = this.getPromQueriesFromFile();
		
		for (int qryCntr = 0; qryCntr < promQueries.length; qryCntr++) {
			
			this.buildJSONForQuery(this.executePromQuery(promQueries[qryCntr]));
			
		}
		
		
		return null;
	}
	
	
	private String buildJSONForQuery(String json) throws Throwable {
		
		JSONObject respObj = new JSONObject(json);
		
		String str = respObj.getString("status");
		
		logr.info("status = " + str);
		
		JSONObject dataObj = respObj.getJSONObject("data");
		
		JSONArray resArray = dataObj.getJSONArray("result");
		
		if (resArray != null && resArray.length() > 0) {
			logr.info("Result array has data | array length = " + resArray.length());
		} else {
			logr.info("Result array has no data");
			return null;
		}
		
		JSONObject metricWrapper = null;
		JSONObject metricMeta = null;
		JSONObject metricValue = null;
		
		for (int i=0; i < resArray.length(); i++) {
			metricWrapper = resArray.getJSONObject(i);
			metricMeta = metricWrapper.getJSONObject("metric");
			
			
			logr.info(metricMeta.getString("__name__"));
			
		}
		

		
		return "";
	}
	
	private String executePromQuery(String promQl) throws Throwable {
		
		String queryResults = null;
		
		String authMode = serviceConfig.getAuthenticationMode();
		if (authMode == null) {
			authMode = "";
		}
		
		switch (authMode) {
		
		case AUTH_MODE_AWSSIGV4:
			queryResults = this.executePromQueryWithAwsSigv4(promQl);
			break;

		default:
			queryResults = this.executePromQueryWithNoAuth(promQl);
			break;
		}
		
		logr.info(queryResults);
		
		return queryResults;
	}

	private String executePromQueryWithNoAuth(String promQl) throws Throwable {
		
		String restEndpoint = this.constructEndpointQuery(promQl);	
		
		CloseableHttpClient client = HttpClients.createDefault();
		
		HttpGet request = new HttpGet(restEndpoint);

		request.addHeader("Accept", "application/json, text/plain, */*");
		//request.addHeader("Accept", "application/vnd.appd.events+json;v=2");

	    CloseableHttpResponse response = client.execute(request);
		
	    logr.debug(" - Executing Query with No Auth");
	    logr.debug(" - Query: " + promQl + " : HTTP Status: " + response.getStatusLine().getStatusCode());
	    
		String resp = "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuilder out = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            out.append(line);
        }			
		
        resp = out.toString();
		reader.close();
		
		logr.debug(" - Query Response");
		logr.debug(resp);

		HttpClientUtils.closeQuietly(response);
		HttpClientUtils.closeQuietly(client);
				
		
		return resp;
	}
	
	private String executePromQueryWithAwsSigv4(String promQl) throws Throwable {
		
		String restEndpoint = this.constructEndpointQuery(promQl);
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("query", promQl);
	    
		logr.carriageReturnDebug();
		logr.carriageReturnDebug();
		logr.debug("****************************************************************************************************************************************************************************************");
		logr.debug(" - Executing Query with AWS Sigv4 Auth");
	    logr.debug(" - Query: " + promQl);
	    logr.debug("****************************************************************************************************************************************************************************************");
	    
		return Sigv4Client.processRequest(restEndpoint, this.serviceConfig.getAwsRegion(), this.serviceConfig.getAwsAccessKey(), this.serviceConfig.getAwsSecretKey(), queryParameters);
		
	}
	
	private String constructEndpointQuery(String promQl) {
		return this.serviceConfig.getPrometheusUrl() + "?query=" + promQl;
	}
	
	
	private String[] getPromQueriesFromFile() throws Throwable {
		String[] lines = StringUtils.getFileAsArrayOfLines(this.eventsSourceConfig.getQueriesTextFilePath());
		
		int validQueries = 0;
		
		for (int i = 0; i < lines.length; i++) {
			if (lines[i] != null && !lines[i].equals("")) {
				validQueries++;
				lines[i] = lines[i].trim();
			}
		}

		List<String> queries = new ArrayList<String>(validQueries);
		
		for (int i = 0; i < lines.length; i++) {
			if (lines[i] != null && !lines[i].equals("")) {
				queries.add(lines[i]);
			}
		}
		
		return queries.toArray(new String[validQueries]);
		
	}
	
	
}
