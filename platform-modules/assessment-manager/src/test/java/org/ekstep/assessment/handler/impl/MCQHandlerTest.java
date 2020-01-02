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
        handler = AssessmentItemFactory.getHandler("mcq");
        mapper = new ObjectMapper();
    }

    @AfterClass
    public static void destroy() {
        handler = null;
        mapper = null;
    }

   @Test
    public void populateQuestions() throws Exception {
        String questionMap = handler.populateQuestion(HandlerImplData.mcqBodyString);
        Assert.assertNotNull(questionMap);
        Assert.assertTrue(StringUtils.isNoneBlank(questionMap));
    }

    /*@Test
    public void populateQuestionsEmptyResponse() throws Exception {
        Map<String, Object> bodyMap = mapper.readValue(HandlerImplData.mcqQuestionNotPresentBodyString, new TypeReference<Map<String, Object>>() {});
        Map<String, Object> questionMap = handler.populateQuestion(bodyMap);
        Assert.assertNotNull(questionMap);
        Assert.assertTrue(MapUtils.isEmpty(questionMap));
    }

    @Test
    public void populateQuestionsDataNotPresent() throws Exception {
        Map<String, Object> bodyMap = mapper.readValue(HandlerImplData.mcqDataNotPresentBodyString, new TypeReference<Map<String, Object>>() {});
        Map<String, Object> questionMap = handler.populateQuestion(bodyMap);
        Assert.assertNotNull(questionMap);
        Assert.assertTrue(MapUtils.isEmpty(questionMap));
    }

    @Test
    public void populateAnswer() throws Exception {
        Map<String, Object> bodyMap = mapper.readValue(HandlerImplData.mcqBodyString, new TypeReference<Map<String, Object>>() {});
        Map<String, Object> answerMap = handler.populateAnswer(bodyMap);
        Assert.assertNotNull(answerMap);
        Assert.assertTrue(MapUtils.isNotEmpty(answerMap));
    }

    @Test
    public void populateAnswerEmptyResponse() throws Exception {
        Map<String, Object> bodyMap = mapper.readValue(HandlerImplData.mcqAnswerNotPresentBodyString, new TypeReference<Map<String, Object>>() {});
        Map<String, Object> answerMap = handler.populateAnswer(bodyMap);
        Assert.assertNotNull(answerMap);
        Assert.assertTrue(MapUtils.isEmpty(answerMap));
    }

    @Test
    public void populateAnswerDataNotPresent() throws Exception {
        Map<String, Object> bodyMap = mapper.readValue(HandlerImplData.mcqDataNotPresentBodyString, new TypeReference<Map<String, Object>>() {});
        Map<String, Object> answerMap = handler.populateAnswer(bodyMap);
        Assert.assertNotNull(answerMap);
        Assert.assertTrue(MapUtils.isEmpty(answerMap));
    }*/

    @Test
    public void getInstanceTest() {
        IAssessmentHandler handler = MCQHandler.getInstance();
        Assert.assertTrue(handler instanceof MCQHandler);
    }

}

