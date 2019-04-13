package org.ekstep.taxonomy.controller;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ekstep.cassandra.connector.util.CassandraConnector;
import org.ekstep.common.dto.Response;
import org.ekstep.graph.engine.common.TestParams;
import org.ekstep.graph.model.cache.CategoryCache;
import org.ekstep.learning.router.LearningRequestRouterPool;
import org.ekstep.test.common.CommonTestSetup;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Kumar Gauraw
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class CollectionV3ControllerTest extends CommonTestSetup {

    /** The context. */
    @Autowired
    private WebApplicationContext context;

    /** The actions. */
    private ResultActions actions;

    private MockMvc mockMvc;

    private static final String BASE_PATH = "/collection/v3";
    private static final String CONTENT_BASE_PATH = "/content/v3";
    private static ObjectMapper mapper = new ObjectMapper();

    private static final String[] VALID_DIAL_CODES = { "ABC123", "BCD123", "CDE123", "DEF123", "EFG123" };
    private static final String SCRIPT_1 = "CREATE KEYSPACE IF NOT EXISTS content_store_test WITH replication = {'class': 'SimpleStrategy','replication_factor': '1'};";
    private static final String SCRIPT_2 = "CREATE TABLE IF NOT EXISTS content_store_test.content_data (content_id text, last_updated_on timestamp,body blob,oldBody blob,screenshots blob,stageIcons blob,PRIMARY KEY (content_id));";
    private static final String SCRIPT_3 = "CREATE KEYSPACE IF NOT EXISTS hierarchy_store_test WITH replication = {'class': 'SimpleStrategy','replication_factor': '1'};";
    private static final String SCRIPT_4 = "CREATE TABLE IF NOT EXISTS hierarchy_store_test.content_hierarchy_test (identifier text, hierarchy text, PRIMARY KEY (identifier));";

    @BeforeClass
    public static void setup() throws Exception {
        loadDefinition("definitions/content_definition.json","definitions/content_image_definition.json", "definitions/concept_definition.json",
                "definitions/dimension_definition.json", "definitions/domain_definition.json");
        executeScript(SCRIPT_1, SCRIPT_2, SCRIPT_3, SCRIPT_4);
        LearningRequestRouterPool.init();
        createFramework();
    }

    @Before
    public void init() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
    }

    @AfterClass
    public static void clean() throws Exception {

    }

    /*
     * Framework Name: NCFTEST
     *
     * Framework Category: medium, subject, board, gradeLevel, topic
     *
     * Framework Term: english, math, cbse, class 1, addition
     *
     */
    public static void createFramework() throws Exception {
        String fwHierarchy = "{\"categories\":[{\"identifier\":\"ncftest_medium\",\"code\":\"medium\",\"terms\":[{\"identifier\":\"ncftest_medium_english\",\"code\":\"english\",\"name\":\"english\",\"description\":\"English Medium\",\"index\":1,\"category\":\"medium\",\"status\":\"Live\"}],\"name\":\"medium\",\"description\":\"Medium for NCFTEST\",\"index\":1,\"status\":\"Live\"},{\"identifier\":\"ncftest_subject\",\"code\":\"subject\",\"terms\":[{\"identifier\":\"ncftest_subject_math\",\"code\":\"math\",\"name\":\"math\",\"description\":\"Mathematics\",\"index\":1,\"category\":\"subject\",\"status\":\"Live\"}],\"name\":\"subject\",\"description\":\"Subject for NCFTEST\",\"index\":2,\"status\":\"Live\"},{\"identifier\":\"ncftest_board\",\"code\":\"board\",\"terms\":[{\"identifier\":\"ncftest_board_cbse\",\"code\":\"cbse\",\"name\":\"cbse\",\"description\":\"CBSE Board\",\"index\":1,\"category\":\"board\",\"status\":\"Live\"}],\"name\":\"board\",\"description\":\"Board for NCFTEST\",\"index\":3,\"status\":\"Live\"},{\"identifier\":\"ncftest_topic\",\"code\":\"topic\",\"terms\":[{\"identifier\":\"ncftest_topic_addition\",\"code\":\"addition\",\"name\":\"addition\",\"description\":\"Addition\",\"index\":1,\"category\":\"topic\",\"status\":\"Live\"}],\"name\":\"topic\",\"description\":\"Topics for NCFTEST\",\"index\":4,\"status\":\"Live\"},{\"identifier\":\"ncftest_gradelevel\",\"code\":\"gradeLevel\",\"terms\":[{\"identifier\":\"ncftest_gradelevel_class-1\",\"code\":\"class 1\",\"name\":\"class 1\",\"description\":\"Class 1\",\"index\":1,\"category\":\"gradeLevel\",\"status\":\"Live\"}],\"name\":\"gradeLevel\",\"description\":\"Grade Level for NCFTEST\",\"index\":5,\"status\":\"Live\"}]}";
        Map<String, Object> frameworkHierarchy = mapper.readValue(fwHierarchy,
                new TypeReference<Map<String, Object>>() {
                });
        CategoryCache.setFramework("NCFTEST", frameworkHierarchy);
    }

    @Test
    public void testDialLinkWithLiveCollection() throws Exception{
        String createCollReq = "{\"request\": {\"content\": {\"name\": \"Test-G-Dev-01\",\"code\": \"test.book.1\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBook\"}}}";
        String textbookId = createContent(createCollReq);
        String sysUpdateReq = "{\"request\":{\"content\":{\"status\":\"Live\"}}}";
        systemUpdate(textbookId, sysUpdateReq);
        updateCassandraHierarchy(textbookId, true);
        getCollectionHierarchy(textbookId);
        getContent(textbookId+".img");
        String dialLinkReq = "{\"request\":{\"content\":[{\"dialcode\":\"ABC123\",\"identifier\":\"do_11273962618141081612\"},{\"dialcode\":[\"BCD123\",\"CDE123\"],\"identifier\":[\"do_11273962618146816013\"]},{\"dialcode\":\"DEF123\",\"identifier\":[\"do_11273962618149273614\"]},{\"dialcode\":\"EFG123\",\"identifier\":[\"do_11273962618151731215\"]}]}}";
        linkDialCode(textbookId, dialLinkReq);
        getCassandraRecord(textbookId+".img");
        getCollectionHierarchy(textbookId);
        getContent(textbookId+".img");
    }


    private String createContent(String requestBody) throws Exception {
        String path = CONTENT_BASE_PATH + "/create";
        actions = mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi").header("X-Channel-Id", "channelTest")
                .header("user-id", "ilimi").contentType(MediaType.APPLICATION_JSON).content(requestBody));
        assertEquals(200, actions.andReturn().getResponse().getStatus());
        return (String) getResponse(actions).getResult().get(TestParams.node_id.name());
    }

    private Response getContent(String contentId) throws Exception {
        String path = CONTENT_BASE_PATH + "/read/" + contentId;
        actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON).header("X-Channel-Id", "channelKA"));
        System.out.println("Get Content for  "+contentId+" : "+actions.andReturn().getResponse().getContentAsString());
        return getResponse(actions);
    }

    private Response getCollectionHierarchy(String contentId) throws Exception {
        String path = CONTENT_BASE_PATH + "/hierarchy/" + contentId;
        actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON).header("X-Channel-Id", "channelKA"));
        System.out.println("Get Hierarchy for "+contentId+" : "+actions.andReturn().getResponse().getContentAsString());
        return getResponse(actions);
    }

    private void systemUpdate(String contentId, String requestBody) throws Exception {
        String path = "/system/v3/content/update/"+contentId;
        actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).header("user-id", "ilimi").header("X-Channel-Id", "channelTest")
                .header("user-id", "ilimi").contentType(MediaType.APPLICATION_JSON).content(requestBody));
        assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    private Response linkDialCode(String contentId, String requestBody) throws Exception {
        String path = BASE_PATH+"/dialcode/link/"+contentId;
        actions = mockMvc.perform(MockMvcRequestBuilders.post(path).header("X-Channel-Id", "channelTest").contentType(MediaType.APPLICATION_JSON).content(requestBody));
        System.out.println("DIAL LINK Response : "+ actions.andReturn().getResponse().getContentAsString());
        assertEquals(200, actions.andReturn().getResponse().getStatus());
        return getResponse(actions);
    }
    private void updateCassandraHierarchy(String contentId, boolean isLive){
        String liveHierarchy = "{\"ownershipType\":[\"createdBy\"],\"parent\":\"rootNodeIdentifier\",\"code\":\"test.book.1\",\"channel\":\"in.ekstep\",\"downloadUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/rootNodeIdentifier/test-g-dev-01_1555132095854_rootNodeIdentifier_2.0_spine.ecar\",\"language\":[\"English\"],\"variants\":{\"online\":{\"ecarUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/rootNodeIdentifier/test-g-dev-01_1555132096208_rootNodeIdentifier_2.0_online.ecar\",\"size\":1479.0},\"spine\":{\"ecarUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/rootNodeIdentifier/test-g-dev-01_1555132095854_rootNodeIdentifier_2.0_spine.ecar\",\"size\":21593.0}},\"mimeType\":\"application/vnd.ekstep.content-collection\",\"posterImage\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/content/do_112577747416195072185/artifact/edu-success_1535369558773.jpeg\",\"idealScreenSize\":\"normal\",\"createdOn\":\"2019-04-13T10:05:09.129+0530\",\"objectType\":\"Content\",\"appIcon\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/content/rootNodeIdentifier/artifact/edu-success_1535369558773.thumb.jpeg\",\"children\":[{\"ownershipType\":[\"createdBy\"],\"parent\":\"rootNodeIdentifier\",\"code\":\"TestBookUnit-01\",\"downloadUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273962618141081612/test_collection_textbookunit_01_1555132093281_do_11273962618141081612_2.0_spine.ecar\",\"description\":\"Test_Collection_TextBookUnit_01\",\"language\":[\"English\"],\"variants\":{\"online\":{\"ecarUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273962618141081612/test_collection_textbookunit_01_1555132093746_do_11273962618141081612_2.0_online.ecar\",\"size\":1087.0},\"spine\":{\"ecarUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273962618141081612/test_collection_textbookunit_01_1555132093281_do_11273962618141081612_2.0_spine.ecar\",\"size\":1087.0}},\"mimeType\":\"application/vnd.ekstep.content-collection\",\"idealScreenSize\":\"normal\",\"createdOn\":\"2019-04-13T10:05:49.100+0530\",\"objectType\":\"Content\",\"children\":[{\"ownershipType\":[\"createdBy\"],\"parent\":\"do_11273962618141081612\",\"code\":\"TestBookUnit-01-01\",\"downloadUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273962618146816013/test_collection_textbookunit_01_01_1555132092091_do_11273962618146816013_2.0_spine.ecar\",\"questions\":[],\"description\":\"TTest_Collection_TextBookUnit_01_01\",\"language\":[\"English\"],\"variants\":{\"online\":{\"ecarUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273962618146816013/test_collection_textbookunit_01_01_1555132092599_do_11273962618146816013_2.0_online.ecar\",\"size\":843.0},\"spine\":{\"ecarUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273962618146816013/test_collection_textbookunit_01_01_1555132092091_do_11273962618146816013_2.0_spine.ecar\",\"size\":844.0}},\"mimeType\":\"application/vnd.ekstep.content-collection\",\"idealScreenSize\":\"normal\",\"createdOn\":\"2019-04-13T10:05:49.105+0530\",\"objectType\":\"Content\",\"usesContent\":[],\"contentDisposition\":\"inline\",\"contentEncoding\":\"gzip\",\"lastUpdatedOn\":\"2019-04-13T10:05:49.105+0530\",\"sYS_INTERNAL_LAST_UPDATED_ON\":\"2019-04-13T10:38:13.246+0530\",\"dialcodeRequired\":\"No\",\"contentType\":\"TextBookUnit\",\"identifier\":\"do_11273962618146816013\",\"audience\":[\"Learner\"],\"lastStatusChangedOn\":\"2019-04-13T10:35:52.723+0530\",\"visibility\":\"Parent\",\"os\":[\"All\"],\"index\":1,\"mediaType\":\"content\",\"osId\":\"org.ekstep.launcher\",\"pkgVersion\":2.0,\"versionKey\":\"1555130149105\",\"idealScreenDensity\":\"hdpi\",\"depth\":2,\"s3Key\":\"ecar_files/do_11273962618146816013/test_collection_textbookunit_01_01_1555132092091_do_11273962618146816013_2.0_spine.ecar\",\"size\":844.0,\"lastPublishedOn\":\"2019-04-13T10:38:12.089+0530\",\"concepts\":[],\"compatibilityLevel\":1.0,\"name\":\"Test_Collection_TextBookUnit_01_01\",\"status\":\"Live\"}],\"contentDisposition\":\"inline\",\"contentEncoding\":\"gzip\",\"lastUpdatedOn\":\"2019-04-13T10:05:49.100+0530\",\"sYS_INTERNAL_LAST_UPDATED_ON\":\"2019-04-13T10:38:14.476+0530\",\"dialcodeRequired\":\"No\",\"contentType\":\"TextBookUnit\",\"identifier\":\"do_11273962618141081612\",\"audience\":[\"Learner\"],\"lastStatusChangedOn\":\"2019-04-13T10:35:55.736+0530\",\"visibility\":\"Parent\",\"os\":[\"All\"],\"index\":1,\"mediaType\":\"content\",\"osId\":\"org.ekstep.launcher\",\"pkgVersion\":2.0,\"versionKey\":\"1555130149100\",\"idealScreenDensity\":\"hdpi\",\"depth\":1,\"s3Key\":\"ecar_files/do_11273962618141081612/test_collection_textbookunit_01_1555132093281_do_11273962618141081612_2.0_spine.ecar\",\"size\":1087.0,\"lastPublishedOn\":\"2019-04-13T10:38:13.276+0530\",\"compatibilityLevel\":1.0,\"name\":\"Test_Collection_TextBookUnit_01\",\"status\":\"Live\"},{\"ownershipType\":[\"createdBy\"],\"parent\":\"rootNodeIdentifier\",\"code\":\"TestBookUnit-02\",\"downloadUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273962618149273614/test_collection_textbookunit_02_1555132094516_do_11273962618149273614_2.0_spine.ecar\",\"questions\":[],\"description\":\"TTest_Collection_TextBookUnit_02\",\"language\":[\"English\"],\"variants\":{\"online\":{\"ecarUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273962618149273614/test_collection_textbookunit_02_1555132095031_do_11273962618149273614_2.0_online.ecar\",\"size\":839.0},\"spine\":{\"ecarUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273962618149273614/test_collection_textbookunit_02_1555132094516_do_11273962618149273614_2.0_spine.ecar\",\"size\":837.0}},\"mimeType\":\"application/vnd.ekstep.content-collection\",\"idealScreenSize\":\"normal\",\"createdOn\":\"2019-04-13T10:05:49.108+0530\",\"objectType\":\"Content\",\"usesContent\":[],\"contentDisposition\":\"inline\",\"contentEncoding\":\"gzip\",\"lastUpdatedOn\":\"2019-04-13T10:05:49.108+0530\",\"sYS_INTERNAL_LAST_UPDATED_ON\":\"2019-04-13T10:38:15.664+0530\",\"dialcodeRequired\":\"No\",\"contentType\":\"TextBookUnit\",\"identifier\":\"do_11273962618149273614\",\"audience\":[\"Learner\"],\"lastStatusChangedOn\":\"2019-04-13T10:35:57.748+0530\",\"visibility\":\"Parent\",\"os\":[\"All\"],\"index\":2,\"mediaType\":\"content\",\"osId\":\"org.ekstep.launcher\",\"pkgVersion\":2.0,\"versionKey\":\"1555130149108\",\"idealScreenDensity\":\"hdpi\",\"depth\":1,\"s3Key\":\"ecar_files/do_11273962618149273614/test_collection_textbookunit_02_1555132094516_do_11273962618149273614_2.0_spine.ecar\",\"size\":837.0,\"lastPublishedOn\":\"2019-04-13T10:38:14.515+0530\",\"concepts\":[],\"compatibilityLevel\":1.0,\"name\":\"Test_Collection_TextBookUnit_02\",\"status\":\"Live\"}],\"contentDisposition\":\"inline\",\"contentEncoding\":\"gzip\",\"lastUpdatedOn\":\"2019-04-13T10:05:09.129+0530\",\"sYS_INTERNAL_LAST_UPDATED_ON\":\"2019-04-13T10:38:17.111+0530\",\"contentType\":\"TextBook\",\"dialcodeRequired\":\"No\",\"identifier\":\"rootNodeIdentifier\",\"audience\":[\"Learner\"],\"lastStatusChangedOn\":\"2019-04-13T10:36:01.487+0530\",\"visibility\":\"Default\",\"os\":[\"All\"],\"index\":null,\"mediaType\":\"content\",\"osId\":\"org.ekstep.quiz.app\",\"pkgVersion\":2.0,\"versionKey\":\"1555130149293\",\"idealScreenDensity\":\"hdpi\",\"depth\":0,\"s3Key\":\"ecar_files/rootNodeIdentifier/test-g-dev-01_1555132095854_rootNodeIdentifier_2.0_spine.ecar\",\"framework\":\"NCF\",\"size\":21593.0,\"lastPublishedOn\":\"2019-04-13T10:38:15.841+0530\",\"createdBy\":\"874ed8a5-782e-4f6c-8f36-e0288455901e\",\"compatibilityLevel\":1.0,\"name\":\"Test-G-Dev-01\",\"status\":\"Live\"}".replaceAll("rootNodeIdentifier",contentId);
        String draftHierarchy = "{\"identifier\":\"rootNodeIdentifier\",\"children\":[{\"ownershipType\":[\"createdBy\"],\"parent\":\"rootNodeIdentifier\",\"code\":\"TestBookUnit-01\",\"downloadUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273962618141081612/test_collection_textbookunit_01_1555132093281_do_11273962618141081612_2.0_spine.ecar\",\"description\":\"Test_Collection_TextBookUnit_01\",\"language\":[\"English\"],\"variants\":{\"online\":{\"ecarUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273962618141081612/test_collection_textbookunit_01_1555132093746_do_11273962618141081612_2.0_online.ecar\",\"size\":1087},\"spine\":{\"ecarUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273962618141081612/test_collection_textbookunit_01_1555132093281_do_11273962618141081612_2.0_spine.ecar\",\"size\":1087}},\"mimeType\":\"application/vnd.ekstep.content-collection\",\"idealScreenSize\":\"normal\",\"createdOn\":\"2019-04-13T10:05:49.100+0530\",\"objectType\":\"Content\",\"children\":[{\"ownershipType\":[\"createdBy\"],\"parent\":\"do_11273962618141081612\",\"code\":\"TestBookUnit-01-01\",\"downloadUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273962618146816013/test_collection_textbookunit_01_01_1555132092091_do_11273962618146816013_2.0_spine.ecar\",\"questions\":[],\"description\":\"TTest_Collection_TextBookUnit_01_01\",\"language\":[\"English\"],\"variants\":{\"online\":{\"ecarUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273962618146816013/test_collection_textbookunit_01_01_1555132092599_do_11273962618146816013_2.0_online.ecar\",\"size\":843},\"spine\":{\"ecarUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273962618146816013/test_collection_textbookunit_01_01_1555132092091_do_11273962618146816013_2.0_spine.ecar\",\"size\":844}},\"mimeType\":\"application/vnd.ekstep.content-collection\",\"idealScreenSize\":\"normal\",\"createdOn\":\"2019-04-13T10:05:49.105+0530\",\"objectType\":\"Content\",\"usesContent\":[],\"contentDisposition\":\"inline\",\"contentEncoding\":\"gzip\",\"lastUpdatedOn\":\"2019-04-13T10:05:49.105+0530\",\"sYS_INTERNAL_LAST_UPDATED_ON\":\"2019-04-13T10:38:13.246+0530\",\"dialcodeRequired\":\"No\",\"contentType\":\"TextBookUnit\",\"identifier\":\"do_11273962618146816013\",\"audience\":[\"Learner\"],\"lastStatusChangedOn\":\"2019-04-13T10:35:52.723+0530\",\"visibility\":\"Parent\",\"os\":[\"All\"],\"index\":1,\"mediaType\":\"content\",\"osId\":\"org.ekstep.launcher\",\"pkgVersion\":2,\"versionKey\":\"1555130149105\",\"idealScreenDensity\":\"hdpi\",\"depth\":2,\"s3Key\":\"ecar_files/do_11273962618146816013/test_collection_textbookunit_01_01_1555132092091_do_11273962618146816013_2.0_spine.ecar\",\"size\":844,\"lastPublishedOn\":\"2019-04-13T10:38:12.089+0530\",\"concepts\":[],\"compatibilityLevel\":1,\"name\":\"Test_Collection_TextBookUnit_01_01\",\"status\":\"Live\"}],\"contentDisposition\":\"inline\",\"contentEncoding\":\"gzip\",\"lastUpdatedOn\":\"2019-04-13T10:05:49.100+0530\",\"sYS_INTERNAL_LAST_UPDATED_ON\":\"2019-04-13T10:38:14.476+0530\",\"dialcodeRequired\":\"No\",\"contentType\":\"TextBookUnit\",\"identifier\":\"do_11273962618141081612\",\"audience\":[\"Learner\"],\"lastStatusChangedOn\":\"2019-04-13T10:35:55.736+0530\",\"visibility\":\"Parent\",\"os\":[\"All\"],\"index\":1,\"mediaType\":\"content\",\"osId\":\"org.ekstep.launcher\",\"pkgVersion\":2,\"versionKey\":\"1555130149100\",\"idealScreenDensity\":\"hdpi\",\"depth\":1,\"s3Key\":\"ecar_files/do_11273962618141081612/test_collection_textbookunit_01_1555132093281_do_11273962618141081612_2.0_spine.ecar\",\"size\":1087,\"lastPublishedOn\":\"2019-04-13T10:38:13.276+0530\",\"compatibilityLevel\":1,\"name\":\"Test_Collection_TextBookUnit_01\",\"status\":\"Live\"},{\"ownershipType\":[\"createdBy\"],\"parent\":\"rootNodeIdentifier\",\"code\":\"TestBookUnit-02\",\"downloadUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273962618149273614/test_collection_textbookunit_02_1555132094516_do_11273962618149273614_2.0_spine.ecar\",\"questions\":[],\"description\":\"TTest_Collection_TextBookUnit_02\",\"language\":[\"English\"],\"variants\":{\"online\":{\"ecarUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273962618149273614/test_collection_textbookunit_02_1555132095031_do_11273962618149273614_2.0_online.ecar\",\"size\":839},\"spine\":{\"ecarUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273962618149273614/test_collection_textbookunit_02_1555132094516_do_11273962618149273614_2.0_spine.ecar\",\"size\":837}},\"mimeType\":\"application/vnd.ekstep.content-collection\",\"idealScreenSize\":\"normal\",\"createdOn\":\"2019-04-13T10:05:49.108+0530\",\"objectType\":\"Content\",\"usesContent\":[],\"contentDisposition\":\"inline\",\"contentEncoding\":\"gzip\",\"lastUpdatedOn\":\"2019-04-13T10:05:49.108+0530\",\"sYS_INTERNAL_LAST_UPDATED_ON\":\"2019-04-13T10:38:15.664+0530\",\"dialcodeRequired\":\"No\",\"contentType\":\"TextBookUnit\",\"identifier\":\"do_11273962618149273614\",\"audience\":[\"Learner\"],\"lastStatusChangedOn\":\"2019-04-13T10:35:57.748+0530\",\"visibility\":\"Parent\",\"os\":[\"All\"],\"index\":2,\"mediaType\":\"content\",\"osId\":\"org.ekstep.launcher\",\"pkgVersion\":2,\"versionKey\":\"1555130149108\",\"idealScreenDensity\":\"hdpi\",\"depth\":1,\"s3Key\":\"ecar_files/do_11273962618149273614/test_collection_textbookunit_02_1555132094516_do_11273962618149273614_2.0_spine.ecar\",\"size\":837,\"lastPublishedOn\":\"2019-04-13T10:38:14.515+0530\",\"concepts\":[],\"compatibilityLevel\":1,\"name\":\"Test_Collection_TextBookUnit_02\",\"status\":\"Live\"}]}".replaceAll("rootNodeIdentifier",contentId+".img");
        String liveScript = "insert into hierarchy_store_test.content_hierarchy_test (identifier,hierarchy) values('rootNodeIdentifier','liveHierarchyData')".replace("rootNodeIdentifier",contentId).replace("liveHierarchyData",liveHierarchy);
        String draftScript = "insert into hierarchy_store_test.content_hierarchy_test (identifier,hierarchy) values('rootNodeIdentifier','liveHierarchyData')".replace("rootNodeIdentifier",contentId+".img").replace("liveHierarchyData",draftHierarchy);

        System.out.println("draftScript : "+draftScript);
        System.out.println("liveScript : "+liveScript);

        if(isLive){
            executeScript(draftScript,liveScript);
        }else{
            executeScript(draftScript);
        }
    }

    private void getCassandraRecord(String contentId){
        try{
            //Session session = CassandraConnector.getSession();
            String query = "select hierarchy from hierarchy_store_test.content_hierarchy_test where identifier = 'rootIdentifier'".replace("rootIdentifier",contentId);
            ResultSet results = CassandraConnector.getSession().execute(query);
            if(null!=results){
                for(Row row : results.all()){
                    System.out.println("Cassandra Hierarchy : "+row.getString("hierarchy"));
                }
            }
        }catch(Exception e){

        }
    }

}
