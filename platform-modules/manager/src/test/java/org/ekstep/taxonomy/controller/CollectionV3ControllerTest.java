package org.ekstep.taxonomy.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ekstep.graph.model.cache.CategoryCache;
import org.ekstep.learning.router.LearningRequestRouterPool;
import org.ekstep.test.common.CommonTestSetup;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

/**
 *
 * @author Kumar Gauraw
 */
public class CollectionV3ControllerTest extends CommonTestSetup {

    /** The context. */
    @Autowired
    private WebApplicationContext context;

    /** The actions. */
    private ResultActions actions;

    private MockMvc mockMvc;

    private static final String BASE_PATH = "/collection/v3";
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
        //ElasticSearchUtil.deleteIndex(DIALCODE_INDEX);
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

}
