package org.ekstep.jobs.samza.task;

import org.apache.samza.task.MessageCollector;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import java.util.Map;

public class ContentAutoTaggingSink {

    private JobMetrics metrics;
    private ContentAutoTaggingConfig config;
    private MessageCollector collector;

    public ContentAutoTaggingSink(MessageCollector collector, JobMetrics metrics, ContentAutoTaggingConfig config) {
        this.collector = collector;
        this.metrics = metrics;
        this.config = config;
    }

    public void success() { metrics.incSuccessCounter(); }

    public void failed() {
        metrics.incFailedCounter();
    }

    public void error() { metrics.incErrorCounter(); }

    public void skip() { metrics.incSkippedCounter(); }

}
