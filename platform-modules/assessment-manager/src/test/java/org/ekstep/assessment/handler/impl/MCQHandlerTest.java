package org.ekstep.assessment.handler.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.ekstep.assessment.handler.AssessmentItemFactory;
import org.ekstep.assessment.handler.IAssessmentHandler;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Map;


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
        Map<String, Object> bodyMap = mapper.readValue(HandlerImplData.mcqBodyString, new TypeReference<Map<String, Object>>() {});
        Map<String, Object> questionMap = handler.populateQuestion(bodyMap);
        Assert.assertNotNull(questionMap);
        Assert.assertTrue(MapUtils.isNotEmpty(questionMap));
    }

    @Test
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
    }

    @Test
    public void populateOptions() throws Exception {
        Map<String, Object> bodyMap = mapper.readValue(HandlerImplData.mcqBodyString, new TypeReference<Map<String, Object>>() {});
        List<Map<String, Object>> optionMap = handler.populateOptions(bodyMap);
        Assert.assertNotNull(optionMap);
        Assert.assertTrue(CollectionUtils.isNotEmpty(optionMap));
    }

    @Test
    public void populateOptionsEmptyResponse() throws Exception {
        Map<String, Object> bodyMap = mapper.readValue(HandlerImplData.mcqOptionNotPresentBodyString, new TypeReference<Map<String, Object>>() {});
        List<Map<String, Object>> optionMap = handler.populateOptions(bodyMap);
        Assert.assertNotNull(optionMap);
        Assert.assertTrue(CollectionUtils.isEmpty(optionMap));
    }

    @Test
    public void populateOptionDataNotPresent() throws Exception {
        Map<String, Object> bodyMap = mapper.readValue(HandlerImplData.mcqDataNotPresentBodyString, new TypeReference<Map<String, Object>>() {});
        List<Map<String, Object>> optionMap = handler.populateOptions(bodyMap);
        Assert.assertNotNull(optionMap);
        Assert.assertTrue(CollectionUtils.isEmpty(optionMap));
    }

    @Test
    public void getInstanceTest() {
        IAssessmentHandler handler = MCQHandler.getInstance();
        Assert.assertTrue(handler instanceof MCQHandler);
    }

}

