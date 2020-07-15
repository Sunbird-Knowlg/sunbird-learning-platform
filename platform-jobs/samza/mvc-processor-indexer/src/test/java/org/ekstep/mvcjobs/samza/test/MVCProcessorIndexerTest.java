package org.ekstep.mvcjobs.samza.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.MapUtils;
import org.ekstep.mvcjobs.samza.service.util.MVCProcessorIndexer;
import org.ekstep.mvcsearchindex.elasticsearch.ElasticSearchUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ElasticSearchUtil.class)
public class MVCProcessorIndexerTest {
     MVCProcessorIndexer mvcProcessorIndexer = new MVCProcessorIndexer();
    ObjectMapper mapper = new ObjectMapper();
    private String uniqueId = "do_113041248230580224116";
    private String eventData = "{\"eventData\":{\"identifier\":\"do_113041248230580224116\",\"action\":\"update-es-index\",\"stage\":1}}";
    private String doc = "{\"eid\":\"MVC_JOB_PROCESSOR\",\"ets\":1591603456223,\"mid\":\"LP.1591603456223.a5d1c6f0-a95e-11ea-b80d-b75468d19fe4\",\"actor\":{\"id\":\"UPDATE ES INDEX\",\"type\":\"System\"},\"context\":{\"pdata\":{\"ver\":\"1.0\",\"id\":\"org.ekstep.platform\"},\"channel\":\"01285019302823526477\"},\"object\":{\"ver\":\"1.0\",\"id\":\"do_113041248230580224116\"},\"eventData\":{\"identifier\":\"do_113041248230580224116\",\"action\":\"update-es-index\",\"stage\":1}}";
  @Test
    public void testUpsertDocument() throws Exception {
        PowerMockito.mock(ElasticSearchUtil.class);
        PowerMockito.doNothing().when(ElasticSearchUtil.class);
        ElasticSearchUtil.addDocumentWithId(Mockito.anyString(),Mockito.anyString(),Mockito.anyString());
        mvcProcessorIndexer.upsertDocument(uniqueId,getEvent(doc));
}
     @Test
     public void testRemoveExtraParams() throws IOException {
         Map<String, Object> message = mvcProcessorIndexer.removeExtraParams(getEvent(eventData));
         Assert.assertTrue(MapUtils.isNotEmpty(message));
    }
    public  Map<String, Object> getEvent(String message) throws IOException {
       return mapper.readValue(message,Map.class);
    }
}
