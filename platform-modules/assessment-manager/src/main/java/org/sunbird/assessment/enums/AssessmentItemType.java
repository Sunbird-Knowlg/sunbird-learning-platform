package org.sunbird.assessment.enums;

import org.apache.commons.lang3.StringUtils;

public enum AssessmentItemType {
    mcq, mmcq, sort_list, ftb, mtf, speech_question, canvas_question, recognition, vsa, sa, la, reference;

    public static boolean isValidAssessmentType(String str) {
        AssessmentItemType val = null;
        try {
            AssessmentItemType[] types = AssessmentItemType.values();
            for (AssessmentItemType type : types) {
                if (StringUtils.equals(type.name(), str))
                    val = type;
            }
        } catch (Exception e) {
        }
        if (null == val)
            return false;
        return true;
    }

    public static AssessmentItemType getAssessmentType(String str) {
        return AssessmentItemType.valueOf(str);
    }

}
