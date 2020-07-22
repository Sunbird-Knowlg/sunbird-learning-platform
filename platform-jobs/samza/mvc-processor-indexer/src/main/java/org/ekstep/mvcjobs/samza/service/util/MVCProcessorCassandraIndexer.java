package org.ekstep.mvcjobs.samza.service.util;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.searchindex.util.HTTPUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class MVCProcessorCassandraIndexer  {
    String arr[] = {"organisation","channel","framework","board","medium","subject","gradeLevel","name","description","language","appId","appIcon","appIconLabel","contentEncoding","identifier","node_id","nodeType","mimeType","resourceType","contentType","allowedContentTypes","objectType","posterImage","artifactUrl","launchUrl","previewUrl","streamingUrl","downloadUrl","status","pkgVersion","source","lastUpdatedOn","ml_contentText","ml_contentTextVector","ml_Keywords","level1Name","level1Concept","level2Name","level2Concept","level3Name","level3Concept","textbook_name","sourceURL","label","all_fields"};;
    String contentreadapi = "", mlworkbenchapirequest = "", mlvectorListRequest = "" , jobname = "" , mlkeywordapi = "" , mlvectorapi = ""  ;
    Map<String,Object> mapStage1 = new HashMap<>();
  public MVCProcessorCassandraIndexer() {
        contentreadapi = Platform.config.getString("kp.content_service.base_url") + "/content/v3/read/";
        mlworkbenchapirequest = "{\"request\":{ \"input\" :{ \"content\" : [] } } }";
        mlvectorListRequest = "{\"request\":{\"text\":[],\"cid\": \"\",\"language\":\"en\",\"method\":\"BERT\",\"params\":{\"dim\":768,\"seq_len\":25}}}";
        jobname = "vidyadaan_content_keyword_tagging";
        mlkeywordapi = "http://127.0.0.1:3579/daggit/submit";
        mlvectorapi = "http://127.0.0.1:1729/ml/vector/search";

    }
    // Insert to cassandra
    public  Map<String,Object> insertintoCassandra(Map<String,Object> obj, String identifier) throws Exception {
        String action = obj.get("action").toString();

        if(StringUtils.isNotBlank(action)) {
            if(action.equalsIgnoreCase("update-es-index")) {
                obj = getContentDefinition(obj ,identifier);
                CassandraConnector.updateContentProperties(identifier,mapStage1);
            } else if(action.equalsIgnoreCase("update-ml-keywords")) {
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
        return obj;
    }

    Map<String,Object> getContentDefinition(Map<String,Object> newmap , String identifer) throws Exception {
        try {
            String content = HTTPUtil.makeGetRequest(contentreadapi+identifer);
            JSONObject obj = new JSONObject(content);
            JSONObject contentobj = (JSONObject) (((JSONObject)obj.get("result")).get("content"));
            extractFieldstobeinserted(contentobj);
            makepostreqForMlAPI(contentobj);
            newmap = filterData(newmap,contentobj);

        }catch (Exception e) {
            System.out.println(e);
            throw new Exception("Get content metdata failed");
        }
        return newmap;
    }
    //Getting Fields to be inserted into cassandra
    private void extractFieldstobeinserted(JSONObject contentobj) {
        if(contentobj.has("level1Concept")){
            mapStage1.put("level1Concept",contentobj.get("level1Concept"));
        }
        if(contentobj.has("level2Concept")){
            mapStage1.put("level2Concept",contentobj.get("level2Concept"));
        }
        if(contentobj.has("level3Concept")){
            mapStage1.put("level3Concept",contentobj.get("level3Concept"));
        }
        if(contentobj.has("textbook_name")){
            mapStage1.put("textbook_name",contentobj.get("textbook_name"));
        }
        if(contentobj.has("level1Name")){
            mapStage1.put("level1Name",contentobj.get("level1Name"));
        }
        if(contentobj.has("level2Name")){
            mapStage1.put("level2Name",contentobj.get("level2Name"));
        }
        if(contentobj.has("level3Name")){
            mapStage1.put("level3Name",contentobj.get("level3Name"));
        }
        if(contentobj.has("source")){
            mapStage1.put("source",contentobj.get("source"));
        }
        if(contentobj.has("sourceURL")){
            mapStage1.put("sourceURL",contentobj.get("sourceURL"));
        }
    }

    // POST reqeuest for ml keywords api
    void makepostreqForMlAPI(JSONObject contentdef) throws Exception {
        try {

            JSONObject obj = new JSONObject(mlworkbenchapirequest);
            JSONObject req = ((JSONObject)(obj.get("request")));
            JSONObject input = (JSONObject)req.get("input");
            JSONArray content = (JSONArray)input.get("content");
            content.put(contentdef);
            req.put("job",jobname);
            String resp = HTTPUtil.makePostRequest(mlkeywordapi,obj.toString());
        }
        catch (Exception e) {
            System.out.println(e);
            throw new Exception("Ml keyword api failed");
        }
    }

    // Filter the params of content  to add in ES
    public  Map<String,Object> filterData(Map<String,Object> obj ,JSONObject content) {

        String key = null;
        Object value = null;
        for(int i = 0 ; i < arr.length ; i++ ) {
            key = (arr[i]);
            value = content.has(key)  ? content.get(key) : null;
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
