package org.ekstep.itemset.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ekstep.assessment.enums.AssessmentAPIParams;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.RequestParams;
import org.ekstep.common.mgr.ConvertToGraphNode;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.dac.model.Relation;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.graph.model.node.MetadataDefinition;
import org.ekstep.learning.util.ControllerUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

public class QuestionPaperGeneratorTest {

    ObjectMapper mapper = new ObjectMapper();

    @BeforeClass
    public static void create() {

    }

    @AfterClass
    public static void destroy() {

    }

    @Test
    public void testGetFileName() {
        String fileName = QuestionPaperGenerator.getFileName("html_");
        Assert.assertNotNull(fileName);
    }

    @Ignore
    @Test // Test this with dev connection
    public void testGenerateQuestionPaper() throws Exception {
        String itemSet = "{\"copyright\":\"\",\"code\":\"test\",\"previewUrl\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/%22itemset%22/do_112928802520481792112/do_112928802520481792112_html_1578293964638.html\",\"purpose\":\"Knowledge\",\"description\":\"\",\"language\":[\"English\"],\"title\":\"Question Set - 1\",\"type\":\"materialised\",\"createdOn\":\"2020-01-05T11:16:03.972+0000\",\"gradeLevel\":[\"Other\"],\"lastUpdatedOn\":\"2020-01-06T06:59:24.845+0000\",\"used_for\":\"assessment\",\"owner\":\"\",\"difficulty_level\":\"low\",\"identifier\":\"do_112928802520481792112\",\"lastStatusChangedOn\":\"2020-01-06T05:49:41.642+0000\",\"languageCode\":[],\"depth_of_knowledge\":\"Recall\",\"version\":2,\"framework\":\"NCF\",\"sub_purpose\":\"\",\"createdBy\":\"\",\"max_score\":3,\"name\":\"Q1\",\"items\":[{\"identifier\":\"test.mcq_test_107\",\"name\":\"Mmcq Question 2\",\"description\":null,\"objectType\":\"AssessmentItem\",\"relation\":\"hasSequenceMember\",\"status\":\"Live\"},{\"identifier\":\"test.mcq_test_108\",\"name\":\"Mmcq Question 2\",\"description\":null,\"objectType\":\"AssessmentItem\",\"relation\":\"hasSequenceMember\",\"status\":\"Live\"},{\"identifier\":\"test.mcq_test_105\",\"name\":\"Mmcq Question 2\",\"description\":null,\"objectType\":\"AssessmentItem\",\"relation\":\"hasSequenceMember\",\"status\":\"Live\"},{\"identifier\":\"test.mcq_test_104\",\"name\":\"Mmcq Question 2\",\"description\":null,\"objectType\":\"AssessmentItem\",\"relation\":\"hasSequenceMember\",\"status\":\"Live\"},{\"identifier\":\"test.mcq_test_106\",\"name\":\"Mmcq Question 2\",\"description\":null,\"objectType\":\"AssessmentItem\",\"relation\":\"hasSequenceMember\",\"status\":\"Live\"},{\"identifier\":\"test.mcq_test_102\",\"name\":\"Mmcq Question 2\",\"description\":null,\"objectType\":\"AssessmentItem\",\"relation\":\"hasSequenceMember\",\"status\":\"Live\"},{\"identifier\":\"test.mcq_test_101\",\"name\":\"Mmcq Question 2\",\"description\":null,\"objectType\":\"AssessmentItem\",\"relation\":\"hasSequenceMember\",\"status\":\"Live\"},{\"identifier\":\"test.mcq_test_103\",\"name\":\"Mmcq Question 2\",\"description\":null,\"objectType\":\"AssessmentItem\",\"relation\":\"hasSequenceMember\",\"status\":\"Live\"}],\"status\":\"Live\"}";
        Map<String, Object> nodeMap = mapper.readValue(itemSet, Map.class);
        Node node = ConvertToGraphNode.convertToGraphNode(nodeMap, new ControllerUtil().getDefinition("domain", "ItemSet"), null);
        long count=1;
        for (Relation rel: node.getOutRelations()) {
            rel.setEndNodeObjectType("AssessmentItem");
            rel.setStartNodeObjectType("ItemSet");
            long finalCount = count;
            rel.setMetadata(new HashMap<String, Object>() {{
                put("IL_SEQUENCE_INDEX", finalCount);
            }});
            count +=1;
        }
        File file = QuestionPaperGenerator.generateQuestionPaper(node);
        Assert.assertNotNull(file);
        System.out.println("File path : " + file.getAbsolutePath());
    }

