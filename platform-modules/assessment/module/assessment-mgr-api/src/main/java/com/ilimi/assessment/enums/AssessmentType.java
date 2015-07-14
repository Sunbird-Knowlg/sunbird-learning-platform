package com.ilimi.assessment.enums;

import org.apache.commons.lang3.StringUtils;

public enum AssessmentType {
    mcq, mmcq, sort_list, ftb, mtf, speech_question, canvas_question;
    
    public static boolean isValidAssessmentType(String str) {
        AssessmentType val = null;
        try {
            AssessmentType[] types = AssessmentType.values();
            for (AssessmentType type : types) {
                if (StringUtils.equals(type.name(), str))
                    val = type;
            }
        } catch (Exception e) {
        }
        if (null == val)
            return false;
        return true;
    }
    
    public static AssessmentType getAssessmentType(String str) {
        return AssessmentType.valueOf(str);
    }
    
}
