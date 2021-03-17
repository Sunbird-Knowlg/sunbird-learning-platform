package org.sunbird.graph.enums;

import org.apache.commons.lang3.StringUtils;

public enum CollectionTypes {

    SET, SEQUENCE, TAG;

    public static boolean isValidCollectionType(String str) {
        CollectionTypes val = null;
        try {
            CollectionTypes[] types = CollectionTypes.values();
            for (CollectionTypes type : types) {
                if (StringUtils.equals(type.name(), str))
                    val = type;
            }
        } catch (Exception e) {
        }
        if (null == val)
            return false;
        return true;
    }
}
