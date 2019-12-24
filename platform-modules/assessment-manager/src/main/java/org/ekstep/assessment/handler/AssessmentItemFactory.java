package org.ekstep.assessment.handler;

import org.ekstep.assessment.handler.impl.MCQHandler;

public class AssessmentItemFactory {


    public static IAssessmentHandler getHandler(String questionType) {
        IAssessmentHandler manager = null;
        switch (questionType) {
            case "mcq": manager = MCQHandler.getInstance();
                break;
            case "sa":
                System.out.println("Implementation for sa");
                break;
            case "vsa":
                System.out.println("Implementation for vsa");
                break;
            case "la":
                System.out.println("Implementation for la");
                break;
            default: break;
        }
        return manager;
    }
}
