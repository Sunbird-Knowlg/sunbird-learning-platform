package org.ekstep.jobs.samza.util;

import org.ekstep.jobs.samza.reader.*;
import java.util.List;
import java.util.Map;

public class Event {
    private Map<String, Object> map;

    public Event(Map<String, Object> map) {
        this.map = map;
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public <T> NullableValue<T> read(String keyPath) {
        try {
            ParentType parentMap = lastParentMap(map, keyPath);
            return new NullableValue<T>(parentMap.<T>readChild());
        } catch (Exception e) {
            return new NullableValue<T>(null);
        }
    }

    private ParentType lastParentMap(Map<String, Object> map, String keyPath) {
        Object parent = map;
        String[] keys = keyPath.split("\\.");
        int lastIndex = keys.length - 1;
        if (keys.length > 1) {
            for (int i = 0; i < lastIndex && parent != null; i++) {
                Object o;
                if (parent instanceof Map) {
                    o = new ParentMap((Map<String, Object>) parent, keys[i]).readChild();
                } else if (parent instanceof List) {
                    o = new ParentListOfMap((List<Map<String, Object>>) parent, keys[i]).readChild();
                } else {
                    o = new NullParent(parent, keys[i]).readChild();
                }
                parent = o;
            }
        }
        String lastKeyInPath = keys[lastIndex];
        if (parent instanceof Map) {
            return new ParentMap((Map<String, Object>) parent, lastKeyInPath);
        } else if (parent instanceof List) {
            return new ParentListOfMap((List<Map<String, Object>>) parent, lastKeyInPath);
        } else {
            return new NullParent(parent, lastKeyInPath);
        }
    }

    @Override
    public String toString() {
        return "Event{" +
                "map=" + map +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event telemetry = (Event) o;

        return map != null ? map.equals(telemetry.map) : telemetry.map == null;
    }

}