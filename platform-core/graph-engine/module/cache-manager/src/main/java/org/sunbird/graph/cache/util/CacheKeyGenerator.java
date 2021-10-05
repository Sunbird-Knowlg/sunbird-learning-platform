package org.sunbird.graph.cache.util;

public class CacheKeyGenerator {

    private static final String KEY_SEPARATOR = ":";
    
    public static String getUniqueIdKey() {
        return RedisKeysEnum.UNIQUE_ID.name();
    }

    public static String getKey(String graphId, String identifier, String type) {
    		return graphId + KEY_SEPARATOR + type + KEY_SEPARATOR + removeSpaces(identifier);
    }
    
    public static String getKey(String graphId, String identifier) {
		return graphId + KEY_SEPARATOR + removeSpaces(identifier);
    }
    
    public static String getSetMembersKey(String graphId, String nodeId) {
        return graphId + KEY_SEPARATOR + RedisKeysEnum.SET + KEY_SEPARATOR + nodeId + KEY_SEPARATOR + RedisKeysEnum.MEMBERS;
    }

    public static String getSetCriteriaKey(String graphId, String objectType) {
        return graphId + KEY_SEPARATOR + RedisKeysEnum.SET + KEY_SEPARATOR + objectType + KEY_SEPARATOR + RedisKeysEnum.CRITERIA
                + KEY_SEPARATOR;
    }

    public static String getSetCriteriaValueKey(String graphId, String setId, String key) {
        return graphId + KEY_SEPARATOR + RedisKeysEnum.SET + KEY_SEPARATOR + setId + KEY_SEPARATOR + RedisKeysEnum.VALUE + KEY_SEPARATOR
                + key;
    }

    public static String getTagMembersKey(String graphId, String nodeId) {
        return graphId + KEY_SEPARATOR + RedisKeysEnum.TAG + KEY_SEPARATOR + nodeId + KEY_SEPARATOR + RedisKeysEnum.MEMBERS;
    }

    public static String getSequenceMembersKey(String graphId, String nodeId) {
        return graphId + KEY_SEPARATOR + RedisKeysEnum.SEQ + KEY_SEPARATOR + nodeId + KEY_SEPARATOR + RedisKeysEnum.MEMBERS;
    }

    public static String getDefNodeKey(String graphId, String objectType) {
    	return graphId + KEY_SEPARATOR + RedisKeysEnum.DEF_NODE + KEY_SEPARATOR + removeSpaces(objectType);
    }

    public static String getNodePropertyKey(String graphId, String objectId, String propertyName) {
    	return graphId + KEY_SEPARATOR + removeSpaces(objectId) + KEY_SEPARATOR + removeSpaces(propertyName);
    }
    
    public static String getAllNodePropertyKeysPattern(String graphId, String propertyName) {
    	return graphId + KEY_SEPARATOR + "*" + KEY_SEPARATOR + removeSpaces(propertyName);
    }
    
    public static String getAllGraphKeysPattern(String graphId) {
        return graphId + KEY_SEPARATOR + "*";
    }

    private static String removeSpaces(String str) {
        return str.replaceAll("\\s", "");
    }
}
