package org.ekstep.common.util;

import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.text.WordUtils.capitalize;

public class RequestValidatorUtil {

    private static final String defaultRequestPrefix = "request";
    private static Function<String, String> prefixValidator = e -> null ==  e ?  defaultRequestPrefix : e;

    public static boolean isEmptyOrNull(Object o) { return isNull(o); }

    public static boolean isEmptyOrNull(Object... o) {
        if (isNull(o)) return true;
        for (Object e : o) if (isEmptyOrNull(e)) return true;
        return false;
    }

    public static boolean isEmptyOrNull(String s) {
        return isBlank(s);
    }

    public static boolean isEmptyOrNull(Collection c) {
        return isNull(c) || c.isEmpty();
    }

    public static boolean isEmptyOrNull(Map m) {
        return isNull(m) || m.isEmpty();
    }

    public static boolean isEmptyOrNullRecursive(Object o) {
        if (isNull(o)) return true;
        return false;
    }

    public static boolean isEmptyOrNullRecursive(Object... o) {
        if (isNull(o)) return true;
        for (Object e : o) if (isEmptyOrNullRecursive(e)) return true;
        return false;
    }

    public static boolean isEmptyOrNullRecursive(String s) {
        return isEmptyOrNull(s);
    }

    public static boolean isEmptyOrNullRecursive(String... s) {
        return isEmptyOrNull(s);
    }

    public static boolean isEmptyOrNullRecursive(Collection c) {
        if (isNull(c)) return true;
        for (Object e : c) if (isEmptyOrNullRecursive(e)) return true;
        return false;
    }

    public static boolean isEmptyOrNullRecursive(Map m) {
        return isNull(m) || isEmptyMapRecursive(m);
    }

    public static boolean isEmptyOrNullRecursive(Map... m) {
        if (isNull(m)) return true;
        for (Map e : m) if (isEmptyOrNullRecursive(e)) return true;
        return false;
    }

    public static boolean isEmptyMapRecursive(Map m) {
        if (m.isEmpty()) return true;
        for (Entry entry : (Set<Entry>) m.entrySet()) {
            if (isEmptyOrNullRecursive(entry.getValue())) return true;
        }
        return false;
    }

