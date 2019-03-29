package org.ekstep.jobs.samza.service;

import com.google.gson.Gson;
import org.apache.samza.config.Config;
import org.apache.samza.task.MessageCollector;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.task.ContentAutoTaggingConfig;
import org.ekstep.jobs.samza.task.ContentAutoTaggingSink;
import org.ekstep.jobs.samza.task.ContentAutoTaggingSource;
import org.ekstep.jobs.samza.util.*;

public class ContentAutoTaggingService {

    private static JobLogger LOGGER = new JobLogger(ContentAutoTaggingService.class);
    private final ContentAutoTaggingConfig config;
    private DaggitServiceClient client;

    public ContentAutoTaggingService(ContentAutoTaggingConfig config, DaggitServiceClient client) {

        this.config = config;
        this.client = client;
    }

    public void process(ContentAutoTaggingSource source, ContentAutoTaggingSink sink) {

        GraphEvent event = null;
        try {
            event = source.getEvent();
            String operationType = event.getOperationType();
            if(operationType.equals("CREATE")) {
                String contentId = event.getContentId();
                DaggitAPIResponse response = client.submit(contentId);
                if(response.successful()) {
                    LOGGER.info("Successfully submitted request");
                    sink.success();
                }
                else {
                    LOGGER.info("Submit API call failed");
                    sink.failed();
                }
            }

            else
            {
                LOGGER.info("Skipping as event type is other than create");
                sink.skip();
            }

        }
        catch (Exception e) {
            LOGGER.info("Exception: "+ e.getMessage());
            e.printStackTrace();
            sink.error();
        }
    }
}
