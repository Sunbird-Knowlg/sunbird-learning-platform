package org.ekstep.jobs.samza.task;

import org.apache.samza.config.Config;

public class ContentAutoTaggingConfig {

    private final String JOB_NAME = "ContentAutoTaggging";

    private String metricsTopic;
    private String apiEndPoint;

    public ContentAutoTaggingConfig(Config config) {
        this.metricsTopic = config.get("output.metrics.topic.name");
        this.apiEndPoint = config.get("daggit.api.endpoint");
    }

    public String getJobName() {
        return JOB_NAME;
    }

    public String getMetricsTopic() {
        return metricsTopic;
    }

    public String getAPIEndPoint() {
        return apiEndPoint;
    }
}
