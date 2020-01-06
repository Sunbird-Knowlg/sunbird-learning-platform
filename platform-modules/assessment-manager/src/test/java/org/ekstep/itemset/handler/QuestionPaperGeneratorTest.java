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
