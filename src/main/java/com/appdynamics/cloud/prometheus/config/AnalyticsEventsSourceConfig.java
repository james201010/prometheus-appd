/**
 * 
 */
package com.appdynamics.cloud.prometheus.config;

/**
 * @author James Schneider
 *
 */
public class AnalyticsEventsSourceConfig {
	
	private String eventsSourceClass;
	private String schemaName;
	private String executionInterval;
	
	
	
	
	/**
	 * 
	 */
	public AnalyticsEventsSourceConfig() {
		
	}



	public String getEventsSourceClass() {
		return eventsSourceClass;
	}



	public void setEventsSourceClass(String eventsSourceClass) {
		this.eventsSourceClass = eventsSourceClass;
	}



	public String getSchemaName() {
		return schemaName;
	}



	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}



	public String getExecutionInterval() {
		return executionInterval;
	}



	public void setExecutionInterval(String publishEventsInterval) {
		this.executionInterval = publishEventsInterval;
	}




	
}
