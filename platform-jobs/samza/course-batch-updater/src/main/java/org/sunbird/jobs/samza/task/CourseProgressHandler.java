package org.sunbird.jobs.samza.task;

import org.apache.commons.collections4.MapUtils;

import java.util.HashMap;
import java.util.Map;

public class CourseProgressHandler {

    private Map<String, Object> batchProgressEvents = new HashMap<>();
    
    public void clear() {
        batchProgressEvents.clear();
    }
    
    public String getKey(String batchId, String userId) {
        return batchId + "_" + userId;
    }
    
    public CourseProgressHandler put(String key, Map<String, Object> value) {
        batchProgressEvents.put(key, value);
        return this;
    }
    
    public Object get(String key) {
        return batchProgressEvents.get(key);
    }
    
    public boolean containsKey(String key) {
        return batchProgressEvents.containsKey(key);
    }
    
    public boolean isNotEmpty() {
        return MapUtils.isNotEmpty(batchProgressEvents);
    }
    
    public Map<String, Object> getMap() {
        return batchProgressEvents;
    }

    public int size() {
        return batchProgressEvents.size();
    }
}
