
package org.ekstep.mvcjobs.samza.test;

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.ekstep.mvcjobs.samza.service.util.MVCProcessorESIndexer;
import org.ekstep.mvcsearchindex.elasticsearch.ElasticSearchUtil;
import static org.junit.Assert.assertTrue;

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
@PrepareForTest({ElasticSearchUtil.class, Config.class, MVCProcessorESIndexer.class})
@PowerMockIgnore({"javax.management.*", "sun.security.ssl.*", "javax.net.ssl.*" , "javax.crypto.*"})
public class MVCProcessorESIndexerTest {
    private String uniqueId = "do_113041248230580224116";
    private String eventDataNewDoc = "{\"identifier\":\"do_113041248230580224116\",\"action\":\"update-es-index\",\"stage\":1}";
    private String eventDataMlKeywords = "{\"action\":\"update-ml-keywords\",\"stage\":\"2\",\"ml_Keywords\":[\"maths\",\"addition\",\"add\"],\"ml_contentText\":\"This is the content text for addition of two numbers.\"}";
    private String eventDataContentRating = "{\"action\":\"update-content-rating\",\"stage\":4,\"metadata\":{\"me_averageRating\":\"1\",\"me_total_time_spent_in_app\":\"2\",\"me_total_time_spent_in_portal\":\"3\",\"me_total_time_spent_in_desktop\":\"4\",\"me_total_play_sessions_in_app\":\"5\",\"me_total_play_sessions_in_portal\":\"6\",\"me_total_play_sessions_in_desktop\":\"7\"}}";
    private String eventDataContentTextVector = "{\"action\":\"update-ml-contenttextvector\",\"stage\":3,\"ml_contentTextVector\":[[1.1,2,7.4,68]]}";
    private Config configMock;
    private MVCProcessorESIndexer mvcProcessorESIndexer = new MVCProcessorESIndexer();

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);
        configMock = mock(Config.class);
        stub(configMock.get("nested.fields")).toReturn("badgeAssertions,targets,badgeAssociations,plugins,me_totalTimeSpent,me_totalPlaySessionCount,me_totalTimeSpentInSec,batches");
        PowerMockito.mockStatic(ElasticSearchUtil.class);
        PowerMockito.doNothing().when(ElasticSearchUtil.class);
    }

    @Test
    public void testUpsertDocumentCaseUpdateEsIndex() throws Exception {
        ElasticSearchUtil.addDocumentWithId(Mockito.anyString(),Mockito.anyString(),Mockito.anyString());
        mvcProcessorESIndexer.upsertDocument(uniqueId,getEvent(eventDataNewDoc));
        when(ElasticSearchUtil.getDocumentAsStringById(Mockito.anyString(),Mockito.anyString())).thenReturn(uniqueId);
        String doc = ElasticSearchUtil.getDocumentAsStringById(Mockito.anyString(),Mockito.anyString());
        assertTrue(StringUtils.contains(doc, uniqueId));
    }

    @Test
    public void testUpsertDocumentUpdateMlKeywords() throws Exception {
        ElasticSearchUtil.updateDocument(Mockito.anyString(),Mockito.anyString(),Mockito.anyString());
        mvcProcessorESIndexer.upsertDocument(uniqueId,getEvent(eventDataMlKeywords));
        when(ElasticSearchUtil.getDocumentAsStringById(Mockito.anyString(),Mockito.anyString())).thenReturn(uniqueId);
        String doc = ElasticSearchUtil.getDocumentAsStringById(Mockito.anyString(),Mockito.anyString());
        assertTrue(StringUtils.contains(doc, uniqueId));
    }

    @Test
    public void testUpsertDocumentUpdateMlContentTextVector() throws Exception {
        ElasticSearchUtil.updateDocument(Mockito.anyString(),Mockito.anyString(),Mockito.anyString());
        mvcProcessorESIndexer.upsertDocument(uniqueId,getEvent(eventDataContentTextVector));
        when(ElasticSearchUtil.getDocumentAsStringById(Mockito.anyString(),Mockito.anyString())).thenReturn(uniqueId);
        String doc = ElasticSearchUtil.getDocumentAsStringById(Mockito.anyString(),Mockito.anyString());
        assertTrue(StringUtils.contains(doc, uniqueId));
    }

    @Test
    public void testUpsertDocumentUpdateContentRating() throws Exception {
        ElasticSearchUtil.updateDocument(Mockito.anyString(),Mockito.anyString(),Mockito.anyString());
        when(ElasticSearchUtil.getDocumentAsStringById(Mockito.anyString(),Mockito.anyString())).thenReturn(uniqueId);
        mvcProcessorESIndexer.upsertDocument(uniqueId,getEvent(eventDataContentRating));
        String doc = ElasticSearchUtil.getDocumentAsStringById(Mockito.anyString(),Mockito.anyString());
        assertTrue(StringUtils.contains(doc, uniqueId));
    }


    public  Map<String, Object> getEvent(String message) throws IOException {
        return  new Gson().fromJson(message, Map.class);
    }

}

