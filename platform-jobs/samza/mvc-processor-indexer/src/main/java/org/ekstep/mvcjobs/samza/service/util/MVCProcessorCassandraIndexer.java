package org.ekstep.mvcjobs.samza.service.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.searchindex.util.HTTPUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class MVCProcessorCassandraIndexer  {
    ObjectMapper mapper = new ObjectMapper();
    String elasticSearchParamArr[] = {"organisation","channel","framework","board","medium","subject","gradeLevel","name","description","language","appId","appIcon","appIconLabel","contentEncoding","identifier","node_id","nodeType","mimeType","resourceType","contentType","allowedContentTypes","objectType","posterImage","artifactUrl","launchUrl","previewUrl","streamingUrl","downloadUrl","status","pkgVersion","source","lastUpdatedOn","ml_contentText","ml_contentTextVector","ml_Keywords","level1Name","level1Concept","level2Name","level2Concept","level3Name","level3Concept","textbook_name","sourceURL","label","all_fields"};;
    String contentreadapiurl = "", mlworkbenchapirequest = "", mlvectorListRequest = "" , jobname = "" , mlvectorapi = ""  ;
    Map<String,Object> mapStage1 = new HashMap<>();
    List<String> level1concept = null,level2concept  = null, level3concept = null , textbook_name , level1_name , level2_name , level3_name ;
    private JobLogger LOGGER = new JobLogger(MVCProcessorCassandraIndexer.class);
    public MVCProcessorCassandraIndexer() {
        mlworkbenchapirequest = "{\"request\":{ \"input\" :{ \"content\" : [] } } }";
        mlvectorListRequest = "{\"request\":{\"text\":[],\"cid\": \"\",\"language\":\"en\",\"method\":\"BERT\",\"params\":{\"dim\":768,\"seq_len\":25}}}";
        jobname = "vidyadaan_content_keyword_tagging";
        mlvectorapi = "http://127.0.0.1:1729/ml/vector/search";

    }
    // Insert to cassandra
    public  Map<String,Object> insertintoCassandra(Map<String,Object> obj, String identifier) throws Exception {
        String action = obj.get("action").toString();

        if(StringUtils.isNotBlank(action)) {
            if(action.equalsIgnoreCase("update-es-index")) {
                LOGGER.info("MVCProcessorCassandraIndexer :: insertintoCassandra ::: update-es-index-1 event");
                obj = getContentMetaData(obj ,identifier);
                LOGGER.info("MVCProcessorCassandraIndexer :: insertintoCassandra ::: Inserting into cassandra stage-1");
                CassandraConnector.updateContentProperties(identifier,mapStage1);
                mapStage1 = null;
            } else if(action.equalsIgnoreCase("update-ml-keywords")) {
                LOGGER.info("MVCProcessorCassandraIndexer :: insertintoCassandra ::: update-ml-keywords");
                 String ml_contentText;
                 List<String> ml_Keywords;
                 ml_contentText = obj.get("ml_contentText") != null ? obj.get("ml_contentText").toString() : null;
                 ml_Keywords = obj.get("ml_Keywords") != null ? (List<String>) obj.get("ml_Keywords") : null;

                makepostreqForVectorApi(ml_contentText,identifier);
                Map<String,Object> mapForStage2 = new HashMap<>();
                mapForStage2.put("ml_keywords",ml_Keywords);
                mapForStage2.put("ml_content_text",ml_contentText);

                CassandraConnector.updateContentProperties(identifier,mapForStage2);
            }
            else  if(action.equalsIgnoreCase("update-ml-contenttextvector")) {
                LOGGER.info("MVCProcessorCassandraIndexer :: insertintoCassandra ::: update-ml-contenttextvector event");
                 List<List<Double>> ml_contentTextVectorList;
                 Set<Double> ml_contentTextVector = null;
              ml_contentTextVectorList = obj.get("ml_contentTextVector") != null ? (List<List<Double>>) obj.get("ml_contentTextVector") : null;
                if(ml_contentTextVectorList != null)
                {
                    ml_contentTextVector = new HashSet<Double>(ml_contentTextVectorList.get(0));

                }
                Map<String,Object> mapForStage3 = new HashMap<>();
                mapForStage3.put("ml_content_text_vector",ml_contentTextVector);
                CassandraConnector.updateContentProperties(identifier,mapForStage3);

            }
        }
        LOGGER.info("INSERTION SUCCESSFULL IN CASSANDRA");
        return obj;
    }

    Map<String,Object> getContentMetaData(Map<String,Object> newmap , String identifer) throws Exception {
        try {
            contentreadapiurl = "https://dock.sunbirded.org/api/content/v1/read/";
            LOGGER.info("MVCProcessorCassandraIndexer :: getContentMetaData :::  Making API call to read content " + contentreadapiurl);
            String content = HTTPUtil.makeGetRequest(contentreadapiurl+identifer);
            LOGGER.info("MVCProcessorCassandraIndexer :: getContentMetaData ::: retrieved content meta " + content);
            Map<String,Object> obj = mapper.readValue(content,Map.class);
            Map<String,Object> contentobj = (HashMap<String,Object>) (((HashMap<String,Object>)obj.get("result")).get("content"));

            extractFieldsToBeInserted(contentobj);
            makepostreqForMlAPI(contentobj);
            newmap = filterData(newmap,contentobj);

        }catch (Exception e) {
            LOGGER.info("MVCProcessorCassandraIndexer :: getContentDefinition ::: Error in getContentDefinitionFunction " + e.getMessage());
            throw new Exception("Get content metdata failed");
        }
        return newmap;
    }
    //Getting Fields to be inserted into cassandra
    private void extractFieldsToBeInserted(Map<String,Object> contentobj) {
        if(contentobj.containsKey("level1Concept")){
            level1concept = (List<String>)contentobj.get("level1Name");
            mapStage1.put("level1_concept", level1concept);
        }
        if(contentobj.containsKey("level2Concept")){
            level2concept = (List<String>)contentobj.get("level1Name");
            mapStage1.put("level2_concept", level2concept);
        }
        if(contentobj.containsKey("level3Concept")){
            level3concept = (List<String>)contentobj.get("level1Name");
            mapStage1.put("level3_concept",level3concept );
        }
        if(contentobj.containsKey("textbook_name")){
            textbook_name = (List<String>)contentobj.get("level1Name");
            mapStage1.put("textbook_name", textbook_name);
        }
        if(contentobj.containsKey("level1Name")){
            level1_name = (List<String>)contentobj.get("level1Name");
            mapStage1.put("level1_name", level1_name);
        }
        if(contentobj.containsKey("level2Name")){
            level2_name = (List<String>)contentobj.get("level1Name");
            mapStage1.put("level2_name", level2_name);
        }
        if(contentobj.containsKey("level3Name")){
            level3_name = (List<String>)contentobj.get("level1Name");
            mapStage1.put("level3_name", level3_name);
        }
        if(contentobj.containsKey("source")){
            mapStage1.put("source",contentobj.get("source"));
        }
        if(contentobj.containsKey("sourceURL")){
            mapStage1.put("sourceurl",contentobj.get("sourceURL"));
        }
        LOGGER.info("MVCProcessorCassandraIndexer :: extractedmetadata");

    }

    // POST reqeuest for ml keywords api
    void makepostreqForMlAPI(Map<String,Object> contentdef) throws Exception {
        try {

            JSONObject obj = new JSONObject(mlworkbenchapirequest);
            JSONObject req = ((JSONObject)(obj.get("request")));
            JSONObject input = (JSONObject)req.get("input");
            JSONArray content = (JSONArray)input.get("content");
            content.put(contentdef);
            req.put("job",jobname);
            LOGGER.info("MVCProcessorCassandraIndexer :: makepostreqForMlAPI  ::: The ML workbench URL is " + "http://"+Platform.config.getString("mlkeywordapi") + "/daggit/submit" );
            String resp = HTTPUtil.makePostRequest("http://"+Platform.config.getString("mlkeywordapi") + "/daggit/submit",obj.toString());
            LOGGER.info("MVCProcessorCassandraIndexer :: makepostreqForMlAPI  ::: The ML workbench response is " + resp );
        }
        catch (Exception e) {
            LOGGER.info("MVCProcessorCassandraIndexer :: makepostreqForMlAPI  ::: ML workbench api request failed ");
            throw new Exception("Ml keyword api failed");
        }
    }

    // Filter the params of content  to add in ES
    public  Map<String,Object> filterData(Map<String,Object> obj ,Map<String,Object> content) {

        String key = null;
        Object value = null;
        for(int i = 0 ; i < elasticSearchParamArr.length ; i++ ) {
            key = (elasticSearchParamArr[i]);
            value = content.containsKey(key)  ? content.get(key) : null;
            if(value != null) {
                obj.put(key,value);
                value = null;
            }
        }
        return obj;

    }

    // Post reqeuest for vector api
    public  void makepostreqForVectorApi(String contentText,String identifier) throws Exception {
        try {
            JSONObject obj = new JSONObject(mlworkbenchapirequest);
            JSONObject req = ((JSONObject) (obj.get("request")));
            req.put("cid",identifier);
            req.put("text",contentText);
            String resp = HTTPUtil.makePostRequest(mlvectorapi,obj.toString());
        }
        catch (Exception e) {
           System.out.println(e);
           throw new Exception("ML vector api failed");
        }
    }

}
