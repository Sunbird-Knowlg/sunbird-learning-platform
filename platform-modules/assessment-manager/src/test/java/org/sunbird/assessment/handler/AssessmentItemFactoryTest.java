package org.sunbird.assessment.handler;

import org.sunbird.assessment.handler.AssessmentItemFactory;
import org.sunbird.assessment.handler.IAssessmentHandler;
import org.sunbird.assessment.handler.impl.DefaultHandler;
import org.sunbird.assessment.handler.impl.MCQHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class AssessmentItemFactoryTest {

    @Test
    public void assessmentItemFactoryMcqTest() throws Exception {
        IAssessmentHandler handler = AssessmentItemFactory.getHandler("mcq");
        Assert.assertNotNull(handler);
        Assert.assertTrue(handler instanceof MCQHandler);
    }

    @Test
    public void assessmentItemFactorySATest() {
        IAssessmentHandler handler = AssessmentItemFactory.getHandler("reference");
        Assert.assertNotNull(handler);
        Assert.assertTrue(handler instanceof DefaultHandler);
    }

    @Test
    public void assessmentItemFactoryVSATest() {
        IAssessmentHandler handler = AssessmentItemFactory.getHandler("reference");
        Assert.assertNotNull(handler);
        Assert.assertTrue(handler instanceof DefaultHandler);     }

    @Test
    public void assessmentItemFactoryLATest() {
        IAssessmentHandler handler = AssessmentItemFactory.getHandler("reference");
        Assert.assertNotNull(handler);
        Assert.assertTrue(handler instanceof DefaultHandler);     }

    @Test
    public void assessmentItemFactoryDefaultTest() {
        IAssessmentHandler handler = AssessmentItemFactory.getHandler("MCQ");
        Assert.assertNull(handler);
    }

}

