package org.sunbird.sync.tool.mgr;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.sunbird.graph.cache.util.RedisStoreUtil;
import org.sunbird.learning.hierarchy.store.HierarchyStore;
import org.sunbird.learning.util.ControllerUtil;
import org.sunbird.searchindex.elasticsearch.ElasticSearchUtil;
import org.sunbird.sync.tool.util.DialcodeSync;
import org.sunbird.graph.dac.model.Node;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DialcodeSync.class, HierarchyStore.class, ControllerUtil.class, ElasticSearchUtil.class, RedisStoreUtil.class})
@PowerMockIgnore({ "javax.management.*", "sun.security.ssl.*", "javax.net.ssl.*", "javax.crypto.*" })
public class CassandraESSyncManagerTest {
    private ObjectMapper mapper = new ObjectMapper();

	@Test
    public void testsyncDialcodesByIdsWithDialcodes() throws Exception {
        DialcodeSync dialcodeSync = PowerMockito.mock(DialcodeSync.class);
        PowerMockito.when(dialcodeSync.sync(Mockito.anyList())).thenReturn(1);
        List<String> dialcodes = Arrays.asList("A1B2C3");
        
        CassandraESSyncManager cassandraESSyncManager = new CassandraESSyncManager(dialcodeSync);
        cassandraESSyncManager.syncDialcodesByIds(dialcodes);
    }

	@Test
    public void testsyncDialcodesByIdsWithoutDialcodes() throws Exception {
        DialcodeSync dialcodeSync = PowerMockito.mock(DialcodeSync.class);
        PowerMockito.when(dialcodeSync.sync(Mockito.anyList())).thenReturn(1);
        List<String> dialcodes = null;
        
        CassandraESSyncManager cassandraESSyncManager = new CassandraESSyncManager(dialcodeSync);
        cassandraESSyncManager.syncDialcodesByIds(dialcodes);
    }

