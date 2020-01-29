package org.ekstep.assessment.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.assessment.handler.AssessmentItemFactory;
import org.ekstep.assessment.handler.IAssessmentHandler;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class MCQHandlerTest {
    private static IAssessmentHandler handler;
    private static ObjectMapper mapper;

    @BeforeClass
    public static void create() {
        mapper = new ObjectMapper();
        handler = AssessmentItemFactory.getHandler("mcq");
    }

    @AfterClass
    public static void destroy() {
        handler = null;
        mapper = null;
    }

   @Test
    public void populateQuestion() throws Exception {
        String questionString = handler.populateQuestion(HandlerImplData.mcqBodyString);
        Assert.assertNotNull(questionString);
        Assert.assertTrue(StringUtils.isNoneBlank(questionString));
    }

    @Test
    public void populateAnswer() throws Exception {
        String passedAnswerString = "Answer";
        String answerString = handler.populateAnswer(passedAnswerString);
        Assert.assertNotNull(answerString);
        Assert.assertTrue(StringUtils.isNoneBlank(answerString));
        Assert.assertTrue(StringUtils.equals(passedAnswerString, answerString));
    }

    @Test
    public void populateMalformedAnswer() throws Exception {
        String answerString = handler.populateAnswer(null);
        Assert.assertNull(answerString);
        Assert.assertTrue(StringUtils.isBlank(answerString));
    }

    @Test
    public void getInstanceTest() {
        IAssessmentHandler handler = MCQHandler.getInstance();
        Assert.assertTrue(handler instanceof MCQHandler);
    }

}

