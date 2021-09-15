/**
 * 
 */
package com.appdynamics.cloud.prometheus.analytics;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.appdynamics.cloud.prometheus.Logger;
import com.appdynamics.cloud.prometheus.config.ServiceConfig;

/**
 * @author James Schneider
 *
 */
public class AnalyticsEventsDriver implements Runnable {

	private ServiceConfig serviceConfig;
	private AnalyticsEventsSource analyticsEventsSource;
	private static Logger logr = new Logger(AnalyticsEventsDriver.class.getSimpleName());
	
	/**
	 * 
	 */
	public AnalyticsEventsDriver(ServiceConfig serviceConfig, AnalyticsEventsSource analyticsEventsSource) {
		this.serviceConfig = serviceConfig;
		this.analyticsEventsSource = analyticsEventsSource;
		
	}

	@Override
	public void run() {
		
		while (true) {

			try {
				
				logr.info("##############################  Processing Batch Events for '" + this.analyticsEventsSource.getSchemaName() + "' schema ##############################");
				String payload = this.analyticsEventsSource.getEvents2PublishJson();
				
				String publishUrl = this.serviceConfig.getEventsServiceEndpoint() + "/events/publish/" + this.analyticsEventsSource.getSchemaName();
				
				this.publishEvents(this.serviceConfig.getControllerGlobalAccount(), 
						this.serviceConfig.getEventsServiceApikey(), publishUrl, payload, this.analyticsEventsSource.getSchemaName());
				
				Thread.currentThread().sleep(this.analyticsEventsSource.getExecutionInterval() * 60000);
				
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
			
		}
		
	}

	private void publishEvents(String accountName, String apiKey, String restEndpoint, String payload, String schemaName) throws Throwable {
		
		CloseableHttpClient client = HttpClients.createDefault();
		
		HttpPost request = new HttpPost(restEndpoint);
		request.addHeader("X-Events-API-AccountName", accountName);
		request.addHeader("X-Events-API-Key", apiKey);

		request.addHeader("Content-Type", "application/vnd.appd.events+json;v=2");
		request.addHeader("Accept", "application/vnd.appd.events+json;v=2");

	    StringEntity entity = new StringEntity(payload);
	    request.setEntity(entity);
	    
	    CloseableHttpResponse response = client.execute(request);
		
	    logr.info(" - Schema: " + schemaName + " : HTTP Status: " + response.getStatusLine().getStatusCode());
	    
		String resp = "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuilder out = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            out.append(line);
        }			
		
        resp = out.toString();
		reader.close();
		
		logr.debug("Publish Events Response");
		logr.debug(resp);

		HttpClientUtils.closeQuietly(response);
		HttpClientUtils.closeQuietly(client);
		
		
	}
	
	
}
