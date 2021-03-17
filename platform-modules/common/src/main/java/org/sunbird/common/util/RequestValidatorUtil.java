package org.sunbird.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.text.WordUtils.capitalize;

public class RequestValidatorUtil {

    private static Function<String, String> prefixValidator = e -> null ==  e ?  "REQUEST" : e;

    public static boolean isEmptyOrNull(Object... o) {
        if (Objects.isNull(o)) {
            return true;
        }
        for (Object e : o) {
            if (Objects.isNull(e)) {
                return true;
            }
            if (e instanceof String) {
                return isBlank((String) e);
            }
            if (e instanceof String[]) {
                return 0 == ((String[])e).length;
            }
            if (e instanceof Collection) {
                Collection c = (Collection) e;
                if (c.isEmpty()) {
                    return true;
                }
            }
            if (e instanceof Map) {
                Map m = (Map) e;
                if (m.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isEmptyOrNullMapRecursive(Map m) {
        if (m.isEmpty()) { return true; }
        for (Entry entry : (Set<Entry>) m.entrySet()) {
            if (isEmptyOrNull(entry.getValue())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEmptyOrNullObjectForMapRecursive(Object... o) {
        if (Objects.isNull(o)) {
            return true;
        }
        for (Object e : o) {
            if (e instanceof Map) {
                return isEmptyOrNullMapRecursive((Map) e);
            }
            return isEmptyOrNull(e);
        }
        return false;
    }

    public static List getEmptyOrNullKeysFromMap(Map<String, Object> map) {
        List<String> keys = new ArrayList<>();
        if (isEmptyOrNull(map)) {
            keys.add("REQUEST");
            return keys;
        }
        for (Entry<String, Object> entry : map.entrySet()) {
            if (isEmptyOrNull(entry.getValue())) {
                keys.add(entry.getKey());
            }
        }
        return keys;
    }

    public static List<String> getEmptyOrNullKeysFromMapRecursive(Map<String, Object> requestMap, String prefix) {
        List<String> keys = new ArrayList<>();
        if (Objects.isNull(requestMap) || requestMap.isEmpty()) {
            keys.add("requestMap");
            return keys;
        }
        for (Entry<String, Object> entry : requestMap.entrySet()) {
            String updatedPrefix = prefix + "." + splitCamelCase(capitalize(entry.getKey()));
            if (entry.getValue() instanceof Map) {
                keys.addAll(
                        getEmptyOrNullKeysFromMapRecursive(
                            (Map) entry.getValue(),
                            updatedPrefix));
            }
            if (isEmptyOrNull(entry.getValue())) {
                keys.add(updatedPrefix);
            }
        }
        return keys;
    }

    public static List<String> getEmptyOrNullKeysFromMapRecursive(Map<String, Object> map,
                                                                  String prefix,
                                                                  String prefixKey,
                                                                  Set<String> keys) {
        List<String> emptyKeys = new ArrayList<>();
        if (isEmptyOrNull(map)) {
            emptyKeys.add(prefix);
            return emptyKeys;
        }
        for (Entry<String, Object> entry : map.entrySet()) {
            String updatedPrefix = prefix + "." + splitCamelCase(capitalize(entry.getKey()));
            String updatedPrefixKey = prefixKey + "." + entry.getKey();
            if (entry.getValue() instanceof Map && keys.contains(updatedPrefixKey)) {
                emptyKeys.addAll(
                        getEmptyOrNullKeysFromMapRecursive(
                                (Map) entry.getValue(),
                                updatedPrefix,
                                updatedPrefixKey,
                                keys));
            }
            if (isEmptyOrNull(entry.getValue()) && keys.contains(updatedPrefixKey)) {
                emptyKeys.add(updatedPrefix);
            }
        }
        return emptyKeys;
    }

    public static Stream<String> getEmptyErrorMessagesFor(List<String> emptyRequestKeys) {
        return emptyRequestKeys.stream().map(e -> e + "  cannot be Blank or Null");
    }

    public static List<String> getEmptyErrorMessagesFor(Map<String, Object> requestMap, String prefix) {
        return getEmptyErrorMessagesFor(getEmptyOrNullKeysFromMapRecursive(requestMap, prefixValidator.apply(prefix))).
                collect(toList());
    }

    public static String getEmptyErrorMessageFor(Map<String, Object> requestMap, String prefix) {
        return getEmptyErrorMessagesFor(getEmptyOrNullKeysFromMapRecursive(requestMap, prefixValidator.apply(prefix))).
                collect(joining(",\n"));
    }

    public static String getEmptyErrorMessageFor(Map<String, Object> requestMap, String prefix, String[] keys) {
        String validatedPrefix = prefixValidator.apply(prefix);
        Set<String> keySet = getKeySet(validatedPrefix, keys);
        return getEmptyErrorMessagesFor(getEmptyOrNullKeysFromMapRecursive(requestMap,
                                                                            validatedPrefix,
                                                                            validatedPrefix,
                                                                            keySet)).
                collect(joining(",\n"));
    }

    public static String getEmptyErrorMessageFor(Map<String, Object> requestMap, String prefix, List<String> keys) {
        String validatedPrefix = prefixValidator.apply(prefix);
        Set<String> keySet = getKeySet(validatedPrefix, keys);
        return getEmptyErrorMessagesFor(getEmptyOrNullKeysFromMapRecursive(requestMap,
                                                                            validatedPrefix,
                                                                            validatedPrefix,
                                                                            keySet)).
                collect(joining(",\n"));
    }

    private static Set<String> getKeySet(String prefix, Object keys) {
        return getStream(keys).
                map(e -> prefix + "." + e).
                collect(toSet());
    }

    private static Stream<String> getStream(Object o) {
        if (o instanceof String[]) {
            return Arrays.stream((String[]) o);
        }
        if (o instanceof Collection) {
            return ((Collection) o).stream();
        }
        return null;
    }

    public static String splitCamelCase(String s) {
        return s.replaceAll(
                String.format("%s|%s|%s",
                        "(?<=[A-Z])(?=[A-Z][a-z])",
                        "(?<=[^A-Z])(?=[A-Z])",
                        "(?<=[A-Za-z])(?=[^A-Za-z])"),
                " ");
    }

}
