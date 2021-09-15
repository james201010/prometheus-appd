/**
 * 
 */
package com.appdynamics.cloud.prometheus.analytics;

import com.appdynamics.cloud.prometheus.config.AnalyticsEventsSourceConfig;
import com.appdynamics.cloud.prometheus.config.ServiceConfig;

/**
 * Interface for a provider of an Analytics Schema and the Events data to be published to it
 * 
 * @author James Schneider
 *
 */
public interface AnalyticsEventsSource {
	
	// This is called just after constructing an instance implementation so the source can initialize once
	public abstract void initialize(ServiceConfig serviceConfig, AnalyticsEventsSourceConfig analyticsEventsSourceConfig) throws Throwable;
	
	// Used in the publishing of the events to identify the correct schema to publish to
	public abstract String getSchemaName() throws Throwable;
	
	// TODO Not is use as of yet
	public abstract String getSchemaDefinitionJson() throws Throwable;
	
	// Called once per "getExecutionInterval()" to get the next batch of events in JSON format to publish
	public abstract String getEvents2PublishJson() throws Throwable;
	
	
	// Defines the interval in minutes which the next batch of events will be published
	public abstract int getExecutionInterval() throws Throwable;
	
	

}
