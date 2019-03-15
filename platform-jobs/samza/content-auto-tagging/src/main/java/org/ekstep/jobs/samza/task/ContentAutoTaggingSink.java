package org.ekstep.jobs.samza.task;

import org.apache.samza.task.MessageCollector;
import org.ekstep.jobs.samza.service.task.JobMetrics;

public class ContentAutoTaggingSink {

    private JobMetrics metrics;
    private ContentAutoTaggingConfig config;
    private MessageCollector collector;

    public ContentAutoTaggingSink(MessageCollector collector, JobMetrics metrics, ContentAutoTaggingConfig config) {
        this.collector = collector;
        this.metrics = metrics;
        this.config = config;
    }

}
