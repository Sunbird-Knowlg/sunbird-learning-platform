package org.ekstep.assessment.handler;

import org.ekstep.assessment.handler.impl.DefaultHandler;
import org.ekstep.assessment.handler.impl.MCQHandler;

public class AssessmentItemFactory {


    public static IAssessmentHandler getHandler(String questionType) {
        IAssessmentHandler manager = null;
        switch (questionType) {
            case "mcq": manager = MCQHandler.getInstance();
                break;
            case "reference": manager = DefaultHandler.getInstance();
                break;
            default: break;
        }
        return manager;
    }
}
