package org.ekstep.taxonomy.mgr.impl;


import com.datastax.driver.core.ResultSet;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ekstep.cassandra.connector.util.CassandraConnector;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.content.mgr.impl.HierarchyManager;
import org.ekstep.content.mgr.impl.operation.content.CreateOperation;
import org.ekstep.learning.router.LearningRequestRouterPool;
import org.ekstep.taxonomy.content.common.TestParams;
import org.ekstep.test.common.CommonTestSetup;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.ekstep.taxonomy.mgr.impl.ContentManagerImplCreateContentTest.channelId;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class ContentHierarchyTest extends CommonTestSetup {

    HierarchyManager manager = new HierarchyManager();
    private static ObjectMapper mapper = new ObjectMapper();



    @Rule
    public ExpectedException exception = ExpectedException.none();

    private static String script_1 = "CREATE KEYSPACE IF NOT EXISTS hierarchy_store_test WITH replication = {'class':" +
            " 'SimpleStrategy','replication_factor': '1'};";
    private static String script_2 = "CREATE TABLE IF NOT EXISTS hierarchy_store_test.content_hierarchy_test " +
            "(identifier text, hierarchy text,PRIMARY KEY (identifier));";


    @BeforeClass
    public static void init() throws Exception {
        LearningRequestRouterPool.init();
        loadDefinition("definitions/content_definition.json", "definitions/concept_definition.json",
                "definitions/dimension_definition.json", "definitions/domain_definition.json");
        executeScript(script_1, script_2);
        createTextBook();
    }

    private static void createTextBook() throws Exception {
        String request = "{\"identifier\":\"do_11273401693397811211\",\"visibility\":\"Default\"," +
                "\"description\":\"TB\",\"name\":\"TB\",\"language\":[\"English\"],\"contentType\":\"TextBook\",\"code\":\"TB\",\"mimeType\":\"application/vnd.ekstep.content-collection\"}";
        Map<String, Object> tbMap = mapper.readValue(request, Map.class);
        Response response = new CreateOperation().create(tbMap, channelId);
        mapper.writeValueAsString(response);
    }

    @Test
    public void testUpdateHierarchy() throws IOException {
        String requestStr = "{\"nodesModified\":{\"do_11273401693397811211\":{\"isNew\":false,\"root\":true,\"metadata\":{}},\"textbookunit_0\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U1\",\"code\":\"testbook 0\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbookunit_1\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U2\",\"code\":\"testbook 1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbookunit_2\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U3\",\"code\":\"testbook 2\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbooksubunit_0\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U1.1\",\"code\":\"testbook 1.1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbooksubunit_1\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U2.1\",\"code\":\"testbook 2.1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbooksubunit_2\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U3.1\",\"code\":\"testbook 3.1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}},\"textbooksubsubunit_2\":{\"isNew\":true,\"root\":false,\"metadata\":{\"name\":\"U3.1.1\",\"code\":\"testbook 3.1.1\",\"mimeType\":\"application/vnd.ekstep.content-collection\",\"contentType\":\"TextBookUnit\"}}},\"hierarchy\":{\"do_11273401693397811211\":{\"name\":\"Test Undefined\",\"contentType\":\"TextBook\",\"children\":[\"textbookunit_0\",\"textbookunit_1\",\"textbookunit_2\"],\"root\":true},\"textbookunit_0\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[\"textbooksubunit_0\"],\"root\":false},\"textbookunit_1\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[\"textbooksubunit_1\"],\"root\":false},\"textbookunit_2\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[\"textbooksubunit_2\"],\"root\":false},\"textbooksubunit_0\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[],\"root\":false},\"textbooksubunit_1\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[],\"root\":false},\"textbooksubunit_2\":{\"name\":\"New Unit\",\"contentType\":\"TextBookUnit\",\"children\":[\"textbooksubsubunit_2\"],\"root\":false}},\"lastUpdatedBy\":\"pradyumna\"}";
        Map<String, Object> request = mapper.readValue(requestStr, Map.class);
        Response response = manager.update(request);
        Assert.assertEquals(ResponseCode.OK, response.getResponseCode());
        Assert.assertNotNull(response.getResult());

        String selectQuery = "SELECT * from hierarchy_store_test.content_hierarchy_test where " +
                "identifier='do_11273401693397811211.img'";
        session = CassandraConnector.getSession();
        ResultSet resultSet = session.execute(selectQuery);
        Assert.assertNotNull(resultSet.one().getString("hierarchy"));
    }

    /*@Test
    public void testReadValidCollection() {
        String contentId = "";
        Response response = manager.getContentHierarchy(contentId, contentId, null, null);
        Assert.assertEquals("successful", response.getParams().getStatus());
        Assert.assertNotNull(response.getResult());

    }

    @Test
    public void testReadValidCollectionModeEdit() {
        String contentId = "";
        Response response = manager.getContentHierarchy(contentId, contentId, "edit", null);
        Assert.assertEquals("successful", response.getParams().getStatus());
        Assert.assertNotNull(response.getResult());

    }

    @Test
    public void testReadValidWithoutHierarchy() {
        String contentId = "";
        Response response = manager.getContentHierarchy(contentId, contentId, null, null);
        Assert.assertEquals("successful", response.getParams().getStatus());
        Assert.assertNotNull(response.getResult());
    }

    @Test
    public void testReadInvalidId() {
        String contentId = "";
        Response response = manager.getContentHierarchy(contentId, contentId, null, null);
        Assert.assertEquals("failed", response.getParams().getStatus());
        Assert.assertEquals(ResponseCode.RESOURCE_NOT_FOUND.name(), response.getResponseCode());
    }


    @Test
    public void testReadValidUnit() {
        String contentId = "";
        Response response = manager.getContentHierarchy(contentId, contentId, null, null);
        Assert.assertEquals("successful", response.getParams().getStatus());
        Assert.assertNotNull(response.getResult());
    }

    @Test
    public void testReadInvalidUnitId() {
        String contentId = "";
        Response response = manager.getContentHierarchy(contentId, contentId, null, null);
        Assert.assertEquals("failed", response.getParams().getStatus());
        Assert.assertEquals(ResponseCode.RESOURCE_NOT_FOUND, response.getResponseCode());
    }



    @Test
    public void testUpdateWithoutTBId() throws IOException {
        String requestStr = "";
        Map<String, Object> request = mapper.readValue(requestStr, Map.class);
        Response response = manager.update(request);
        Assert.assertEquals(ResponseCode.CLIENT_ERROR, response.getResponseCode());
    }

    @Test
    public void testUpdateInvalidTBId() throws IOException {
        String requestStr = "";
        Map<String, Object> request = mapper.readValue(requestStr, Map.class);
        Response response = manager.update(request);
        Assert.assertEquals(ResponseCode.RESOURCE_NOT_FOUND, response.getResponseCode());
    }

    @Test
    public void testUpdateWithoutHierarchy() throws IOException {
        String requestStr = "";
        Map<String, Object> request = mapper.readValue(requestStr, Map.class);
        Response response = manager.update(request);
        Assert.assertEquals(ResponseCode.OK, response.getResponseCode());
    }

    @Test
    public void testUpdateInvalidRequest() throws IOException {
        String requestStr = "";
        Map<String, Object> request = mapper.readValue(requestStr, Map.class);
        Response response = manager.update(request);
        Assert.assertEquals(ResponseCode.CLIENT_ERROR, response.getResponseCode());
    }*/

}