    @Test
    public void testGenerateHtmlString() throws Exception {
        Method method = QuestionPaperGenerator.class.getDeclaredMethod("generateHtmlString", File.class, Map.class, Node.class);
        method.setAccessible(true);
        String nodeString = "{\"id\":0,\"graphId\":null,\"identifier\":\"do_112928802520481792112\",\"nodeType\":null,\"objectType\":null,\"metadata\":{\"owner\":\"\",\"difficulty_level\":\"low\",\"copyright\":\"\",\"lastStatusChangedOn\":\"2020-01-06T05:49:41.642+0000\",\"code\":\"test\",\"previewUrl\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/%22itemset%22/do_112928802520481792112/do_112928802520481792112_html_1578293964638.html\",\"purpose\":\"Knowledge\",\"description\":\"\",\"language\":[\"English\"],\"title\":\"Question Set - 1\",\"type\":\"materialised\",\"languageCode\":[],\"createdOn\":\"2020-01-05T11:16:03.972+0000\",\"depth_of_knowledge\":\"Recall\",\"version\":2,\"gradeLevel\":[\"Other\"],\"framework\":\"NCF\",\"sub_purpose\":\"\",\"createdBy\":\"\",\"max_score\":3,\"name\":\"Q1\",\"lastUpdatedOn\":\"2020-01-06T06:59:24.845+0000\",\"used_for\":\"assessment\",\"status\":\"Live\"},\"outRelations\":[{\"id\":0,\"graphId\":null,\"relationType\":\"hasSequenceMember\",\"startNodeId\":null,\"endNodeId\":\"test.mcq_test_107\",\"startNodeName\":null,\"endNodeName\":null,\"startNodeType\":null,\"endNodeType\":null,\"startNodeObjectType\":\"ItemSet\",\"endNodeObjectType\":\"AssessmentItem\",\"metadata\":{\"IL_SEQUENCE_INDEX\":1},\"startNodeMetadata\":null,\"endNodeMetadata\":null},{\"id\":0,\"graphId\":null,\"relationType\":\"hasSequenceMember\",\"startNodeId\":null,\"endNodeId\":\"test.mcq_test_108\",\"startNodeName\":null,\"endNodeName\":null,\"startNodeType\":null,\"endNodeType\":null,\"startNodeObjectType\":\"ItemSet\",\"endNodeObjectType\":\"AssessmentItem\",\"metadata\":{\"IL_SEQUENCE_INDEX\":2},\"startNodeMetadata\":null,\"endNodeMetadata\":null},{\"id\":0,\"graphId\":null,\"relationType\":\"hasSequenceMember\",\"startNodeId\":null,\"endNodeId\":\"test.mcq_test_105\",\"startNodeName\":null,\"endNodeName\":null,\"startNodeType\":null,\"endNodeType\":null,\"startNodeObjectType\":\"ItemSet\",\"endNodeObjectType\":\"AssessmentItem\",\"metadata\":{\"IL_SEQUENCE_INDEX\":3},\"startNodeMetadata\":null,\"endNodeMetadata\":null},{\"id\":0,\"graphId\":null,\"relationType\":\"hasSequenceMember\",\"startNodeId\":null,\"endNodeId\":\"test.mcq_test_104\",\"startNodeName\":null,\"endNodeName\":null,\"startNodeType\":null,\"endNodeType\":null,\"startNodeObjectType\":\"ItemSet\",\"endNodeObjectType\":\"AssessmentItem\",\"metadata\":{\"IL_SEQUENCE_INDEX\":4},\"startNodeMetadata\":null,\"endNodeMetadata\":null},{\"id\":0,\"graphId\":null,\"relationType\":\"hasSequenceMember\",\"startNodeId\":null,\"endNodeId\":\"test.mcq_test_106\",\"startNodeName\":null,\"endNodeName\":null,\"startNodeType\":null,\"endNodeType\":null,\"startNodeObjectType\":\"ItemSet\",\"endNodeObjectType\":\"AssessmentItem\",\"metadata\":{\"IL_SEQUENCE_INDEX\":5},\"startNodeMetadata\":null,\"endNodeMetadata\":null},{\"id\":0,\"graphId\":null,\"relationType\":\"hasSequenceMember\",\"startNodeId\":null,\"endNodeId\":\"test.mcq_test_102\",\"startNodeName\":null,\"endNodeName\":null,\"startNodeType\":null,\"endNodeType\":null,\"startNodeObjectType\":\"ItemSet\",\"endNodeObjectType\":\"AssessmentItem\",\"metadata\":{\"IL_SEQUENCE_INDEX\":6},\"startNodeMetadata\":null,\"endNodeMetadata\":null},{\"id\":0,\"graphId\":null,\"relationType\":\"hasSequenceMember\",\"startNodeId\":null,\"endNodeId\":\"test.mcq_test_101\",\"startNodeName\":null,\"endNodeName\":null,\"startNodeType\":null,\"endNodeType\":null,\"startNodeObjectType\":\"ItemSet\",\"endNodeObjectType\":\"AssessmentItem\",\"metadata\":{\"IL_SEQUENCE_INDEX\":7},\"startNodeMetadata\":null,\"endNodeMetadata\":null},{\"id\":0,\"graphId\":null,\"relationType\":\"hasSequenceMember\",\"startNodeId\":null,\"endNodeId\":\"test.mcq_test_103\",\"startNodeName\":null,\"endNodeName\":null,\"startNodeType\":null,\"endNodeType\":null,\"startNodeObjectType\":\"ItemSet\",\"endNodeObjectType\":\"AssessmentItem\",\"metadata\":{\"IL_SEQUENCE_INDEX\":8},\"startNodeMetadata\":null,\"endNodeMetadata\":null}],\"inRelations\":null,\"tags\":null}";
        String assessmentMapString = "{\"test.mcq_test_107\":{\"question\":\"<div class='mcq-vertical cheveron-helper'><div class='mcq-title'><p>The Rowlatt Act was passed in : </p></div><i class='chevron down icon'></i><div class='mcq-options'><div data-simple-choice-interaction data-response-variable='responseValue' value=0 class='mcq-option'><p>1905</p></div><div data-simple-choice-interaction data-response-variable='responseValue' value=1 class='mcq-option'><p>1915</p></div><div data-simple-choice-interaction data-response-variable='responseValue' value=2 class='mcq-option'><p>1925</p></div><div data-simple-choice-interaction data-response-variable='responseValue' value=3 class='mcq-option'><p>1935</p></div></div></div>\",\"answer\":\"1\",\"index\":1},\"test.mcq_test_108\":{\"question\":\"<div class='mcq-vertical cheveron-helper'><div class='mcq-title'><p>Who is father of Nation?</p></div><i class='chevron down icon'></i><div class='mcq-options'><div data-simple-choice-interaction data-response-variable='responseValue' value=0 class='mcq-option'><p>Jawahar Lal Nehru</p></div><div data-simple-choice-interaction data-response-variable='responseValue' value=1 class='mcq-option'><p>Sardar Vallabh Bhai Patel</p></div><div data-simple-choice-interaction data-response-variable='responseValue' value=2 class='mcq-option'><p>Mahatma Gandhi</p></div><div data-simple-choice-interaction data-response-variable='responseValue' value=3 class='mcq-option'><p>Bhagat Singh</p></div></div></div>\",\"answer\":\"2\",\"index\":2},\"test.mcq_test_105\":{\"question\":\"<div class='mcq-vertical cheveron-helper'><div class='mcq-title'><p>The revolutionary like Ashfaqullah Khan, Chandra Shekhar  Azad, Ram Prasad Bismil, Roshan Singh and Rajendra Lahiri were all associated with : </p></div><i class='chevron down icon'></i><div class='mcq-options'><div data-simple-choice-interaction data-response-variable='responseValue' value=0 class='mcq-option'><p>The Kakori Conspircy case (1925)</p></div><div data-simple-choice-interaction data-response-variable='responseValue' value=1 class='mcq-option'><p>1857 Revolt</p></div><div data-simple-choice-interaction data-response-variable='responseValue' value=2 class='mcq-option'><p>Chauri Chaura Case</p></div><div data-simple-choice-interaction data-response-variable='responseValue' value=3 class='mcq-option'><p>The Jallianwala Bagh massacre</p></div></div></div>\",\"answer\":\"2\",\"index\":3},\"test.mcq_test_104\":{\"question\":\"<div class='mcq-vertical cheveron-helper'><div class='mcq-title'><p>Who is the founder of Haryanka Dynasty?</p></div><i class='chevron down icon'></i><div class='mcq-options'><div data-simple-choice-interaction data-response-variable='responseValue' value=0 class='mcq-option'><p>Ajatshatru</p></div><div data-simple-choice-interaction data-response-variable='responseValue' value=1 class='mcq-option'><p>Harshvardhan</p></div><div data-simple-choice-interaction data-response-variable='responseValue' value=2 class='mcq-option'><p>Bimbisar</p></div><div data-simple-choice-interaction data-response-variable='responseValue' value=3 class='mcq-option'><p>Ghananand</p></div></div></div>\",\"answer\":\"3\",\"index\":4},\"test.mcq_test_106\":{\"question\":\"<div class='mcq-vertical cheveron-helper'><div class='mcq-title'><p>The Moplah Rebellion in 1921 in Malabar was Muslim Peasants Rabellion against :</p></div><i class='chevron down icon'></i><div class='mcq-options'><div data-simple-choice-interaction data-response-variable='responseValue' value=0 class='mcq-option'><p>Muslim Land Holders</p></div><div data-simple-choice-interaction data-response-variable='responseValue' value=1 class='mcq-option'><p>The British Government Authority</p></div><div data-simple-choice-interaction data-response-variable='responseValue' value=2 class='mcq-option'><p>The non-tribal outsiders</p></div><div data-simple-choice-interaction data-response-variable='responseValue' value=3 class='mcq-option'><p>The non-tribal outsiders</p></div></div></div>\",\"answer\":\"2\",\"index\":5},\"test.mcq_test_102\":{\"question\":\"<div class='mcq-vertical cheveron-helper'><div class='mcq-title'><p>What is the capital of India?</p></div><i class='chevron down icon'></i><div class='mcq-options'><div data-simple-choice-interaction data-response-variable='responseValue' value=0 class='mcq-option'><p>Bangalore</p></div><div data-simple-choice-interaction data-response-variable='responseValue' value=1 class='mcq-option'><p>Hydrabad</p></div><div data-simple-choice-interaction data-response-variable='responseValue' value=2 class='mcq-option'><p>New Delhi</p></div><div data-simple-choice-interaction data-response-variable='responseValue' value=3 class='mcq-option'><p>Chennai</p></div></div></div>\",\"answer\":\"2\",\"index\":6},\"test.mcq_test_101\":{\"question\":\"<div class='mcq-vertical cheveron-helper'><div class='mcq-title'><p>Which of the following sets of tests would be LEAST urgently indicated for a patient presenting with intraocular pressures of R 22mmHg and L 46mmHg?</p></div><i class='chevron down icon'></i><div class='mcq-options'><div data-simple-choice-interaction data-response-variable='responseValue' value=0 class='mcq-option'><p>Visual field examination and dilated optic nerve head assessment</p></div><div data-simple-choice-interaction data-response-variable='responseValue' value=1 class='mcq-option'><p>Examination of the irides and anterior chamber assessment for cells and flare</p></div><div data-simple-choice-interaction data-response-variable='responseValue' value=2 class='mcq-option'><p>Gonioscopy and assessment of the lens capsule</p></div><div data-simple-choice-interaction data-response-variable='responseValue' value=3 class='mcq-option'><p>Assessment of the corneal endothelium and iris transillumination</p></div></div></div>\",\"answer\":\"1\",\"index\":7},\"test.mcq_test_103\":{\"question\":\"<div class='mcq-vertical cheveron-helper'><div class='mcq-title'><p>What is the capital of USA?</p></div><i class='chevron down icon'></i><div class='mcq-options'><div data-simple-choice-interaction data-response-variable='responseValue' value=0 class='mcq-option'><p>Washington DC</p></div><div data-simple-choice-interaction data-response-variable='responseValue' value=1 class='mcq-option'><p>Brasilia</p></div><div data-simple-choice-interaction data-response-variable='responseValue' value=2 class='mcq-option'><p>Maxico City</p></div><div data-simple-choice-interaction data-response-variable='responseValue' value=3 class='mcq-option'><p>New Delhi</p></div></div></div>\",\"answer\":\"0\",\"index\":8}}";
        Node node = mapper.readValue(nodeString, Node.class);
        Map<String, Object> assessmentMap = mapper.readValue(assessmentMapString, Map.class);
        String htmlString = (String)method.invoke(QuestionPaperGenerator.class, null, assessmentMap, node);
        Assert.assertNotNull(htmlString);
    }
//    @Test
//    public void testGetFileName() {
//        String fileName = QuestionPaperGenerator.getFileName("html_");
//        Assert.assertNotNull(fileName);
//    }
//
//    @Test
//    public void testGetFileName() {
//        String fileName = QuestionPaperGenerator.getFileName("html_");
//        Assert.assertNotNull(fileName);
//    }
//    @Test
//    public void testGetFileName() {
//        String fileName = QuestionPaperGenerator.getFileName("html_");
//        Assert.assertNotNull(fileName);
//    }

}
