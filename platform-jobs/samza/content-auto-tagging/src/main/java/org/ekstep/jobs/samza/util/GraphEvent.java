package org.ekstep.jobs.samza.util;

import com.google.gson.Gson;
import org.ekstep.jobs.samza.reader.NullableValue;

import java.util.Map;

public class GraphEvent {

    private final Event event;

    public GraphEvent(Map<String, Object> map) {
        this.event = new Event(map);
    }

    public Map<String, Object> getMap() {
        return event.getMap();
    }

    public String getJson() {
        Gson gson = new Gson();
        String json = gson.toJson(getMap());
        return json;
    }

    public String getOperationType() {
        NullableValue<String> operationType = event.read("operationType");
        return operationType.value();
    }

    public String getContentId() {
        NullableValue<String> contentId = event.read("nodeUniqueId");
        return contentId.value();
    }
}