    public static List getEmptyOrNullKeysFromMap(Map<String, Object> map) {
        List<String> keys = new ArrayList<>();
        if (isEmptyOrNull(map)) {
            keys.add(defaultRequestPrefix);
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
        if (isNull(requestMap) || requestMap.isEmpty()) {
            keys.add(defaultRequestPrefix);
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
                                                                  String prefixKey,
                                                                  Set<String> keys) {
        List<String> emptyKeys = new ArrayList<>();
        if (isEmptyOrNull(map)) {
            emptyKeys.add(prefixKey);
            return emptyKeys;
        }
        for (Entry<String, Object> entry : map.entrySet()) {
            String updatedPrefixKey = prefixKey + "." + entry.getKey();
            if (entry.getValue() instanceof Map && keys.contains(updatedPrefixKey)) {
                keys.remove(updatedPrefixKey);
                emptyKeys.addAll(
                        getEmptyOrNullKeysFromMapRecursive(
                                (Map) entry.getValue(),
                                updatedPrefixKey,
                                keys));
            }
            if (keys.contains(updatedPrefixKey)) {
                keys.remove(updatedPrefixKey);
                if (isEmptyOrNull(entry.getValue())) emptyKeys.add(updatedPrefixKey);
            }
        }
        return emptyKeys;
    }

    public static List<String> getEmptyOrNullKeysFromMapRecursiveAnValidateInternally(Map<String, Object> map,
                                                                                     String prefixKey,
                                                                                     Set<String> keys) {
        List<String> emptyKeys = new ArrayList<>();
        if (isEmptyOrNull(map)) {
            emptyKeys.add(prefixKey);
            return emptyKeys;
        }
        for (Entry<String, Object> entry : map.entrySet()) {
            String updatedPrefixKey = prefixKey + "." + entry.getKey();
            if (entry.getValue() instanceof Map && keys.contains(updatedPrefixKey)) {
                keys.remove(updatedPrefixKey);
                emptyKeys.addAll(
                        getEmptyOrNullKeysFromMapRecursiveAnValidateInternally(
                                (Map) entry.getValue(),
                                updatedPrefixKey,
                                keys));
            }
            if (keys.contains(updatedPrefixKey)) {
                keys.remove(updatedPrefixKey);
                if (isEmptyOrNullRecursive(entry.getValue())) emptyKeys.add(updatedPrefixKey);
            }
        }
        return emptyKeys;
    }

    public static List<String> getEmptyOrNullKeysFromMapRecursive(Map<String, Object> map,
                                                                  String prefixKey,
                                                                  Map<String, Class> keys) {
        List<String> emptyKeys = new ArrayList<>();
        if (isEmptyOrNull(map)) {
            emptyKeys.add(prefixKey);
            return emptyKeys;
        }
        for (Entry<String, Object> entry : map.entrySet()) {
            String updatedPrefixKey = prefixKey + "." + entry.getKey();
            if (keys.containsKey(updatedPrefixKey) &&
                    keys.get(updatedPrefixKey).isAssignableFrom(entry.getValue().getClass())) {
                keys.remove(updatedPrefixKey);
                if (entry.getValue() instanceof Map) {
                    emptyKeys.addAll(
                            getEmptyOrNullKeysFromMapRecursive(
                                    (Map) entry.getValue(),
                                    updatedPrefixKey,
                                    keys));
                }
                if (isEmptyOrNull(entry.getValue())) emptyKeys.add(updatedPrefixKey);
            }
        }
        return emptyKeys;
    }

    public static List<String> getEmptyOrNullKeysFromMapRecursiveAnValidateInternally(Map<String, Object> map,
                                                                                      String prefixKey,
                                                                                      Map<String, Class> keys) {
        List<String> emptyKeys = new ArrayList<>();
        if (isEmptyOrNull(map)) {
            emptyKeys.add(prefixKey);
            return emptyKeys;
        }
        for (Entry<String, Object> entry : map.entrySet()) {
            String updatedPrefixKey = prefixKey + "." + entry.getKey();
            if (keys.containsKey(updatedPrefixKey) &&
                    keys.get(updatedPrefixKey).isAssignableFrom(entry.getValue().getClass())) {
                keys.remove(updatedPrefixKey);
                if (entry.getValue() instanceof Map) {
                    emptyKeys.addAll(
                            getEmptyOrNullKeysFromMapRecursiveAnValidateInternally(
                                    (Map) entry.getValue(),
                                    updatedPrefixKey,
                                    keys));
                }
                if (isEmptyOrNullRecursive(entry.getValue())) emptyKeys.add(updatedPrefixKey);
            }
        }
        return emptyKeys;
    }

    public static Stream<String> getEmptyErrorMessagesFor(List<String> emptyRequestKeys) {
        return emptyRequestKeys.stream().map(e -> e + " request key is not present or having invalid value.");
    }

    public static String getEmptyErrorMessageFor(List<String> emptyRequestKeys) {
        return CollectionUtils.isEmpty(emptyRequestKeys) ?
                "" : emptyRequestKeys +  "  request key(s) are not present or having invalid value.";
    }

    public static List<String> getEmptyErrorMessagesFor(Map<String, Object> requestMap, String prefix) {
        return getEmptyErrorMessagesFor(getEmptyOrNullKeysFromMapRecursive(requestMap, prefixValidator.apply(prefix))).
                collect(toList());
    }

    public static String getEmptyErrorMessageFor(Map<String, Object> requestMap,
                                                 String prefix) {
        return getEmptyErrorMessagesFor(getEmptyOrNullKeysFromMapRecursive(requestMap, prefixValidator.apply(prefix))).
                collect(joining(",\n"));
    }

    public static String getEmptyErrorMessageFor(Map<String, Object> requestMap,
                                                 String prefix,
                                                 String[] keys) {
        String validatedPrefix = prefixValidator.apply(prefix);
        Set<String> keySet = getKeySet(validatedPrefix, keys);
        return getEmptyErrorMessageFor(requestMap, validatedPrefix, keySet);
    }

    public static String getEmptyErrorMessageFor(Map<String, Object> requestMap,
                                                 String prefix,
                                                 List<String> keys) {
        String validatedPrefix = prefixValidator.apply(prefix);
        Set<String> keySet = getKeySet(validatedPrefix, keys);
        return getEmptyErrorMessageFor(requestMap, validatedPrefix, keySet);
    }


    public static String getEmptyErrorMessageRecursiveFor(Map<String, Object> requestMap,
                                                          String prefix,
                                                          String[] keys) {
        String validatedPrefix = prefixValidator.apply(prefix);
        Set<String> keySet = getKeySet(validatedPrefix, keys);
        return getEmptyErrorMessageRecursiveFor(requestMap, validatedPrefix, keySet);
    }
    public static String getEmptyErrorMessageFor(Map<String, Object> requestMap,
                                                 String prefix,
                                                 Map<String, Class> keys) {
        String validatedPrefix = prefixValidator.apply(prefix);
        Map<String, Class> validatorKeyMap = getKeyMap(validatedPrefix, keys);
        return getEmptyErrorMessageFor(
                appendNotValidatedKeysToEmptyKeys(
                        validatorKeyMap, getEmptyOrNullKeysFromMapRecursive(requestMap,
                                validatedPrefix,
                                validatorKeyMap)));
    }

    public static String getEmptyErrorMessageRecursiveFor(Map<String, Object> requestMap,
                                                          String prefix,
                                                          Map<String, Class> keys) {
        String validatedPrefix = prefixValidator.apply(prefix);
        Map<String, Class> validatorKeyMap = getKeyMap(validatedPrefix, keys);
        return getEmptyErrorMessageFor(
                appendNotValidatedKeysToEmptyKeys(
                        validatorKeyMap, getEmptyOrNullKeysFromMapRecursiveAnValidateInternally(
                                requestMap,
                                validatedPrefix,
                                validatorKeyMap)));
    }

    public static String getEmptyErrorMessageRecursiveFor(Map<String, Object> requestMap,
                                                 String prefix,
                                                 List<String> keys) {
        String validatedPrefix = prefixValidator.apply(prefix);
        Set<String> keySet = getKeySet(validatedPrefix, keys);
        return getEmptyErrorMessageRecursiveFor(requestMap, validatedPrefix, keySet);
    }

    private static String getEmptyErrorMessageFor(Map<String, Object> requestMap,
                                                  String validatedPrefix,
                                                  Set<String> validatorKeySet) {
        return getEmptyErrorMessageFor(
                appendNotValidatedKeysToEmptyKeys(
                        validatorKeySet, getEmptyOrNullKeysFromMapRecursive(requestMap,
                                                                            validatedPrefix,
                                                                            validatorKeySet)));
    }

    private static String getEmptyErrorMessageRecursiveFor(Map<String, Object> requestMap,
                                                           String validatedPrefix,
                                                           Set<String> validatorKeySet) {
        return getEmptyErrorMessageFor(
                appendNotValidatedKeysToEmptyKeys(
                        validatorKeySet, getEmptyOrNullKeysFromMapRecursiveAnValidateInternally(requestMap,
                                                                                                validatedPrefix,
                                                                                                validatorKeySet)));
    }

    private static List<String> appendNotValidatedKeysToEmptyKeys(Object keys, List<String> emptyKeys) {
        if (keys instanceof Set || keys instanceof List) {
            emptyKeys.addAll((Collection) keys);
        }
        if (keys instanceof Map) {
            emptyKeys.addAll(((Map)keys).keySet());
        }
        return emptyKeys;
    }

    private static Set<String> getKeySet(String prefix, Object keys) {
        return getStream(keys).
                map(e -> prefix + "." + e).
                collect(toSet());
    }

    private static Map<String, Class> getKeyMap(String prefix, Map<String, Class> keys) {
        return keys.entrySet().stream().
                collect(toMap(e -> prefix + "." + e.getKey(),
                              e -> e.getValue(),
                              (u,v) -> u,
                              LinkedHashMap::new));
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
