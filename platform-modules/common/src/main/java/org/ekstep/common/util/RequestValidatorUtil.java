package org.ekstep.common.util;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

public class RequestValidatorUtil {

    public static boolean isValidNonMapObject(Object... o) {
        if (Objects.isNull(o)) {
            return false;
        }
        for (Object e : o) {
            if (Objects.isNull(e)) {
                return false;
            }
            if (e instanceof String) {
                return StringUtils.isBlank((String) e);
            }
            if (e instanceof String[]) {
                return 0 != ((String[]) e).length;
            }
            if (e instanceof Collection) {
                Collection c = (Collection) e;
                if (c.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isValidObject(Object... o) {
        if (Objects.isNull(o)) {
            return false;
        }
        for (Object e : o) {
            if (Objects.isNull(e)) {
                return false;
            }
            if (e instanceof String) {
                return StringUtils.isBlank((String) e);
            }
            if (e instanceof String[]) {
                return 0 != ((String[])e).length;
            }
            if (e instanceof Collection) {
                Collection c = (Collection) e;
                if (c.isEmpty()) {
                    return false;
                }
            }
            if (e instanceof Map) {
                Map m = (Map) e;
                if (m.isEmpty()) return false;
                for (Entry entry : (Set<Entry>) m.entrySet()) {
                    if (!isValidObject(entry.getValue())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static List getNonValidKeysFromMap(Map<String, Object> requestMap) {
        List<String> keys = new ArrayList<>();
        if (Objects.isNull(requestMap) || requestMap.isEmpty()) {
            keys.add("requestMap");
            return keys;
        }
        for (Entry<String, Object> entry : requestMap.entrySet()) {
            if (!isValidNonMapObject(entry.getValue())) {
                keys.add(entry.getKey());
            }
        }
        return keys;
    }

    public static List getNonValidKeysFromMapRecursive(Map<String, Object> requestMap, String prefix) {
        List<String> keys = new ArrayList<>();
        if (Objects.isNull(requestMap) || requestMap.isEmpty()) {
            keys.add("requestMap");
            return keys;
        }
        for (Entry<String, Object> entry : requestMap.entrySet()) {
            if (entry.getValue() instanceof Map) {
                keys.addAll(
                        getNonValidKeysFromMapRecursive(
                            (Map) entry.getValue(),
                            prefix + "." + entry.getKey()));
            }
            if (!isValidNonMapObject(entry.getValue())) {
                keys.add(prefix + "." + entry.getKey());
            }
        }
        return keys;
    }

    public static boolean isValidRequestMap(Map m) {
        if(getNonValidKeysFromMap(m).isEmpty()) {
            return false;
        }
        return true;
    }

}
