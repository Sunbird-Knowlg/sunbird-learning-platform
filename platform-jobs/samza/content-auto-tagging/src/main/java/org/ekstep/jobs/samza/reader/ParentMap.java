package org.ekstep.jobs.samza.reader;

import java.util.Map;

public class ParentMap implements ParentType {
    Map<String, Object> map;
    String childKey;

    public ParentMap(Map<String, Object> map, String childKey) {
        this.map = map;
        this.childKey = childKey;
    }

    @Override
    public <T> T readChild() {
        if (map != null && map.containsKey(childKey) && map.get(childKey) != null) {
            Object child = map.get(childKey);
            return (T) child;
        }
        return null;
    }

    @Override
    public void addChild(Object value) {
        if (map != null)
            map.put(childKey, value);
    }
}
