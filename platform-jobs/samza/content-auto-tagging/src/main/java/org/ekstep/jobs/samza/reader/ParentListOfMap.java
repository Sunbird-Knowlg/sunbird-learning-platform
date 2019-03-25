package org.ekstep.jobs.samza.reader;


import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import java.util.List;
import java.util.Map;

public class ParentListOfMap implements ParentType {
    //TODO#: Make this class more genic.
    List<Map<String, Object>> list;
    String childKey;

    public ParentListOfMap(List<Map<String, Object>> list, String childKey) {
        this.list = list;
        this.childKey = childKey;
    }

    @Override
    public <T> T readChild() {
        if (list == null) {
            return null;
        }
        for (Object itemsObject : list) {
            if (!(itemsObject instanceof Map)) {
                continue;
            }
            Map<String, Object> items = (Map<String, Object>) itemsObject;
            if (items.containsKey(childKey)) {
                Object o = items.get(childKey);
                if (o instanceof List && ((List) o).size() > 0) {
                    return (T) ((List) o).get(0);
                }
            }
        }
        return null;
    }

    @Override
    public void addChild(Object value) {
        throw new NotImplementedException();
    }
}

