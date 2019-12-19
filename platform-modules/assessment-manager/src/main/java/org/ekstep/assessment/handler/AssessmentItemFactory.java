package org.ekstep.assessment.handler;

import org.ekstep.assessment.handler.impl.MCQHandler;

public class AssessmentItemFactory {


    public static IAssessmentHandler getHandler(String questionType) {
        IAssessmentHandler manager = null;
        switch (questionType) {
            case "mcq": manager = MCQHandler.getInstance();
                break;
            case "sa":
                break;
            case "vsa":
                break;
            case "la":
                break;
            default: break;
        }
        return manager;
    }
}
