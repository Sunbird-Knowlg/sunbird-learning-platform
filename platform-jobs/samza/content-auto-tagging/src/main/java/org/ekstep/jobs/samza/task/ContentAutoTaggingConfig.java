package org.ekstep.jobs.samza.task;

import org.apache.samza.config.Config;

public class ContentAutoTaggingConfig {

    private final String JOB_NAME = "ContentAutoTaggging";

    private String metricsTopic;
    private String apiEndPoint;
    private String daggitExperimentName;

    public ContentAutoTaggingConfig(Config config) {
        this.metricsTopic = config.get("output.metrics.topic.name");
        this.apiEndPoint = config.get("daggit.api.endpoint");
        this.daggitExperimentName = config.getOrDefault("daggit.experiment.name", "diksha_content_keyword_tagging");
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

    public String getExperimentName() {
        return daggitExperimentName;
    }
}
