package org.sunbird.mvcjobs.samza.test;

import com.google.gson.Gson;
import org.sunbird.mvcjobs.samza.service.util.ContentUtil;
import org.sunbird.searchindex.util.HTTPUtil;
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
@PrepareForTest({HTTPUtil.class})
@PowerMockIgnore({"javax.management.*", "sun.security.ssl.*", "javax.net.ssl.*" , "javax.crypto.*"})
public class ContentUtilTest {
        private Config configMock;
    private String getResp = "{\"id\":\"api.content.read\",\"ver\":\"1.0\",\"ts\":\"2020-07-21T05:38:46.301Z\",\"params\":{\"resmsgid\":\"7224a4d0-cb14-11ea-9313-0912071b8abe\",\"msgid\":\"722281f0-cb14-11ea-9313-0912071b8abe\",\"status\":\"successful\",\"err\":null,\"errmsg\":null},\"responseCode\":\"OK\",\"result\":{\"content\":{\"ownershipType\":[\"createdBy\"],\"code\":\"test.res.1\",\"channel\":\"in.ekstep\",\"language\":[\"English\"],\"mediaType\":\"content\",\"osId\":\"org.sunbird.quiz.app\",\"languageCode\":[\"en\"],\"version\":2,\"versionKey\":\"1591949601174\",\"license\":\"CC BY 4.0\",\"idealScreenDensity\":\"hdpi\",\"framework\":\"NCFCOPY\",\"s3Key\":\"content/do_113041248230580224116/artifact/validecml_1591949596304.zip\",\"createdBy\":\"95e4942d-cbe8-477d-aebd-ad8e6de4bfc8\",\"compatibilityLevel\":1,\"name\":\"Resource Content 1\",\"status\":\"Draft\",\"level1Concept\":[\"Addition\"],\"level1Name\":[\"Math-Magic\"],\"textbook_name\":[\"How Many Times?\"],\"sourceURL\":\"https://diksha.gov.in/play/content/do_30030488\",\"source\":[\"Diksha 1\"]}}}";
    private String eventData = "{\"identifier\":\"do_113041248230580224116\",\"action\":\"update-es-index\",\"stage\":1}";
    private String uniqueId = "do_113041248230580224116";
        @Before
        public void setup(){
        MockitoAnnotations.initMocks(this);
        configMock = mock(Config.class);
        stub(configMock.get("nested.fields")).toReturn("badgeAssertions,targets,badgeAssociations,plugins,me_totalTimeSpent,me_totalPlaySessionCount,me_totalTimeSpentInSec,batches");
        }

        @Test
        public void getContentMetaData()throws Exception {
            PowerMockito.mockStatic(HTTPUtil.class);
            when(HTTPUtil.makeGetRequest(Mockito.anyString())).thenReturn(getResp);
            ContentUtil.getContentMetaData(getEvent(eventData),uniqueId);
        }

    public  Map<String, Object> getEvent(String message) throws IOException {
        return  new Gson().fromJson(message, Map.class);
    }
}

