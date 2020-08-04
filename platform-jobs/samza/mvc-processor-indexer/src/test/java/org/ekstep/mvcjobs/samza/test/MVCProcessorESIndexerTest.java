package org.ekstep.mvcjobs.samza.test;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.common.Platform;
import org.ekstep.mvcjobs.samza.service.util.MVCProcessorESIndexer;
import org.ekstep.mvcsearchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class MVCProcessorESIndexerTest  {
    private String uniqueId = "do_113041248230580224116";
    private String eventDataStage1 = "{\"identifier\":\"do_113041248230580224116\",\"action\":\"update-es-index\",\"stage\":1}";
    private String eventDataStage2 = "{\"action\":\"update-ml-keywords\",\"stage\":\"2\",\"ml_Keywords\":[\"maths\",\"addition\",\"add\"],\"ml_contentText\":\"This is the content text for addition of two numbers.\"}";
    private String eventData = "{\"identifier\":\"do_113041248230580224116\"}";

    protected static ObjectMapper mapper = new ObjectMapper();

    @BeforeClass
    public static void beforeTest() throws Exception {
        createMVCSearchIndex();
        Thread.sleep(3000);
    }

    @AfterClass
    public static void afterTest() throws Exception {
        System.out.println("deleting index: " + CompositeSearchConstants.MVC_SEARCH_INDEX);
        ElasticSearchUtil.deleteIndex(CompositeSearchConstants.MVC_SEARCH_INDEX);
    }

    protected static void createMVCSearchIndex() throws Exception {
        CompositeSearchConstants.MVC_SEARCH_INDEX = "testmvcindex";
        ElasticSearchUtil.initialiseESClient(CompositeSearchConstants.MVC_SEARCH_INDEX,
                Platform.config.getString("search.es_conn_info"));
        System.out.println("creating index: " + CompositeSearchConstants.MVC_SEARCH_INDEX);
        String settings = "";
        String mappings = "";
        String esindex = "{\"aliases\":{\"mvc-content\":{}},\"settings\":{\"index\":{\"max_ngram_diff\":\"29\",\"mapping\":{\"total_fields\":{\"limit\":\"1500\"}},\"number_of_shards\":\"5\",\"analysis\":{\"filter\":{\"mynGram\":{\"token_chars\":[\"letter\",\"digit\",\"whitespace\",\"punctuation\",\"symbol\"],\"min_gram\":\"1\",\"type\":\"nGram\",\"max_gram\":\"30\"}},\"analyzer\":{\"cs_index_analyzer\":{\"filter\":[\"lowercase\",\"mynGram\"],\"type\":\"custom\",\"tokenizer\":\"standard\"},\"keylower\":{\"filter\":\"lowercase\",\"tokenizer\":\"keyword\"},\"cs_search_analyzer\":{\"filter\":[\"lowercase\"],\"type\":\"custom\",\"tokenizer\":\"standard\"},\"ml_custom_analyzer\":{\"type\":\"standard\",\"stopwords\":[\"_english_\",\"_hindi_\"]}}},\"number_of_replicas\":\"1\"}},\"mappings\":{\"dynamic\":\"strict\",\"properties\":{\"organisation\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"channel\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"framework\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"board\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"medium\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"subject\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"gradeLevel\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"name\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"description\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"language\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"appId\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"appIcon\":{\"type\":\"text\",\"index\":false},\"appIconLabel\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"contentEncoding\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"identifier\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"node_id\":{\"type\":\"long\",\"fields\":{\"raw\":{\"type\":\"long\"}}},\"nodeType\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"mimeType\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"resourceType\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"contentType\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"allowedContentTypes\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"objectType\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"posterImage\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"artifactUrl\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"launchUrl\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"previewUrl\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"streamingUrl\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"downloadUrl\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"status\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"pkgVersion\":{\"type\":\"double\",\"fields\":{\"raw\":{\"type\":\"double\"}}},\"lastUpdatedOn\":{\"type\":\"date\",\"fields\":{\"raw\":{\"type\":\"date\"}}},\"textbook_name\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"level1Name\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"level1Concept\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"level2Name\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"level2Concept\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"level3Name\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"level3Concept\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"source\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"sourceURL\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"copy_to\":[\"all_fields\"],\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"},\"ml_Keywords\":{\"type\":\"text\",\"analyzer\":\"ml_custom_analyzer\",\"search_analyzer\":\"standard\"},\"ml_contentText\":{\"type\":\"text\",\"analyzer\":\"ml_custom_analyzer\",\"search_analyzer\":\"standard\"},\"ml_contentTextVector\":{\"type\":\"dense_vector\",\"dims\":768},\"label\":{\"type\":\"text\",\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"standard\"},\"all_fields\":{\"type\":\"text\",\"fields\":{\"raw\":{\"type\":\"text\",\"analyzer\":\"keylower\",\"fielddata\":true}},\"analyzer\":\"cs_index_analyzer\",\"search_analyzer\":\"cs_search_analyzer\"}}}}";
        ElasticSearchUtil.addIndex(CompositeSearchConstants.MVC_SEARCH_INDEX,
                CompositeSearchConstants.MVC_SEARCH_INDEX_TYPE, settings, mappings,null , esindex);
    }
    @Test
    public void testUpsertDocumentAdd() throws Exception {
        MVCProcessorESIndexer mvcProcessorESIndexer = new MVCProcessorESIndexer();
        mvcProcessorESIndexer.upsertDocument(uniqueId,getEvent(eventDataStage1));
        String doc = ElasticSearchUtil.getDocumentAsStringById(CompositeSearchConstants.MVC_SEARCH_INDEX, uniqueId);
        assertTrue(StringUtils.contains(doc, uniqueId));
}
    @Test
    public void testUpsertDocumentUpdate() throws Exception {
        ElasticSearchUtil.addDocumentWithId(CompositeSearchConstants.MVC_SEARCH_INDEX,
                uniqueId, eventData);
        MVCProcessorESIndexer mvcProcessorESIndexer = new MVCProcessorESIndexer();
        mvcProcessorESIndexer.upsertDocument(uniqueId,getEvent(eventDataStage2));
        String doc = ElasticSearchUtil.getDocumentAsStringById(CompositeSearchConstants.MVC_SEARCH_INDEX, uniqueId);
        assertTrue(StringUtils.contains(doc, uniqueId));
    }



    public  Map<String, Object> getEvent(String message) throws IOException {
      return  mapper.readValue(message, Map.class);
    }





}
