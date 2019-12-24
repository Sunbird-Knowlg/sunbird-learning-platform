package org.ekstep.itemset.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ekstep.assessment.enums.AssessmentAPIParams;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.RequestParams;
import org.ekstep.common.mgr.ConvertToGraphNode;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.graph.model.node.MetadataDefinition;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
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
