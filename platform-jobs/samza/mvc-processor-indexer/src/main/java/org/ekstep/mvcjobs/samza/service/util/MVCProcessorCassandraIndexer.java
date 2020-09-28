package org.ekstep.mvcjobs.samza.service.util;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.searchindex.util.HTTPUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class MVCProcessorCassandraIndexer  {
    String  mlworkbenchapirequest = "", mlvectorListRequest = "" , jobname = ""   ;
    Map<String,Object> mapStage1 = new HashMap<>();
    List<String> level1concept = null,level2concept  = null, level3concept = null , textbook_name , level1_name , level2_name , level3_name ;
    private JobLogger LOGGER = new JobLogger(MVCProcessorCassandraIndexer.class);
    public MVCProcessorCassandraIndexer() {
        mlworkbenchapirequest = "{\"request\":{ \"input\" :{ \"content\" : [] } } }";
        mlvectorListRequest = "{\"request\":{\"text\":[],\"cid\": \"\",\"language\":\"en\",\"method\":\"BERT\",\"params\":{\"dim\":768,\"seq_len\":25}}}";
        jobname = "vidyadaan_content_keyword_tagging";

    }
    // Insert to cassandra
    public  void insertIntoCassandra(Map<String,Object> obj, String identifier) throws Exception {
        String action = obj.get("action").toString();

        if(StringUtils.isNotBlank(action)) {
            if(action.equalsIgnoreCase("update-es-index")) {
                LOGGER.info("MVCProcessorCassandraIndexer :: getContentMetaData ::: extracting required fields" + obj);
                extractFieldsToBeInserted(obj);
                LOGGER.info("MVCProcessorCassandraIndexer :: getContentMetaData ::: making ml workbench api request");
                getMLKeywords(obj);
                LOGGER.info("MVCProcessorCassandraIndexer :: insertIntoCassandra ::: update-es-index-1 event");
                LOGGER.info("MVCProcessorCassandraIndexer :: insertIntoCassandra ::: Inserting into cassandra stage-1");
                CassandraConnector.updateContentProperties(identifier,mapStage1);
            } else if(action.equalsIgnoreCase("update-ml-keywords")) {
                LOGGER.info("MVCProcessorCassandraIndexer :: insertIntoCassandra ::: update-ml-keywords");
                 String ml_contentText;
                 List<String> ml_Keywords;
                 ml_contentText = obj.get("ml_contentText") != null ? obj.get("ml_contentText").toString() : null;
                 ml_Keywords = obj.get("ml_Keywords") != null ? (List<String>) obj.get("ml_Keywords") : null;

                getMLVectors(ml_contentText,identifier);
                Map<String,Object> mapForStage2 = new HashMap<>();
                mapForStage2.put("ml_keywords",ml_Keywords);
                mapForStage2.put("ml_content_text",ml_contentText);
                CassandraConnector.updateContentProperties(identifier,mapForStage2);

            }
            else  if(action.equalsIgnoreCase("update-ml-contenttextvector")) {
                LOGGER.info("MVCProcessorCassandraIndexer :: insertIntoCassandra ::: update-ml-contenttextvector event");
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
    }

    //Getting Fields to be inserted into cassandra
    private void extractFieldsToBeInserted(Map<String,Object> contentobj) {
        if(contentobj.containsKey("level1Concept")){
            level1concept = (List<String>)contentobj.get("level1Concept");
            mapStage1.put("level1_concept", level1concept);
        }
        if(contentobj.containsKey("level2Concept")){
            level2concept = (List<String>)contentobj.get("level2Concept");
            mapStage1.put("level2_concept", level2concept);
        }
        if(contentobj.containsKey("level3Concept")){
            level3concept = (List<String>)contentobj.get("level3Concept");
            mapStage1.put("level3_concept",level3concept );
        }
        if(contentobj.containsKey("textbook_name")){
            textbook_name = (List<String>)contentobj.get("textbook_name");
            mapStage1.put("textbook_name", textbook_name);
        }
        if(contentobj.containsKey("level1Name")){
            level1_name = (List<String>)contentobj.get("level1Name");
            mapStage1.put("level1_name", level1_name);
        }
        if(contentobj.containsKey("level2Name")){
            level2_name = (List<String>)contentobj.get("level2Name");
            mapStage1.put("level2_name", level2_name);
        }
        if(contentobj.containsKey("level3Name")){
            level3_name = (List<String>)contentobj.get("level3Name");
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
    void getMLKeywords(Map<String, Object> contentdef) throws Exception {
        JSONObject obj = new JSONObject(mlworkbenchapirequest);
        JSONObject req = ((JSONObject) (obj.get("request")));
        JSONObject input = (JSONObject) req.get("input");
        JSONArray content = (JSONArray) input.get("content");
        content.put(contentdef);
        req.put("job", jobname);
        LOGGER.info("MVCProcessorCassandraIndexer :: getMLKeywords  ::: The ML workbench URL is " + "http://" + Platform.config.getString("ml.keyword.api") + ":3579/daggit/submit");

        try {
            String resp = HTTPUtil.makePostRequest("http://" + Platform.config.getString("ml.keyword.api") + ":3579/daggit/submit", obj.toString());
            LOGGER.info("MVCProcessorCassandraIndexer :: getMLKeywords  ::: The ML workbench response is " + resp);

        } catch (Exception e) {
            LOGGER.info("MVCProcessorCassandraIndexer :: getMLKeywords  ::: ML workbench api request failed ");
        }

    }


    // Post reqeuest for vector api
    public void getMLVectors(String contentText, String identifier) throws Exception {
        String mlVectorApi = Platform.config.hasPath("ml.vector.api") ? Platform.config.getString("ml.vector.api") : "";
        JSONObject obj = new JSONObject(mlvectorListRequest);
        JSONObject req = ((JSONObject) (obj.get("request")));
        JSONArray text = (JSONArray) req.get("text");
        req.put("cid", identifier);
        text.put(contentText);
        LOGGER.info("MVCProcessorCassandraIndexer :: getMLVectors  ::: The ML vector URL is " + "http://" + mlVectorApi + ":1729/ml/vector/ContentText");

        try {
            String resp = HTTPUtil.makePostRequest("http://" + mlVectorApi + ":1729/ml/vector/ContentText", obj.toString());
            LOGGER.info("MVCProcessorCassandraIndexer :: getMLVectors  ::: ML vector api request response is " + resp);
        } catch (Exception e) {
            LOGGER.info("MVCProcessorCassandraIndexer :: getMLVectors  ::: ML vector api request failed ");
        }
    }

}
