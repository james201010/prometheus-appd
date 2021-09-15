package com.appdynamics.cloud.prometheus.sigv4;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.appdynamics.cloud.prometheus.Logger;

/**
 * Samples showing how to GET an object from Amazon S3 using Signature V4
 * authorization.
 */
public class Sigv4Client {
    
	private static Logger logr = new Logger(Sigv4Client.class.getSimpleName());
	
    /**
     * Request the content of the object '/ExampleObject.txt' from the given
     * bucket in the given region using virtual hosted-style object addressing.
     */
    public static String processRequest(String endpointUrlWithParms, String regionName, String awsAccessKey, String awsSecretKey, Map<String, String> queryParameters) {
        
        // the region-specific endpoint to the target object expressed in path style
        URL endpointUrl;
        try {
            endpointUrl = new URL(endpointUrlWithParms);
            // endpointUrl = new URL("https://" + bucketName + ".s3.amazonaws.com/ExampleObject.txt");
        } catch (MalformedURLException e) {
            throw new RuntimeException("Unable to parse service endpoint: " + e.getMessage());
        }
        
        // for a simple GET, we have no body so supply the precomputed 'empty' hash
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("x-amz-content-sha256", Sigv4SignerBase.EMPTY_BODY_SHA256);
        
        Sigv4SignerForAuth signer = new Sigv4SignerForAuth(
                endpointUrl, "GET", "aps", regionName);
        String authorization = signer.computeSignature(headers, 
        											   queryParameters, // no query parameters
                                                       Sigv4SignerBase.EMPTY_BODY_SHA256, 
                                                       awsAccessKey, 
                                                       awsSecretKey);
                
        // place the computed signature into a formatted 'Authorization' header
        // and call S3
        headers.put("Authorization", authorization);
        String response = Sigv4HttpUtils.invokeHttpRequest(endpointUrl, "GET", headers, null);
        logr.info("--------- Response content ---------");
        logr.info(response);
        logr.info("------------------------------------");
        
        return response;
    }
}
