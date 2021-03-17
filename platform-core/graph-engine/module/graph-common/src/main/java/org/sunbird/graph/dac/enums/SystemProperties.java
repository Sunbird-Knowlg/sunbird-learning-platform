package org.sunbird.graph.dac.enums;

public enum SystemProperties {

    IL_SYS_NODE_TYPE, IL_FUNC_OBJECT_TYPE, IL_UNIQUE_ID, IL_TAG_NAME, IL_ATTRIBUTE_NAME, IL_INDEXABLE_METADATA_KEY, IL_NON_INDEXABLE_METADATA_KEY, 
	IL_IN_RELATIONS_KEY, IL_OUT_RELATIONS_KEY, IL_REQUIRED_PROPERTIES, IL_SYSTEM_TAGS_KEY, IL_SEQUENCE_INDEX;

    public static boolean isSystemProperty(String str) {
        SystemProperties val = null;
        try {
            val = SystemProperties.valueOf(str);
        } catch (Exception e) {
        }
        if (null == val)
            return false;
        return true;
    }

}
