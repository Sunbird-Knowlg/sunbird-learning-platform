package org.ekstep.mvcjobs.samza.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.MapUtils;
import org.ekstep.mvcjobs.samza.service.util.MVCProcessorIndexer;
import org.ekstep.mvcsearchindex.elasticsearch.ElasticSearchUtil;
import org.junit.Assert;
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

import static org.mockito.Mockito.stub;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ElasticSearchUtil.class, Config.class, MVCProcessorIndexer.class})
@PowerMockIgnore({"javax.management.*", "sun.security.ssl.*", "javax.net.ssl.*" , "javax.crypto.*"})
public class MVCProcessorIndexerTest {
    ObjectMapper mapper = new ObjectMapper();
    private String uniqueId = "do_113041248230580224116";
    private String eventData = "{\"identifier\":\"do_113041248230580224116\",\"action\":\"update-es-index\",\"stage\":1}";
    private Config configMock;
    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);
        configMock = Mockito.mock(Config.class);
        stub(configMock.get("nested.fields")).toReturn("badgeAssertions,targets,badgeAssociations,plugins,me_totalTimeSpent,me_totalPlaySessionCount,me_totalTimeSpentInSec,batches");
    }

    @Test
    public void testUpsertDocument() throws Exception {
        PowerMockito.mockStatic(ElasticSearchUtil.class);
        PowerMockito.doNothing().when(ElasticSearchUtil.class);
        ElasticSearchUtil.addDocumentWithId(Mockito.anyString(),Mockito.anyString(),Mockito.anyString());
        MVCProcessorIndexer mvcProcessorIndexer = new MVCProcessorIndexer();
        mvcProcessorIndexer.upsertDocument(uniqueId,getEvent(eventData));
}
     @Test
     public void testRemoveExtraParams() throws IOException {
         MVCProcessorIndexer mvcProcessorIndexer = new MVCProcessorIndexer();
         Map<String, Object> message = mvcProcessorIndexer.removeExtraParams(getEvent(eventData));
         Assert.assertTrue(MapUtils.isNotEmpty(message));
    }
    public  Map<String, Object> getEvent(String message) throws IOException {
       return mapper.readValue(message,Map.class);
    }
}
