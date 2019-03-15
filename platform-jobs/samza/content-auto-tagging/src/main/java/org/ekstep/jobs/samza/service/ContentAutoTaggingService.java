package org.ekstep.jobs.samza.service;

import org.apache.samza.config.Config;
import org.apache.samza.task.MessageCollector;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.task.ContentAutoTaggingConfig;
import org.ekstep.jobs.samza.task.ContentAutoTaggingSink;
import org.ekstep.jobs.samza.task.ContentAutoTaggingSource;
import org.ekstep.jobs.samza.util.JobLogger;

import java.util.Map;

public class ContentAutoTaggingService {

    private static JobLogger LOGGER = new JobLogger(ContentAutoTaggingService.class);
    private final ContentAutoTaggingConfig config;

    public ContentAutoTaggingService(ContentAutoTaggingConfig config) {
        this.config = config;
    }

    public void process(ContentAutoTaggingSource source, ContentAutoTaggingSink sink) {

    }
}
