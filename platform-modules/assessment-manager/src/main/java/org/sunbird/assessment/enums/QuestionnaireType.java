package org.sunbird.assessment.enums;

import org.apache.commons.lang3.StringUtils;

public enum QuestionnaireType {
    dynamic, materialised;
    
    public static boolean isValidQuestionnaireType(String str) {
        QuestionnaireType val = null;
        try {
            QuestionnaireType[] types = QuestionnaireType.values();
            for (QuestionnaireType type : types) {
                if (StringUtils.equals(type.name(), str))
                    val = type;
            }
        } catch (Exception e) {
        }
        if (null == val)
            return false;
        return true;
    }
    
    public static QuestionnaireType getQuestionnaireType(String str) {
        return QuestionnaireType.valueOf(str);
    }
}
