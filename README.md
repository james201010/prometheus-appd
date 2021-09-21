# AppDynamics Integration with Prometheus


## Introduction

This extension connects to a Prometheus endpoint and runs the specified queries.
Responses are then parsed and then passed to [AppDynamics](https://www.appdynamics.com) as analytics events.
[AWS AMP](https://us-west-2.console.aws.amazon.com/prometheus/home) (Amazon Managed Service for Prometheus) is supported using the [AWS Signature Version 4](https://docs.aws.amazon.com/general/latest/gr/signature-version-4.html) signing process.




## Pre-requisites

1. Java JRE 1.8 or above

2. (Optional) AWS Account with access to an AMP Wokspaces - only required if accessing AMP

3. AppDynamics controller with appropriate Analytics licence.



## Installation

### Clone package

```
$ git clone https://github.com/james201010/prometheus-appd.git
$ cd prometheus-app
```

## Configuration

### Configure primary parameters for extension

Open the the conf/config.yaml file for editing. The default configuration is below

```
!!com.appdynamics.cloud.prometheus.config.ServiceConfig

debugLogging: false
eventsServiceEndpoint: "https://analytics.api.appdynamics.com:443"
eventsServiceApikey: ""
controllerGlobalAccount: ""
prometheusUrl: "https://aps-workspaces.us-west-2.amazonaws.com/workspaces/ws-xxxx-yyy-xxxx-yyy/api/v1/query"
authenticationMode: "awssigv4"    # options are ( none | awssigv4 )
awsRegion: "us-west-2"            # mandatory if authenticationMode = awssigv4
awsAccessKey: ""                  # mandatory if authenticationMode = awssigv4
awsSecretKey: ""                  # mandatory if authenticationMode = awssigv4
  
analyticsEventsSources:
  - schemaName: "prom_node_metrics"
    schemaDefinitionFilePath: "./conf/prom_node_metrics_schema.txt"
    queriesTextFilePath: "./conf/prom_node_metrics_queries.txt"
    eventsSourceClass: "com.appdynamics.cloud.prometheus.analytics.PrometheusEventsSource"
    executionInterval: "1"  # executionInterval (is in minutes, whole numbers only)

  - schemaName: "prom_kubelet_metrics"
    schemaDefinitionFilePath: "./conf/prom_kubelet_metrics_schema.txt"
    queriesTextFilePath: "./conf/prom_kubelet_metrics_queries.txt"
    eventsSourceClass: "com.appdynamics.cloud.prometheus.analytics.PrometheusEventsSource"
    executionInterval: "1"  # executionInterval (is in minutes, whole numbers only)
```


Parameter | Function | Default Value
--------- | -------- | -------------
debugLogging | Choose to turn on debug level logging. | `false`
eventsServiceEndpoint | URL to connect to the AppDynamics controller events service. See [our documentation](https://docs.appdynamics.com/display/PRO45/Analytics+Events+API#AnalyticsEventsAPI-AbouttheAnalyticsEventsAPI) for the URL for your controller. | (blank)
eventsServiceApikey | API Key to connect to AppDynamics controller events service. See [our documentation](https://docs.appdynamics.com/display/PRO45/Managing+API+Keys) to create an API key. | (blank)
controllerGlobalAccount | Account name to connect to the AppDynamics controller. See Settings > License > Account for the value for your controller | (blank)
prometheusUrl | The URL of your Prometheus deployment | `http://localhost:9090/api/v1/query`
authenticationMode | The authentication mode needed to connect to the Prometheus deployment. The options are `none` or `awssigv4` | `none`
awsRegion | The AWS region where your AMP workspace is located (optional if `authenticationMode` is not set to `awssigv4`) | (blank)
awsAccessKey | The access key for the AWS IAM user with access to the AMP workspace (optional if `authenticationMode` is not set to `awssigv4`) | (blank)
awsSecretKey | The secret key for the AWS IAM user with access to the AMP workspace (optional if `authenticationMode` is not set to `awssigv4`) | (blank)
analyticsEventsSources | The list of sources that define the PromQL queries and their associated schema where the metrics from the queries will be published to | (one source for `prom_node_metrics` and one for `prom_kubelet_metrics`)

### Configure event sources for extension



Parameter | Function | Default Value
--------- | -------- | -------------
schemaName | Choose to turn on debug level logging. | `false`
schemaDefinitionFilePath | URL to connect to the AppDynamics controller events service. See [our documentation](https://docs.appdynamics.com/display/PRO45/Analytics+Events+API#AnalyticsEventsAPI-AbouttheAnalyticsEventsAPI) for the URL for your controller. | (blank)
queriesTextFilePath | API Key to connect to AppDynamics controller events service. See [our documentation](https://docs.appdynamics.com/display/PRO45/Managing+API+Keys) to create an API key. | (blank)
eventsSourceClass | Account name to connect to the AppDynamics controller. See Settings > License > Account for the value for your controller | (blank)

### Configure a Schema for an event source

To be able to publish Prometheus data to AppDynamics, a custom schema needs to be created in your controller. This schema must match the data types of your Prometheus data. The default schema configurations match the schemas required for the queries in conf/prom_node_metrics_queries.txt and conf/prom_kubelet_metrics_queries.txt.

Open conf/schema.json for editing.

```
{
  "name": "string",
  "instance": "string",
  "job": "string",
  "quantile": "string",
  "code": "string",
  "handler": "string",
  "value": "float"
}

```

Ensure the following:

* There is a paramter for each value returned from every Prometheus query.
* `metric_name` is required and should not be changed.
* `metric_value` is required and shold not be changed.

The extension cannot modify or delete existing schemas. If you have an existing schema which needs editing follow instructions [in our documentation](https://docs.appdynamics.com/display/PRO45/Analytics+Events+API#AnalyticsEventsAPI-update_schemaUpdateEventSchema)

### Configure Prometheus Queries for an event source

The extension has been designed to run Prometheus queries in series for each event source. By default
the extension will run two sample queries as defined in conf/queries.txt and send the data to AppD as analytics events.

Open conf/queries.txt for editing.

```
prometheus_target_interval_length_seconds
prometheus_http_requests_total
```

The two default queries are listed above. You can add and change these to match the data that you'd like to export from Prometheus to AppD. Each query should be on its own line.

Once you have added your queries you should ensure that your schema config matches the data that Prometheus will return. Failure to do this will cause an error at runtime.

## Run Extension

### Run extension - locally
If running locally the extension is ready to run. Run the extension with the
following command.

```
$ npm run run
```

or

```
$ node dist/index.js
```


