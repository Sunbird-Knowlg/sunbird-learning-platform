package org.ekstep.assessment.handler.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.assessment.handler.AssessmentItemFactory;
import org.ekstep.assessment.handler.IAssessmentHandler;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

public class DefaultHandlerTest {
    private static IAssessmentHandler handler;
    private static ObjectMapper mapper;

    @BeforeClass
    public static void create() {
        mapper = new ObjectMapper();
    }

    @AfterClass
    public static void destroy() {
        handler = null;
    }

    @Test
    public void populateVSAQuestion() throws Exception {
        handler = AssessmentItemFactory.getHandler("reference");
        String questionString = handler.populateQuestion(HandlerImplData.vsaBodyString);
        Assert.assertNotNull(questionString);
        Assert.assertTrue(StringUtils.isNoneBlank(questionString));
    }

    @Test
    public void populateSAQuestion() throws Exception {
        handler = AssessmentItemFactory.getHandler("reference");
        String questionString = handler.populateQuestion(HandlerImplData.saBodyString);
        Assert.assertNotNull(questionString);
        Assert.assertTrue(StringUtils.isNoneBlank(questionString));
    }

    @Test
    public void populateLAQuestion() throws Exception {
        handler = AssessmentItemFactory.getHandler("reference");
        String questionString = handler.populateQuestion(HandlerImplData.laBodyString);
        Assert.assertNotNull(questionString);
        Assert.assertTrue(StringUtils.isNoneBlank(questionString));
    }

    @Test
    public void populateVSAAnswer() throws Exception {
        handler = AssessmentItemFactory.getHandler("reference");
        String passedAnswerString = "Answer";
        String answerString = handler.populateAnswer(passedAnswerString);
        Assert.assertNotNull(answerString);
        Assert.assertTrue(StringUtils.isNoneBlank(answerString));
        Assert.assertTrue(StringUtils.equals(passedAnswerString, answerString));
    }

    @Test
    public void populateSAAnswer() throws Exception {
        handler = AssessmentItemFactory.getHandler("reference");
        String answerString = handler.populateAnswer(null);
        Assert.assertNull(answerString);
    }
}
