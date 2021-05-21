package org.sunbird.sync.tool.shell;

import com.datastax.driver.core.Row;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.sunbird.cassandra.connector.util.CassandraConnector;
import org.sunbird.common.dto.Response;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.kafka.KafkaClient;
import org.sunbird.learning.util.ControllerUtil;
import org.sunbird.sync.tool.mgr.BatchEnrolmentSyncManager;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CassandraConnector.class, ControllerUtil.class, KafkaClient.class})
@PowerMockIgnore({ "javax.management.*", "sun.security.ssl.*", "javax.net.ssl.*", "javax.crypto.*" })
public class BatchEnrolmentSyncManagerTest extends GraphEngineTestSetup {

    private static ObjectMapper mapper = new ObjectMapper();
    @Mock Row row;
    @Mock BatchEnrolmentSyncManager mgr;
    
    @BeforeClass
    public static void beforeClass() throws Exception {
        loadDefinition("definitions/content_definition.json");
    }
    
    @Test
    public void testSyncBatchDetailsUpdate() throws Exception {
        graphDb.execute("UNWIND [{ownershipType:[\"createdBy\"],copyright:\"ORG_002\",previewUrl:\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/content/assets/do_112831862871203840114/small.mp4\",downloadUrl:\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/ecar_files/do_112831862871203840114/test-resource-cert_1566389713658_do_112831862871203840114_1.0.ecar\",channel:\"01246944855007232011\",organisation:[\"ORG_002\"],showNotification:true,language:[\"English\"],mimeType:\"video/mp4\",variants:\"{\\\"spine\\\":{\\\"ecarUrl\\\":\\\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/ecar_files/do_112831862871203840114/test-resource-cert_1566389714022_do_112831862871203840114_1.0_spine.ecar\\\",\\\"size\\\":35757.0}}\",appIcon:\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/content/do_112831862871203840114/artifact/033019_sz_reviews_feat_1564126718632.thumb.jpg\",appId:\"dev.sunbird.portal\",artifactUrl:\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/content/assets/do_112831862871203840114/small.mp4\",contentEncoding:\"identity\",lockKey:\"be6bc445-c75e-471d-b46f-71fefe4a1d2f\",contentType:\"Resource\",lastUpdatedBy:\"c4cc494f-04c3-49f3-b3d5-7b1a1984abad\",audience:[\"Learner\"],visibility:\"Default\",consumerId:\"273f3b18-5dda-4a27-984a-060c7cd398d3\",mediaType:\"content\",osId:\"org.sunbird.quiz.app\",lastPublishedBy:\"System\",version:1,license:\"Creative Commons Attribution (CC BY)\",prevState:\"Draft\",lastPublishedOn:\"2019-08-21T12:15:13.652+0000\",size:416488,IL_FUNC_OBJECT_TYPE:\"Content\",name:\"Test Resource Cert\",status:\"Live\",code:\"7e6630c7-3818-4319-92ac-4d08c33904d8\",streamingUrl:\"https://sunbirddevmedia-inct.streaming.media.azure.net/25d7a94c-9be3-471c-926b-51eb5d3c4c2c/small.ism/manifest(format=m3u8-aapl-v3)\",posterImage:\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/content/do_11281332607717376012/artifact/033019_sz_reviews_feat_1564126718632.jpg\",idealScreenSize:\"normal\",createdOn:\"2019-08-21T12:11:50.644+0000\",contentDisposition:\"inline\",lastUpdatedOn:\"2019-08-21T12:15:13.020+0000\",SYS_INTERNAL_LAST_UPDATED_ON:\"2019-08-21T12:30:16.783+0000\",dialcodeRequired:\"No\",creator:\"Pradyumna\",lastStatusChangedOn:\"2019-08-21T12:15:14.384+0000\",createdFor:[\"01246944855007232011\"],os:[\"All\"],IL_SYS_NODE_TYPE:\"DATA_NODE\",pkgVersion:1,versionKey:\"1566389713020\",idealScreenDensity:\"hdpi\",s3Key:\"ecar_files/do_112831862871203840114/test-resource-cert_1566389713658_do_112831862871203840114_1.0.ecar\",framework:\"K-12\",createdBy:\"c4cc494f-04c3-49f3-b3d5-7b1a1984abad\",compatibilityLevel:1,IL_UNIQUE_ID:\"do_112831862871203840114\",resourceType:\"Learn\"},{ownershipType:[\"createdBy\"],copyright:\"Sunbird\",certTemplate:\"[{\\\"name\\\":\\\"100PercentCompletionCertificate\\\",\\\"issuer\\\":{\\\"name\\\":\\\"Gujarat Council of Educational Research and Training\\\",\\\"url\\\":\\\"https://gcert.gujarat.gov.in/gcert/\\\",\\\"publicKey\\\":[\\\"1\\\",\\\"2\\\"]},\\\"signatoryList\\\":[{\\\"name\\\":\\\"CEO Gujarat\\\",\\\"id\\\":\\\"CEO\\\",\\\"designation\\\":\\\"CEO\\\",\\\"image\\\":\\\"https://cdn.pixabay.com/photo/2014/11/09/08/06/signature-523237__340.jpg\\\"}],\\\"htmlTemplate\\\":\\\"https://drive.google.com/uc?authuser=1&id=1ryB71i0Oqn2c3aqf9N6Lwvet-MZKytoM&export=download\\\",\\\"notifyTemplate\\\":{\\\"subject\\\":\\\"Course completion certificate\\\",\\\"stateImgUrl\\\":\\\"https://sunbirddev.blob.core.windows.net/orgemailtemplate/img/File-0128212938260643843.png\\\",\\\"regardsperson\\\":\\\"Chairperson\\\",\\\"regards\\\":\\\"Minister of Gujarat\\\",\\\"emailTemplateType\\\":\\\"defaultCertTemp\\\"}}]\",downloadUrl:\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/ecar_files/do_11283193441064550414/test-prad-course-cert_1566398313947_do_11283193441064550414_1.0_spine.ecar\",channel:\"b00bc992ef25f1a9a8d63291e20efc8d\",organisation:[\"Sunbird\"],language:[\"English\"],variants:\"{\\\"online\\\":{\\\"ecarUrl\\\":\\\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/ecar_files/do_11283193441064550414/test-prad-course-cert_1566398314186_do_11283193441064550414_1.0_online.ecar\\\",\\\"size\\\":4034.0},\\\"spine\\\":{\\\"ecarUrl\\\":\\\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/ecar_files/do_11283193441064550414/test-prad-course-cert_1566398313947_do_11283193441064550414_1.0_spine.ecar\\\",\\\"size\\\":73256.0}}\",mimeType:\"application/vnd.ekstep.content-collection\",leafNodes:[\"do_112831862871203840114\"],c_sunbird_dev_private_batch_count:0,appIcon:\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/content/do_11283193441064550414/artifact/033019_sz_reviews_feat_1564126718632.thumb.jpg\",appId:\"local.sunbird.portal\",contentEncoding:\"gzip\",lockKey:\"b079cf15-9e45-4865-be56-2edafa432dd3\",mimeTypesCount:\"{\\\"application/vnd.ekstep.content-collection\\\":1,\\\"video/mp4\\\":1}\",totalCompressedSize:416488,contentType:\"Course\",lastUpdatedBy:\"874ed8a5-782e-4f6c-8f36-e0288455901e\",audience:[\"Learner\"],toc_url:\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/content/do_11283193441064550414/artifact/do_11283193441064550414_toc.json\",visibility:\"Default\",contentTypesCount:\"{\\\"CourseUnit\\\":1,\\\"Resource\\\":1}\",author:\"b00bc992ef25f1a9a8d63291e20efc8d\",childNodes:[\"do_11283193463014195215\"],consumerId:\"273f3b18-5dda-4a27-984a-060c7cd398d3\",mediaType:\"content\",osId:\"org.sunbird.quiz.app\",lastPublishedBy:\"System\",version:2,license:\"Creative Commons Attribution (CC BY)\",prevState:\"Draft\",size:73256,lastPublishedOn:\"2019-08-21T14:38:33.816+0000\",IL_FUNC_OBJECT_TYPE:\"Content\",name:\"test prad course cert\",status:\"Live\",code:\"org.sunbird.SUi47U\",description:\"Enter description for Course\",posterImage:\"https://sunbirddev.blob.core.windows.net/sunbird-content-dev/content/do_11281332607717376012/artifact/033019_sz_reviews_feat_1564126718632.jpg\",idealScreenSize:\"normal\",createdOn:\"2019-08-21T14:37:23.486+0000\",reservedDialcodes:\"{\\\"I1X4R4\\\":0}\",contentDisposition:\"inline\",lastUpdatedOn:\"2019-08-21T14:38:33.212+0000\",SYS_INTERNAL_LAST_UPDATED_ON:\"2019-11-13T12:54:08.295+0000\",dialcodeRequired:\"No\",creator:\"Creation\",createdFor:[\"ORG_001\"],lastStatusChangedOn:\"2019-08-21T14:38:34.540+0000\",os:[\"All\"],IL_SYS_NODE_TYPE:\"DATA_NODE\",pkgVersion:1,versionKey:\"1566398313212\",idealScreenDensity:\"hdpi\",dialcodes:[\"I1X4R4\"],s3Key:\"ecar_files/do_11283193441064550414/test-prad-course-cert_1566398313947_do_11283193441064550414_1.0_spine.ecar\",depth:0,framework:\"tpd\",me_averageRating:5,createdBy:\"874ed8a5-782e-4f6c-8f36-e0288455901e\",leafNodesCount:1,compatibilityLevel:4,IL_UNIQUE_ID:\"do_112693299606175744173\",c_sunbird_dev_open_batch_count:0,resourceType:\"Course\"}] as row CREATE (n:domain) SET n += row");
        Row row = PowerMockito.mock(Row.class);
//        BatchEnrolmentSyncManager mgr = PowerMockito.spy(new BatchEnrolmentSyncManager());
        PowerMockito.doReturn(Arrays.asList(row)).when(mgr).readBatch(new String[]{"do_112693299606175744173"});
        PowerMockito.when(row.getInt("status")).thenReturn(1);
        PowerMockito.when(row.getString("name")).thenReturn("testbatch");
        PowerMockito.when(row.getString("batchid")).thenReturn("1023456");
        PowerMockito.when(row.getString("startdate")).thenReturn("2020-02-06");
        PowerMockito.when(row.getString("enddate")).thenReturn("2020-02-08");
        PowerMockito.when(row.getString("enrollmentenddate")).thenReturn(null);
        PowerMockito.when(row.getString("enrollmenttype")).thenReturn("open");
        PowerMockito.when(row.getString("courseid")).thenReturn("do_112693299606175744173");
        PowerMockito.when(row.getList("createdfor", String.class)).thenReturn(new ArrayList<>());
        mgr.sync("batch-detail-update", null, null, null, null, new String[]{"do_112693299606175744173"});
    }