    @Test
    public void testSyncCollectionIds() throws Exception {
        CassandraESSyncManager cassandraESSyncManager = new CassandraESSyncManager();
        String hierarchy = "{\"ownershipType\": [  \"createdBy\"],\"code\": \"org.sunbird.feb16.story.test01\",\"channel\": \"in.ekstep\",\"description\": \"Text Book in English for Class III\",\"language\": [  \"English\"],\"mimeType\": \"application/vnd.ekstep.content-collection\",\"idealScreenSize\": \"normal\",\"createdOn\": \"2020-04-28T12:27:00.696+0530\",\"reservedDialcodes\": {  \"L4N4W2\": 0},\"objectType\": \"Content\",\"children\": [  {\"ownershipType\": [  \"createdBy\"],\"parent\": \"do_11300936311299276811\",\"code\": \"b9a50833-eff6-4ef5-a2a4-2413f2d51f6c\",\"channel\": \"in.ekstep\",\"description\": \"Test_CourseUnit_desc_1\",\"language\": [  \"English\"],\"mimeType\": \"application/vnd.ekstep.content-collection\",\"idealScreenSize\": \"normal\",\"createdOn\": \"2020-04-28T12:27:16.715+0530\",\"objectType\": \"Content\",\"children\": [  {\"ownershipType\": [  \"createdBy\"],\"parent\": \"do_11300936324476928016\",\"code\": \"b9a50833-eff6-4ef5-a2a4-2413f2d51f6d\",\"channel\": \"in.ekstep\",\"description\": \"Test_CourseSubUnit_desc_1\",\"language\": [  \"English\"],\"mimeType\": \"application/vnd.ekstep.content-collection\",\"idealScreenSize\": \"normal\",\"createdOn\": \"2020-04-28T12:27:16.713+0530\",\"objectType\": \"Content\",\"contentDisposition\": \"inline\",\"lastUpdatedOn\": \"2020-04-28T12:27:16.713+0530\",\"contentEncoding\": \"gzip\",\"contentType\": \"TextBookUnit\",\"dialcodeRequired\": \"No\",\"identifier\": \"do_11300936324475289614\",\"lastStatusChangedOn\": \"2020-04-28T12:27:16.713+0530\",\"os\": [  \"All\"],\"visibility\": \"Parent\",\"index\": 1,\"mediaType\": \"content\",\"osId\": \"org.sunbird.launcher\",\"languageCode\": [  \"en\"],\"versionKey\": \"1588057036713\",\"license\": \"CC BY 4.0\",\"idealScreenDensity\": \"hdpi\",\"depth\": 2,\"compatibilityLevel\": 1,\"name\": \"Test_Course_SubUnit_name_1\",\"status\": \"Draft\"  },  {\"ownershipType\": [  \"createdBy\"],\"parent\": \"do_11300936324476928016\",\"code\": \"b9a50833-eff6-4ef5-a2a4-2413f2d51f6e\",\"channel\": \"in.ekstep\",\"description\": \"Test_CourseSubUnit_desc_2\",\"language\": [  \"English\"],\"mimeType\": \"application/vnd.ekstep.content-collection\",\"idealScreenSize\": \"normal\",\"createdOn\": \"2020-04-28T12:27:16.703+0530\",\"objectType\": \"Content\",\"contentDisposition\": \"inline\",\"lastUpdatedOn\": \"2020-04-28T12:27:16.703+0530\",\"contentEncoding\": \"gzip\",\"contentType\": \"TextBookUnit\",\"dialcodeRequired\": \"No\",\"identifier\": \"do_11300936324467097612\",\"lastStatusChangedOn\": \"2020-04-28T12:27:16.703+0530\",\"audience\": [  \"Learner\"],\"os\": [  \"All\"],\"visibility\": \"Parent\",\"index\": 2,\"mediaType\": \"content\",\"osId\": \"org.sunbird.launcher\",\"languageCode\": [  \"en\"],\"versionKey\": \"1588057036703\",\"license\": \"CC BY 4.0\",\"idealScreenDensity\": \"hdpi\",\"depth\": 2,\"compatibilityLevel\": 1,\"name\": \"Test_Course_SubUnit_name_2\",\"status\": \"Draft\"  }],\"contentDisposition\": \"inline\",\"lastUpdatedOn\": \"2020-04-28T12:27:16.715+0530\",\"contentEncoding\": \"gzip\",\"contentType\": \"TextBookUnit\",\"dialcodeRequired\": \"No\",\"identifier\": \"do_11300936324476928016\",\"lastStatusChangedOn\": \"2020-04-28T12:27:16.715+0530\",\"audience\": [  \"Learner\"],\"os\": [  \"All\"],\"visibility\": \"Parent\",\"index\": 1,\"mediaType\": \"content\",\"osId\": \"org.sunbird.launcher\",\"languageCode\": [  \"en\"],\"versionKey\": \"1588057036715\",\"license\": \"CC BY 4.0\",\"idealScreenDensity\": \"hdpi\",\"depth\": 1,\"compatibilityLevel\": 1,\"name\": \"Test_CourseUnit_1\",\"status\": \"Draft\",\"dialcodes\": [  \"L4N4W2\"    ]  }],\"contentDisposition\": \"inline\",\"lastUpdatedOn\": \"2020-04-28T12:27:16.845+0530\",\"contentEncoding\": \"gzip\",\"sYS_INTERNAL_LAST_UPDATED_ON\": \"2020-04-28T12:28:50.484+0530\",\"contentType\": \"TextBook\",\"dialcodeRequired\": \"No\",\"identifier\": \"do_11300936311299276811\",\"lastStatusChangedOn\": \"2020-04-28T12:27:00.696+0530\",\"audience\": [  \"Learner\"],\"os\": [  \"All\"],\"visibility\": \"Default\",\"childNodes\": [  \"do_11300936324476928016\",  \"do_11300936324475289614\",  \"do_11300936324467097612\"],\"mediaType\": \"content\",\"osId\": \"org.sunbird.quiz.app\",\"languageCode\": [  \"en\"],\"version\": 2,\"tags\": [  \"QA_Content\"],\"versionKey\": \"1588057036845\",\"license\": \"CC BY 4.0\",\"idealScreenDensity\": \"hdpi\",\"framework\": \"NCF\",\"dialcodes\": [  \"I4Q9K5\"],\"depth\": 0,\"compatibilityLevel\": 1,\"name\": \"Marigold\",\"status\": \"Draft\"\n}";
        Map<String, Object> hierarchyMap = mapper.readValue(hierarchy, new TypeReference<HashMap>() {
        });

        PowerMockito.stub(PowerMockito.method(HierarchyStore.class, "getHierarchy")).toReturn(hierarchyMap);
        PowerMockito.stub(PowerMockito.method(HierarchyStore.class, "saveOrUpdateHierarchy")).toReturn(0);
        PowerMockito.stub(PowerMockito.method(HierarchyStore.class, "deleteHierarchy")).toReturn(0);

        PowerMockito.stub(PowerMockito.method(ControllerUtil.class, "getNode")).toReturn(getNode());

        PowerMockito.mockStatic(ElasticSearchUtil.class);
        PowerMockito.doNothing().when(ElasticSearchUtil.class);
        ElasticSearchUtil.bulkIndexWithIndexId(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap());

        PowerMockito.mockStatic(RedisStoreUtil.class);
        PowerMockito.doNothing().when(RedisStoreUtil.class);
        RedisStoreUtil.delete(Mockito.any());

        cassandraESSyncManager.syncCollectionIds("domain", new ArrayList<String>() {{
            add("do_1234");
        }});
    }

    private Node getNode() {
        Node node = new Node();
        node.setIdentifier("do_1234");
        node.setNodeType("DATA_NODE");
        node.setMetadata(new HashMap<String, Object>() {
            {
                put("identifier", "do_1234");
                put("primaryCategory", "Learning Resource");
                put("audience", new ArrayList<String>() {{
                    add("Student");
                }});
            }
        });
        return node;
    }
}
