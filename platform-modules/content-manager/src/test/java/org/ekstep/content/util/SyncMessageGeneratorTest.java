package org.ekstep.content.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.MapUtils;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.engine.common.GraphEngineTestSetup;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({ "javax.management.*", "sun.security.ssl.*", "javax.net.ssl.*", "javax.crypto.*" })
public class SyncMessageGeneratorTest extends GraphEngineTestSetup {

    ObjectMapper mapper = new ObjectMapper();

    @BeforeClass
    public static void create() throws Exception {
        loadDefinition("definitions/content_definition.json");
    }

    @Test
    public void TestGetMessages() throws Exception {
        ArrayList unitEvents = new ArrayList<Map<String, Object>>();
        Map<String, Object> message = SyncMessageGenerator.getMessages(getNodes(), "Content", getRelationMap(), new HashMap<>(), unitEvents);
        Assert.assertTrue(MapUtils.isNotEmpty(message));
        Assert.assertEquals("ANONYMOUS", ((HashMap<String, Object>) unitEvents.get(0)).get("userId"));
        Assert.assertEquals("Test_CourseUnit_1", ((HashMap<String, Object>) unitEvents.get(0)).get("label"));
    }

    private List<Node> getNodes() throws Exception {
        String nodeString = "{\"id\":0,\"identifier\":\"do_113053289509576704120\",\"objectType\":\"Content\",\"metadata\":{\"ownershipType\":[\"createdBy\"],\"parent\":\"do_113053289443917824115\",\"code\":\"b9a50833-eff6-4ef5-a2a4-2413f2d51f6c\",\"channel\":\"in.ekstep\",\"downloadUrl\":\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/ecar_files/do_113053289443917824115/marigold_1593511439342_do_113053289443917824115_10.0_spine.ecar\",\"description\":\"Test_CourseUnit_desc_1\",\"language\":[\"English\"],\"mimeType\":\"application/vnd.ekstep.content-collection\",\"variants\":\"{\\\"online\\\":{\\\"ecarUrl\\\":\\\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/ecar_files/do_113053289443917824115/marigold_1593511444257_do_113053289443917824115_10.0_online.ecar\\\",\\\"size\\\":2795.0},\\\"spine\\\":{\\\"ecarUrl\\\":\\\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/ecar_files/do_113053289443917824115/marigold_1593511439342_do_113053289443917824115_10.0_spine.ecar\\\",\\\"size\\\":2769.0}}\",\"idealScreenSize\":\"normal\",\"leafNodes\":[],\"createdOn\":\"2020-06-29T13:55:29.587+0530\",\"children\":[\"do_113053289509543936118\",\"do_113053289509511168116\"],\"contentDisposition\":\"inline\",\"lastUpdatedOn\":\"2020-06-29T13:55:29.796+0530\",\"contentEncoding\":\"gzip\",\"contentType\":\"TextBookUnit\",\"dialcodeRequired\":\"No\",\"lastStatusChangedOn\":\"2020-06-29T13:55:29.587+0530\",\"audience\":[\"Learner\"],\"os\":[\"All\"],\"visibility\":\"Parent\",\"index\":1,\"mediaType\":\"content\",\"osId\":\"org.ekstep.launcher\",\"languageCode\":[\"en\"],\"pkgVersion\":10.0,\"versionKey\":\"1593419129587\",\"license\":\"CC BY 4.0\",\"idealScreenDensity\":\"hdpi\",\"depth\":1,\"lastPublishedOn\":\"2020-06-30T15:33:37.208+0530\",\"compatibilityLevel\":1,\"leafNodesCount\":0,\"name\":\"Test_CourseUnit_1\",\"status\":\"Live\"},\"outRelations\":null,\"inRelations\":null,\"tags\":null}";
        Node node = mapper.readValue(nodeString, Node.class);
        return new ArrayList<Node>(){{
            add(node);
        }};
    }

    private Map<String, String> getRelationMap() throws Exception {
        String relationString = "{\"IN_ContentImage_hasSequenceMember\":\"collections\",\"OUT_Concept_associatedTo\":\"concepts\",\"OUT_Content_associatedTo\":\"usesContent\",\"OUT_Content_hasSequenceMember\":\"children\",\"OUT_Library_preRequisite\":\"libraries\",\"OUT_ContentImage_hasSequenceMember\":\"children\",\"IN_Content_associatedTo\":\"usedByContent\",\"OUT_ItemSet_associatedTo\":\"item_sets\",\"OUT_Method_associatedTo\":\"methods\",\"OUT_AssessmentItem_associatedTo\":\"questions\",\"IN_Content_hasSequenceMember\":\"collections\"}";
        Map<String, String> relationMap = mapper.readValue(relationString, Map.class);
        return relationMap;
    }
}