    @Test
    public void testSyncBatchDetailsUpdateFail1() throws Exception {
        Row row = PowerMockito.mock(Row.class);
        BatchEnrolmentSyncManager mgr = PowerMockito.spy(new BatchEnrolmentSyncManager());
        PowerMockito.doReturn(new ArrayList<>()).when(mgr).readBatch(new String[]{"do_112693299606175744173"});
        mgr.sync("batch-detail-update", null, null, null, null, new String[]{"do_112693299606175744173"});
    }

    @Test
    public void testGenerateBatchEnrolUpdateKafkaEvent() throws Exception {
        BatchEnrolmentSyncManager mgr = PowerMockito.spy(new BatchEnrolmentSyncManager());
        Map<String, Object> map = new HashMap<String, Object>() {{
           put("batchid", "batch-1");
           put("courseid", "course-1");
           put("userid", "user-1");
        }};
        String event = mgr.generateBatchEnrolUpdateKafkaEvent(map);
        System.out.println("Event: " +event);
        Assert.assertTrue(StringUtils.isNotBlank(event));
    }

    @Test
    public void testGenerateBatchSyncKafkaEvent() throws Exception {
        BatchEnrolmentSyncManager mgr = PowerMockito.spy(new BatchEnrolmentSyncManager());
        Map<String, Object> map = new HashMap<String, Object>() {{
            put("batchid", "batch-1");
            put("courseid", "course-1");
            put("userid", "user-1");
        }};
        String event = mgr.generateBatchSyncKafkaEvent(map);
        System.out.println("Event: " +event);
        Assert.assertTrue(StringUtils.isNotBlank(event));
    }

}
