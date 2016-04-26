package com.ilimi.graph.cache.util;

public class RedisKeyGenerator {

    private static final String KEY_SEPARATOR = ":";

    public static String getWordKey(String word){
    	return RedisKeysEnum.WORD.name() + KEY_SEPARATOR+ word;
    }
    
    public static String getUniqueIdKey() {
        return RedisKeysEnum.UNIQUE_ID.name();
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

    public static String getDefNodeRequiredMetadataKey(String graphId, String objectType) {
        return getDefNodeMetadataKey(graphId, objectType, RedisKeysEnum.REQUIRED_METADATA.name());
    }

    public static String getDefNodeIndexedMetadataKey(String graphId, String objectType) {
        return getDefNodeMetadataKey(graphId, objectType, RedisKeysEnum.INDEXED_METADATA.name());
    }

    public static String getDefNodeNonIndexedMetadataKey(String graphId, String objectType) {
        return getDefNodeMetadataKey(graphId, objectType, RedisKeysEnum.NON_INDEXED_METADATA.name());
    }

    public static String getDefNodeOutRelationsKey(String graphId, String objectType) {
        return getDefNodeMetadataKey(graphId, objectType, RedisKeysEnum.OUT_RELATIONS.name());
    }

    public static String getDefNodeInRelationsKey(String graphId, String objectType) {
        return getDefNodeMetadataKey(graphId, objectType, RedisKeysEnum.IN_RELATIONS.name());
    }

    public static String getAllGraphKeysPattern(String graphId) {
        return graphId + KEY_SEPARATOR + "*";
    }

    private static String getDefNodeMetadataKey(String graphId, String objectType, String key) {
        return graphId + KEY_SEPARATOR + RedisKeysEnum.DEF_NODE + KEY_SEPARATOR + removeSpaces(objectType) + KEY_SEPARATOR + key;
    }

    private static String removeSpaces(String str) {
        return str.replaceAll("\\s", "");
    }
}
