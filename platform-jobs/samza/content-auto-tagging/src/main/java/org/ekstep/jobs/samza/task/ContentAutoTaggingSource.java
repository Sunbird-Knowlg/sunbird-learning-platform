package org.ekstep.jobs.samza.task;

import com.google.gson.Gson;
import org.apache.samza.system.IncomingMessageEnvelope;

import java.util.Map;

public class ContentAutoTaggingSource {

    private IncomingMessageEnvelope envelope;

    public ContentAutoTaggingSource(IncomingMessageEnvelope envelope) {
        this.envelope = envelope;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMap() {
        String message = (String) envelope.getMessage();
        return (Map<String, Object>) new Gson().fromJson(message, Map.class);
    }

    public String getMessage() {
        return envelope.toString();
    }
}
