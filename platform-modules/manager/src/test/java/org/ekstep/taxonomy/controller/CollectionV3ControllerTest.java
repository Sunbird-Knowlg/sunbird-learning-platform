package org.ekstep.taxonomy.controller;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

    private static final List<String> VALID_DIAL_CODES = Arrays.asList("ABC123", "BCD123", "CDE123", "DEF123", "EFG123");
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

    @Ignore
    @Test
    public void testDialLinkWithLiveCollection() throws Exception {
        String createCollReq = "{\"request\": {\"content\": {\"name\": \"Test-G-Dev-01\",\"code\": \"test.book.1\",\"mimeType\": \"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBook\"}}}";
        String textbookId = createContent(createCollReq);
        String sysUpdateReq = "{\"request\":{\"content\":{\"status\":\"Live\"}}}";
        systemUpdate(textbookId, sysUpdateReq);
        updateCassandraHierarchy(textbookId, true);
        Response hierarchyResponse = getCollectionHierarchy(textbookId);
        assertEquals("OK", hierarchyResponse.getResponseCode().toString());
        String dialLinkReq = "{\"request\":{\"content\":[{\"dialcode\":\"ABC123\",\"identifier\":\"do_11273971924529152012\"},{\"dialcode\":[\"BCD123\",\"CDE123\"],\"identifier\":[\"do_11273971924534886413\"]},{\"dialcode\":\"DEF123\",\"identifier\":[\"do_11273971924536524814\"]},{\"dialcode\":\"EFG123\",\"identifier\":[\"do_11273971924538982415\"]}]}}";
        linkDialCode(textbookId, dialLinkReq);
        Response response = getContent(textbookId + ".img");
        assertEquals("OK", response.getResponseCode().toString());
        String imageHierarchy = getCassandraRecord(textbookId + ".img");
        assertNotNull(imageHierarchy);
        for (String dialcode : VALID_DIAL_CODES) {
            if (!imageHierarchy.contains(dialcode))
                assertTrue(false);
        }
    }

    /**
     *
     * @param requestBody
     * @return
     * @throws Exception
     */
    private String createContent(String requestBody) throws Exception {
        String path = CONTENT_BASE_PATH + "/create";
        actions = mockMvc.perform(MockMvcRequestBuilders.post(path).header("user-id", "ilimi").header("X-Channel-Id", "channelTest")
                .header("user-id", "ilimi").contentType(MediaType.APPLICATION_JSON).content(requestBody));
        assertEquals(200, actions.andReturn().getResponse().getStatus());
        return (String) getResponse(actions).getResult().get(TestParams.node_id.name());
    }

    /**
     *
     * @param contentId
     * @return
     * @throws Exception
     */
    private Response getContent(String contentId) throws Exception {
        String path = CONTENT_BASE_PATH + "/read/" + contentId;
        actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON).header("X-Channel-Id", "channelKA"));
        assertEquals(200, actions.andReturn().getResponse().getStatus());
        return getResponse(actions);
    }

    /**
     *
     * @param contentId
     * @return
     * @throws Exception
     */
    private Response getCollectionHierarchy(String contentId) throws Exception {
        String path = CONTENT_BASE_PATH + "/hierarchy/" + contentId;
        actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON).header("X-Channel-Id", "channelKA"));
        assertEquals(200, actions.andReturn().getResponse().getStatus());
        return getResponse(actions);
    }

    /**
     *
     * @param contentId
     * @param requestBody
     * @throws Exception
     */
    private void systemUpdate(String contentId, String requestBody) throws Exception {
        String path = "/system/v3/content/update/" + contentId;
        actions = mockMvc.perform(MockMvcRequestBuilders.patch(path).header("user-id", "ilimi").header("X-Channel-Id", "channelTest")
                .header("user-id", "ilimi").contentType(MediaType.APPLICATION_JSON).content(requestBody));
        assertEquals(200, actions.andReturn().getResponse().getStatus());
    }

    /**
     *
     * @param contentId
     * @param requestBody
     * @return
     * @throws Exception
     */
    private Response linkDialCode(String contentId, String requestBody) throws Exception {
        String path = BASE_PATH + "/dialcode/link/" + contentId;
        actions = mockMvc.perform(MockMvcRequestBuilders.post(path).header("X-Channel-Id", "channelTest").contentType(MediaType.APPLICATION_JSON).content(requestBody));
        System.out.println("DIAL LINK Response : " + actions.andReturn().getResponse().getContentAsString());
        assertEquals(200, actions.andReturn().getResponse().getStatus());
        return getResponse(actions);
    }

    /**
     *
     * @param contentId
     * @param isLive
     */
    private void updateCassandraHierarchy(String contentId, boolean isLive) {
        String liveHierarchy = "{\"ownershipType\":[\"createdBy\"],\"parent\":\"rootNodeIdentifier\",\"code\":\"test.book.1\",\"channel\":\"in.ekstep\",\"downloadUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/rootNodeIdentifier/test-g-dev-01_1555141553521_rootNodeIdentifier_1.0_spine.ecar\",\"language\":[\"English\"],\"variants\":{\"online\":{\"ecarUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/rootNodeIdentifier/test-g-dev-01_1555141553911_rootNodeIdentifier_1.0_online.ecar\",\"size\":1527},\"spine\":{\"ecarUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/rootNodeIdentifier/test-g-dev-01_1555141553521_rootNodeIdentifier_1.0_spine.ecar\",\"size\":21644}},\"mimeType\":\"application/vnd.ekstep.content-collection\",\"posterImage\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/content/do_112577747416195072185/artifact/edu-success_1535369558773.jpeg\",\"idealScreenSize\":\"normal\",\"createdOn\":\"2019-04-13T13:14:44.635+0530\",\"objectType\":\"Content\",\"appIcon\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/content/rootNodeIdentifier/artifact/edu-success_1535369558773.thumb.jpeg\",\"children\":[{\"ownershipType\":[\"createdBy\"],\"parent\":\"rootNodeIdentifier\",\"code\":\"TestBookUnit-01\",\"downloadUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273971924529152012/test_collection_textbookunit_01_1555141549019_do_11273971924529152012_1.0_spine.ecar\",\"description\":\"Test_Collection_TextBookUnit_01\",\"language\":[\"English\"],\"variants\":{\"online\":{\"ecarUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273971924529152012/test_collection_textbookunit_01_1555141549501_do_11273971924529152012_1.0_online.ecar\",\"size\":1044},\"spine\":{\"ecarUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273971924529152012/test_collection_textbookunit_01_1555141549019_do_11273971924529152012_1.0_spine.ecar\",\"size\":1044}},\"mimeType\":\"application/vnd.ekstep.content-collection\",\"idealScreenSize\":\"normal\",\"createdOn\":\"2019-04-13T13:15:09.436+0530\",\"objectType\":\"Content\",\"children\":[{\"ownershipType\":[\"createdBy\"],\"parent\":\"do_11273971924529152012\",\"code\":\"TestBookUnit-01-01\",\"downloadUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273971924534886413/test_collection_textbookunit_01_01_1555141530517_do_11273971924534886413_1.0_spine.ecar\",\"questions\":[],\"description\":\"TTest_Collection_TextBookUnit_01_01\",\"language\":[\"English\"],\"variants\":{\"online\":{\"ecarUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273971924534886413/test_collection_textbookunit_01_01_1555141546308_do_11273971924534886413_1.0_online.ecar\",\"size\":580},\"spine\":{\"ecarUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273971924534886413/test_collection_textbookunit_01_01_1555141530517_do_11273971924534886413_1.0_spine.ecar\",\"size\":579}},\"mimeType\":\"application/vnd.ekstep.content-collection\",\"idealScreenSize\":\"normal\",\"createdOn\":\"2019-04-13T13:15:09.442+0530\",\"objectType\":\"Content\",\"usesContent\":[],\"contentDisposition\":\"inline\",\"contentEncoding\":\"gzip\",\"lastUpdatedOn\":\"2019-04-13T13:15:09.442+0530\",\"sYS_INTERNAL_LAST_UPDATED_ON\":\"2019-04-13T13:15:47.653+0530\",\"dialcodeRequired\":\"No\",\"contentType\":\"TextBookUnit\",\"identifier\":\"do_11273971924534886413\",\"audience\":[\"Learner\"],\"lastStatusChangedOn\":\"2019-04-13T13:15:46.838+0530\",\"visibility\":\"Parent\",\"os\":[\"All\"],\"index\":1,\"mediaType\":\"content\",\"osId\":\"org.ekstep.launcher\",\"pkgVersion\":1,\"versionKey\":\"1555141509442\",\"idealScreenDensity\":\"hdpi\",\"depth\":2,\"s3Key\":\"ecar_files/do_11273971924534886413/test_collection_textbookunit_01_01_1555141530517_do_11273971924534886413_1.0_spine.ecar\",\"size\":579,\"lastPublishedOn\":\"2019-04-13T13:15:30.361+0530\",\"concepts\":[],\"compatibilityLevel\":1,\"name\":\"Test_Collection_TextBookUnit_01_01\",\"status\":\"Live\"}],\"contentDisposition\":\"inline\",\"contentEncoding\":\"gzip\",\"lastUpdatedOn\":\"2019-04-13T13:15:09.439+0530\",\"sYS_INTERNAL_LAST_UPDATED_ON\":\"2019-04-13T13:15:50.278+0530\",\"dialcodeRequired\":\"No\",\"contentType\":\"TextBookUnit\",\"identifier\":\"do_11273971924529152012\",\"audience\":[\"Learner\"],\"lastStatusChangedOn\":\"2019-04-13T13:15:50.112+0530\",\"visibility\":\"Parent\",\"os\":[\"All\"],\"index\":1,\"mediaType\":\"content\",\"osId\":\"org.ekstep.launcher\",\"pkgVersion\":1,\"versionKey\":\"1555141509436\",\"idealScreenDensity\":\"hdpi\",\"depth\":1,\"s3Key\":\"ecar_files/do_11273971924529152012/test_collection_textbookunit_01_1555141549019_do_11273971924529152012_1.0_spine.ecar\",\"size\":1044,\"lastPublishedOn\":\"2019-04-13T13:15:48.949+0530\",\"compatibilityLevel\":1,\"name\":\"Test_Collection_TextBookUnit_01\",\"status\":\"Live\"},{\"ownershipType\":[\"createdBy\"],\"parent\":\"rootNodeIdentifier\",\"code\":\"TestBookUnit-02\",\"downloadUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273971924536524814/test_collection_textbookunit_02_1555141550361_do_11273971924536524814_1.0_spine.ecar\",\"description\":\"TTest_Collection_TextBookUnit_02\",\"language\":[\"English\"],\"variants\":{\"online\":{\"ecarUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273971924536524814/test_collection_textbookunit_02_1555141550769_do_11273971924536524814_1.0_online.ecar\",\"size\":1041},\"spine\":{\"ecarUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273971924536524814/test_collection_textbookunit_02_1555141550361_do_11273971924536524814_1.0_spine.ecar\",\"size\":1040}},\"mimeType\":\"application/vnd.ekstep.content-collection\",\"idealScreenSize\":\"normal\",\"createdOn\":\"2019-04-13T13:15:09.444+0530\",\"objectType\":\"Content\",\"children\":[{\"ownershipType\":[\"createdBy\"],\"parent\":\"do_11273971924536524814\",\"code\":\"TestBookUnit-02-01\",\"downloadUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273971924538982415/test_collection_textbookunit_02_01_1555141547742_do_11273971924538982415_1.0_spine.ecar\",\"questions\":[],\"description\":\"TTest_Collection_TextBookUnit_02_01\",\"language\":[\"English\"],\"variants\":{\"online\":{\"ecarUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273971924538982415/test_collection_textbookunit_02_01_1555141548124_do_11273971924538982415_1.0_online.ecar\",\"size\":581},\"spine\":{\"ecarUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273971924538982415/test_collection_textbookunit_02_01_1555141547742_do_11273971924538982415_1.0_spine.ecar\",\"size\":579}},\"mimeType\":\"application/vnd.ekstep.content-collection\",\"idealScreenSize\":\"normal\",\"createdOn\":\"2019-04-13T13:15:09.447+0530\",\"objectType\":\"Content\",\"usesContent\":[],\"contentDisposition\":\"inline\",\"contentEncoding\":\"gzip\",\"lastUpdatedOn\":\"2019-04-13T13:15:09.447+0530\",\"sYS_INTERNAL_LAST_UPDATED_ON\":\"2019-04-13T13:15:48.853+0530\",\"dialcodeRequired\":\"No\",\"contentType\":\"TextBookUnit\",\"identifier\":\"do_11273971924538982415\",\"audience\":[\"Learner\"],\"lastStatusChangedOn\":\"2019-04-13T13:15:48.675+0530\",\"visibility\":\"Parent\",\"os\":[\"All\"],\"index\":1,\"mediaType\":\"content\",\"osId\":\"org.ekstep.launcher\",\"pkgVersion\":1,\"versionKey\":\"1555141509447\",\"idealScreenDensity\":\"hdpi\",\"depth\":2,\"s3Key\":\"ecar_files/do_11273971924538982415/test_collection_textbookunit_02_01_1555141547742_do_11273971924538982415_1.0_spine.ecar\",\"size\":579,\"lastPublishedOn\":\"2019-04-13T13:15:47.741+0530\",\"concepts\":[],\"compatibilityLevel\":1,\"name\":\"Test_Collection_TextBookUnit_02_01\",\"status\":\"Live\"}],\"contentDisposition\":\"inline\",\"contentEncoding\":\"gzip\",\"lastUpdatedOn\":\"2019-04-13T13:15:09.444+0530\",\"sYS_INTERNAL_LAST_UPDATED_ON\":\"2019-04-13T13:15:51.592+0530\",\"dialcodeRequired\":\"No\",\"contentType\":\"TextBookUnit\",\"identifier\":\"do_11273971924536524814\",\"audience\":[\"Learner\"],\"lastStatusChangedOn\":\"2019-04-13T13:15:51.433+0530\",\"visibility\":\"Parent\",\"os\":[\"All\"],\"index\":2,\"mediaType\":\"content\",\"osId\":\"org.ekstep.launcher\",\"pkgVersion\":1,\"versionKey\":\"1555141509444\",\"idealScreenDensity\":\"hdpi\",\"depth\":1,\"s3Key\":\"ecar_files/do_11273971924536524814/test_collection_textbookunit_02_1555141550361_do_11273971924536524814_1.0_spine.ecar\",\"size\":1040,\"lastPublishedOn\":\"2019-04-13T13:15:50.356+0530\",\"compatibilityLevel\":1,\"name\":\"Test_Collection_TextBookUnit_02\",\"status\":\"Live\"}],\"contentDisposition\":\"inline\",\"contentEncoding\":\"gzip\",\"lastUpdatedOn\":\"2019-04-13T13:14:44.635+0530\",\"sYS_INTERNAL_LAST_UPDATED_ON\":\"2019-04-13T13:15:54.562+0530\",\"contentType\":\"TextBook\",\"dialcodeRequired\":\"No\",\"identifier\":\"rootNodeIdentifier\",\"audience\":[\"Learner\"],\"lastStatusChangedOn\":\"2019-04-13T13:15:54.435+0530\",\"visibility\":\"Default\",\"os\":[\"All\"],\"index\":null,\"mediaType\":\"content\",\"osId\":\"org.ekstep.quiz.app\",\"pkgVersion\":1,\"versionKey\":\"1555141509674\",\"idealScreenDensity\":\"hdpi\",\"depth\":0,\"s3Key\":\"ecar_files/rootNodeIdentifier/test-g-dev-01_1555141553521_rootNodeIdentifier_1.0_spine.ecar\",\"framework\":\"NCF\",\"size\":21644,\"lastPublishedOn\":\"2019-04-13T13:15:53.495+0530\",\"createdBy\":\"874ed8a5-782e-4f6c-8f36-e0288455901e\",\"compatibilityLevel\":1,\"name\":\"Test-G-Dev-01\",\"status\":\"Live\"}".replaceAll("rootNodeIdentifier", contentId);
        String draftHierarchy = "{\"identifier\":\"rootNodeIdentifier\",\"children\":[{\"ownershipType\":[\"createdBy\"],\"parent\":\"rootNodeIdentifier\",\"code\":\"TestBookUnit-01\",\"downloadUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273971924529152012/test_collection_textbookunit_01_1555141549019_do_11273971924529152012_1.0_spine.ecar\",\"description\":\"Test_Collection_TextBookUnit_01\",\"language\":[\"English\"],\"variants\":{\"online\":{\"ecarUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273971924529152012/test_collection_textbookunit_01_1555141549501_do_11273971924529152012_1.0_online.ecar\",\"size\":1044},\"spine\":{\"ecarUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273971924529152012/test_collection_textbookunit_01_1555141549019_do_11273971924529152012_1.0_spine.ecar\",\"size\":1044}},\"mimeType\":\"application/vnd.ekstep.content-collection\",\"idealScreenSize\":\"normal\",\"createdOn\":\"2019-04-13T13:15:09.436+0530\",\"objectType\":\"Content\",\"children\":[{\"ownershipType\":[\"createdBy\"],\"parent\":\"do_11273971924529152012\",\"code\":\"TestBookUnit-01-01\",\"downloadUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273971924534886413/test_collection_textbookunit_01_01_1555141530517_do_11273971924534886413_1.0_spine.ecar\",\"questions\":[],\"description\":\"TTest_Collection_TextBookUnit_01_01\",\"language\":[\"English\"],\"variants\":{\"online\":{\"ecarUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273971924534886413/test_collection_textbookunit_01_01_1555141546308_do_11273971924534886413_1.0_online.ecar\",\"size\":580},\"spine\":{\"ecarUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273971924534886413/test_collection_textbookunit_01_01_1555141530517_do_11273971924534886413_1.0_spine.ecar\",\"size\":579}},\"mimeType\":\"application/vnd.ekstep.content-collection\",\"idealScreenSize\":\"normal\",\"createdOn\":\"2019-04-13T13:15:09.442+0530\",\"objectType\":\"Content\",\"usesContent\":[],\"contentDisposition\":\"inline\",\"contentEncoding\":\"gzip\",\"lastUpdatedOn\":\"2019-04-13T13:15:09.442+0530\",\"sYS_INTERNAL_LAST_UPDATED_ON\":\"2019-04-13T13:15:47.653+0530\",\"dialcodeRequired\":\"No\",\"contentType\":\"TextBookUnit\",\"identifier\":\"do_11273971924534886413\",\"audience\":[\"Learner\"],\"lastStatusChangedOn\":\"2019-04-13T13:15:46.838+0530\",\"visibility\":\"Parent\",\"os\":[\"All\"],\"index\":1,\"mediaType\":\"content\",\"osId\":\"org.ekstep.launcher\",\"pkgVersion\":1,\"versionKey\":\"1555141509442\",\"idealScreenDensity\":\"hdpi\",\"depth\":2,\"s3Key\":\"ecar_files/do_11273971924534886413/test_collection_textbookunit_01_01_1555141530517_do_11273971924534886413_1.0_spine.ecar\",\"size\":579,\"lastPublishedOn\":\"2019-04-13T13:15:30.361+0530\",\"concepts\":[],\"compatibilityLevel\":1,\"name\":\"Test_Collection_TextBookUnit_01_01\",\"status\":\"Live\"}],\"contentDisposition\":\"inline\",\"contentEncoding\":\"gzip\",\"lastUpdatedOn\":\"2019-04-13T13:15:09.439+0530\",\"sYS_INTERNAL_LAST_UPDATED_ON\":\"2019-04-13T13:15:50.278+0530\",\"dialcodeRequired\":\"No\",\"contentType\":\"TextBookUnit\",\"identifier\":\"do_11273971924529152012\",\"audience\":[\"Learner\"],\"lastStatusChangedOn\":\"2019-04-13T13:15:50.112+0530\",\"visibility\":\"Parent\",\"os\":[\"All\"],\"index\":1,\"mediaType\":\"content\",\"osId\":\"org.ekstep.launcher\",\"pkgVersion\":1,\"versionKey\":\"1555141509436\",\"idealScreenDensity\":\"hdpi\",\"depth\":1,\"s3Key\":\"ecar_files/do_11273971924529152012/test_collection_textbookunit_01_1555141549019_do_11273971924529152012_1.0_spine.ecar\",\"size\":1044,\"lastPublishedOn\":\"2019-04-13T13:15:48.949+0530\",\"compatibilityLevel\":1,\"name\":\"Test_Collection_TextBookUnit_01\",\"status\":\"Live\"},{\"ownershipType\":[\"createdBy\"],\"parent\":\"rootNodeIdentifier\",\"code\":\"TestBookUnit-02\",\"downloadUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273971924536524814/test_collection_textbookunit_02_1555141550361_do_11273971924536524814_1.0_spine.ecar\",\"description\":\"TTest_Collection_TextBookUnit_02\",\"language\":[\"English\"],\"variants\":{\"online\":{\"ecarUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273971924536524814/test_collection_textbookunit_02_1555141550769_do_11273971924536524814_1.0_online.ecar\",\"size\":1041},\"spine\":{\"ecarUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273971924536524814/test_collection_textbookunit_02_1555141550361_do_11273971924536524814_1.0_spine.ecar\",\"size\":1040}},\"mimeType\":\"application/vnd.ekstep.content-collection\",\"idealScreenSize\":\"normal\",\"createdOn\":\"2019-04-13T13:15:09.444+0530\",\"objectType\":\"Content\",\"children\":[{\"ownershipType\":[\"createdBy\"],\"parent\":\"do_11273971924536524814\",\"code\":\"TestBookUnit-02-01\",\"downloadUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273971924538982415/test_collection_textbookunit_02_01_1555141547742_do_11273971924538982415_1.0_spine.ecar\",\"questions\":[],\"description\":\"TTest_Collection_TextBookUnit_02_01\",\"language\":[\"English\"],\"variants\":{\"online\":{\"ecarUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273971924538982415/test_collection_textbookunit_02_01_1555141548124_do_11273971924538982415_1.0_online.ecar\",\"size\":581},\"spine\":{\"ecarUrl\":\"https://ekstep-public-dev.s3-ap-south-1.amazonaws.com/ecar_files/do_11273971924538982415/test_collection_textbookunit_02_01_1555141547742_do_11273971924538982415_1.0_spine.ecar\",\"size\":579}},\"mimeType\":\"application/vnd.ekstep.content-collection\",\"idealScreenSize\":\"normal\",\"createdOn\":\"2019-04-13T13:15:09.447+0530\",\"objectType\":\"Content\",\"usesContent\":[],\"contentDisposition\":\"inline\",\"contentEncoding\":\"gzip\",\"lastUpdatedOn\":\"2019-04-13T13:15:09.447+0530\",\"sYS_INTERNAL_LAST_UPDATED_ON\":\"2019-04-13T13:15:48.853+0530\",\"dialcodeRequired\":\"No\",\"contentType\":\"TextBookUnit\",\"identifier\":\"do_11273971924538982415\",\"audience\":[\"Learner\"],\"lastStatusChangedOn\":\"2019-04-13T13:15:48.675+0530\",\"visibility\":\"Parent\",\"os\":[\"All\"],\"index\":1,\"mediaType\":\"content\",\"osId\":\"org.ekstep.launcher\",\"pkgVersion\":1,\"versionKey\":\"1555141509447\",\"idealScreenDensity\":\"hdpi\",\"depth\":2,\"s3Key\":\"ecar_files/do_11273971924538982415/test_collection_textbookunit_02_01_1555141547742_do_11273971924538982415_1.0_spine.ecar\",\"size\":579,\"lastPublishedOn\":\"2019-04-13T13:15:47.741+0530\",\"concepts\":[],\"compatibilityLevel\":1,\"name\":\"Test_Collection_TextBookUnit_02_01\",\"status\":\"Live\"}],\"contentDisposition\":\"inline\",\"contentEncoding\":\"gzip\",\"lastUpdatedOn\":\"2019-04-13T13:15:09.444+0530\",\"sYS_INTERNAL_LAST_UPDATED_ON\":\"2019-04-13T13:15:51.592+0530\",\"dialcodeRequired\":\"No\",\"contentType\":\"TextBookUnit\",\"identifier\":\"do_11273971924536524814\",\"audience\":[\"Learner\"],\"lastStatusChangedOn\":\"2019-04-13T13:15:51.433+0530\",\"visibility\":\"Parent\",\"os\":[\"All\"],\"index\":2,\"mediaType\":\"content\",\"osId\":\"org.ekstep.launcher\",\"pkgVersion\":1,\"versionKey\":\"1555141509444\",\"idealScreenDensity\":\"hdpi\",\"depth\":1,\"s3Key\":\"ecar_files/do_11273971924536524814/test_collection_textbookunit_02_1555141550361_do_11273971924536524814_1.0_spine.ecar\",\"size\":1040,\"lastPublishedOn\":\"2019-04-13T13:15:50.356+0530\",\"compatibilityLevel\":1,\"name\":\"Test_Collection_TextBookUnit_02\",\"status\":\"Live\"}]}".replaceAll("rootNodeIdentifier", contentId + ".img");
        String liveScript = "insert into hierarchy_store_test.content_hierarchy_test (identifier,hierarchy) values('rootNodeIdentifier','liveHierarchyData')".replace("rootNodeIdentifier", contentId).replace("liveHierarchyData", liveHierarchy);
        String draftScript = "insert into hierarchy_store_test.content_hierarchy_test (identifier,hierarchy) values('rootNodeIdentifier','liveHierarchyData')".replace("rootNodeIdentifier", contentId + ".img").replace("liveHierarchyData", draftHierarchy);
        if (isLive) {
            executeScript(draftScript, liveScript);
        } else {
            executeScript(draftScript);
        }
    }

    /**
     *
     * @param contentId
     * @return
     */
    private String getCassandraRecord(String contentId) {
        try {
            String query = "select hierarchy from hierarchy_store_test.content_hierarchy_test where identifier = 'rootIdentifier'".replace("rootIdentifier", contentId);
            ResultSet results = CassandraConnector.getSession().execute(query);
            if (null != results) {
                while (results.iterator().hasNext()) {
                    Row row = results.iterator().next();
                    String value = row.getString("hierarchy");
                    return value;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
