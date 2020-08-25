package org.ekstep.mvcjobs.samza.test;

import com.google.gson.Gson;
import org.ekstep.mvcjobs.samza.service.util.CassandraConnector;
import org.ekstep.mvcjobs.samza.service.util.MVCProcessorCassandraIndexer;
import org.ekstep.mvcsearchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.util.HTTPUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.apache.samza.config.Config;

import java.io.IOException;
import java.util.Map;

import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ElasticSearchUtil.class, Config.class,MVCProcessorCassandraIndexer.class,CassandraConnector.class, HTTPUtil.class})
@PowerMockIgnore({"javax.management.*", "sun.security.ssl.*", "javax.net.ssl.*" , "javax.crypto.*"})
public class MVCProcessorCassandraTest {
    private String uniqueId = "do_113041248230580224116";
    private String eventData = "{\"identifier\":\"do_113041248230580224116\",\"action\":\"update-es-index\",\"stage\":1}";
    private String eventData2 = "{\"action\":\"update-ml-keywords\",\"stage\":\"2\",\"ml_Keywords\":[\"maths\",\"addition\",\"add\"],\"ml_contentText\":\"This is the content text for addition of two numbers.\"}";
    private String eventData3 = "{\"action\":\"update-ml-contenttextvector\",\"stage\":3,\"ml_contentTextVector\":[[0.2961231768131256, 0.13621050119400024, 0.655802309513092, -0.33641257882118225]]}";
    private String getResp = "{\"id\":\"api.content.read\",\"ver\":\"1.0\",\"ts\":\"2020-07-21T05:38:46.301Z\",\"params\":{\"resmsgid\":\"7224a4d0-cb14-11ea-9313-0912071b8abe\",\"msgid\":\"722281f0-cb14-11ea-9313-0912071b8abe\",\"status\":\"successful\",\"err\":null,\"errmsg\":null},\"responseCode\":\"OK\",\"result\":{\"content\":{\"ownershipType\":[\"createdBy\"],\"code\":\"test.res.1\",\"channel\":\"in.ekstep\",\"language\":[\"English\"],\"mediaType\":\"content\",\"osId\":\"org.ekstep.quiz.app\",\"languageCode\":[\"en\"],\"version\":2,\"versionKey\":\"1591949601174\",\"license\":\"CC BY 4.0\",\"idealScreenDensity\":\"hdpi\",\"framework\":\"NCFCOPY\",\"s3Key\":\"content/do_113041248230580224116/artifact/validecml_1591949596304.zip\",\"createdBy\":\"95e4942d-cbe8-477d-aebd-ad8e6de4bfc8\",\"compatibilityLevel\":1,\"name\":\"Resource Content 1\",\"status\":\"Draft\",\"level1Concept\":[\"Addition\"],\"level1Name\":[\"Math-Magic\"],\"textbook_name\":[\"How Many Times?\"],\"sourceURL\":\"https://diksha.gov.in/play/content/do_30030488\",\"source\":[\"Diksha 1\"]}}}";
    private String postReqStage1Resp = "{\"id\":\"api.daggit\",\"params\":{\"err\":\"null\",\"errmsg\":\"Dag Initialization failed\",\"msgid\":\"\",\"resmsgid\":\"null\",\"status\":\"success\"},\"responseCode\":\"OK\",\"result\":{\"execution_date\":\"2020-07-08\",\"experiment_name\":\"Content_tagging_20200708-141923\",\"status\":200},\"ts\":\"2020-07-08 14:19:23:1594198163\",\"ver\":\"v1\"}";
    private String postReqStage2Resp = "{\"ets\":\"2020-07-14 15:27:23:1594720643\",\"id\":\"api.ml.vector\",\"params\":{\"err\":\"null\",\"errmsg\":\"null\",\"msgid\":\"\",\"resmsgid\":\"null\",\"status\":\"success\"},\"result\":{\"action\":\"get_BERT_embedding\",\"vector\":[[]]}}";
    private Config configMock;

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);
        configMock = mock(Config.class);
        stub(configMock.get("nested.fields")).toReturn("badgeAssertions,targets,badgeAssociations,plugins,me_totalTimeSpent,me_totalPlaySessionCount,me_totalTimeSpentInSec,batches");
    }

    @Test
    public void testInsertToCassandraForStage1() throws Exception  {
       PowerMockito.mockStatic(HTTPUtil.class);
        when(HTTPUtil.makeGetRequest(Mockito.anyString())).thenReturn(getResp);
        when(HTTPUtil.makePostRequest(Mockito.anyString(),Mockito.anyString())).thenReturn(postReqStage1Resp);
        PowerMockito.mockStatic(CassandraConnector.class);
        PowerMockito.doNothing().when(CassandraConnector.class);
        CassandraConnector.updateContentProperties(Mockito.anyString(),Mockito.anyMap());
        MVCProcessorCassandraIndexer cassandraManager = new MVCProcessorCassandraIndexer();
        cassandraManager.insertintoCassandra(getEvent(eventData),uniqueId);
    }
    @Test
    public void testInsertToCassandraForStage2() throws Exception  {
        PowerMockito.mockStatic(HTTPUtil.class);
        when(HTTPUtil.makePostRequest(Mockito.anyString(),Mockito.anyString())).thenReturn(postReqStage2Resp);
        PowerMockito.mockStatic(CassandraConnector.class);
        PowerMockito.doNothing().when(CassandraConnector.class);
        CassandraConnector.updateContentProperties(Mockito.anyString(),Mockito.anyMap());
        MVCProcessorCassandraIndexer cassandraManager = new MVCProcessorCassandraIndexer();
        cassandraManager.insertintoCassandra(getEvent(eventData2),uniqueId);
    }
    @Test
    public void testInsertToCassandraForStage3() throws Exception  {
        PowerMockito.mockStatic(CassandraConnector.class);
        PowerMockito.doNothing().when(CassandraConnector.class);
        CassandraConnector.updateContentProperties(Mockito.anyString(),Mockito.anyMap());
        MVCProcessorCassandraIndexer cassandraManager = new MVCProcessorCassandraIndexer();
        cassandraManager.insertintoCassandra(getEvent(eventData3),uniqueId);
    }

    public  Map<String, Object> getEvent(String message) throws IOException {
        return  new Gson().fromJson(message, Map.class);
    }

}
