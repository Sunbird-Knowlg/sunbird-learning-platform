package org.sunbird.taxonomy.mgr.impl;

import com.datastax.driver.core.ResultSet;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sunbird.cassandra.connector.util.CassandraConnector;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.content.mgr.impl.HierarchyManager;
import org.sunbird.content.mgr.impl.operation.content.CreateOperation;
import org.sunbird.learning.router.LearningRequestRouterPool;
import org.sunbird.test.common.CommonTestSetup;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.IOException;
import java.util.Map;

import static org.sunbird.taxonomy.mgr.impl.ContentManagerImplCreateContentTest.channelId;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class ContentHierarchyTest extends CommonTestSetup {

    private static final String SCRIPT_1 = "CREATE KEYSPACE IF NOT EXISTS hierarchy_store_test WITH replication = {'class':'SimpleStrategy','replication_factor':'1'};";
    private static final String SCRIPT_2 = "CREATE TABLE IF NOT EXISTS hierarchy_store_test.content_hierarchy_test(identifier text, hierarchy text,PRIMARY KEY (identifier));";

    private HierarchyManager manager = new HierarchyManager();
    private static ObjectMapper mapper = new ObjectMapper();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @BeforeClass
    public static void init() throws Exception {
        LearningRequestRouterPool.init();
        loadDefinition("definitions/content_definition.json", "definitions/concept_definition.json",
                "definitions/dimension_definition.json", "definitions/domain_definition.json");
        executeScript(SCRIPT_1, SCRIPT_2);
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

    @Test
    public void testUpdateHierarchyWithoutRootModified() throws IOException {
        String requestStr = "{\"nodesModified\":{\"TestBookUnit-01\":{\"isNew\":true,\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\",\"keywords\":[],\"name\":\"Test_Collection_TextBookUnit_01\",\"description\":\"Test_Collection_TextBookUnit_01\",\"contentType\":\"TextBookUnit\",\"code\":\"30b0cc0c-18dc-4462-9b2b-8390b90dd3aca\"}},\"TestBookUnit-01-01\":{\"isNew\":true,\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\",\"keywords\":[],\"name\":\"Test_Collection_TextBookUnit_01_01\",\"description\":\"TTest_Collection_TextBookUnit_01_01\",\"contentType\":\"TextBookUnit\",\"code\":\"30b0cc0c-18dc-4462-9b2b-8390b90dd3aca\"}},\"TestBookUnit-02\":{\"isNew\":true,\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\",\"keywords\":[],\"name\":\"Test_Collection_TextBookUnit_02\",\"description\":\"TTest_Collection_TextBookUnit_02\",\"contentType\":\"TextBookUnit\",\"code\":\"30b0cc0c-18dc-4462-9b2b-8390b90dd3aca\"}},\"TestBookUnit-02-01\":{\"isNew\":true,\"root\":false,\"metadata\":{\"mimeType\":\"application/vnd.ekstep.content-collection\",\"keywords\":[],\"name\":\"Test_Collection_TextBookUnit_02_01\",\"description\":\"TTest_Collection_TextBookUnit_02_01\",\"contentType\":\"TextBookUnit\",\"code\":\"30b0cc0c-18dc-4462-9b2b-8390b90dd3aca\"}}},\"hierarchy\":{\"do_11273401693397811211\":{\"name\":\"Test TextBook\",\"contentType\":\"Collection\",\"children\":[\"TestBookUnit-01\",\"TestBookUnit-02\"],\"root\":true},\"TestBookUnit-01\":{\"name\":\"Test_Collection_TextBookUnit_01\",\"contentType\":\"TextBookUnit\",\"children\":[\"TestBookUnit-01-01\"],\"root\":false},\"TestBookUnit-02\":{\"name\":\"Test_Collection_TextBookUnit_02\",\"contentType\":\"TextBookUnit\",\"children\":[],\"root\":false}},\"lastUpdatedBy\":\"ecff2373-6c52-4956-b103-a9741eae16f0\"}";
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
