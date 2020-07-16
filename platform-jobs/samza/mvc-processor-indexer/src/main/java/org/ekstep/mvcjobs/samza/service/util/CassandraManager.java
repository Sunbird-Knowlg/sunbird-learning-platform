package org.ekstep.mvcjobs.samza.service.util;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.mvcjobs.samza.task.Postman;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CassandraManager {
    String arr[],serverIP= "127.0.0.1",keyspace = "sunbirddev_content_store";
    Session session;
    List<String> ml_level1 = null, ml_level2  = null, ml_level3 = null , textbook_name , level1_name , level2_name , level3_name , source;
    String contentreadapi = "", mlworkbenchapirequest = "", mlvectorListRequest = "" , jobname = "" , mlkeywordapi = "" , mlvectorapi = "" , source_url = null ;
    String ml_contentText;
    List<String> ml_Keywords;
    List<List<Double>> ml_contentTextVectorList;
    Set<Double> ml_contentTextVector = null;
    CassandraManager() {
        arr = CompositeSearchConstants.propertyArray;
        contentreadapi = CompositeSearchConstants.api;
        mlworkbenchapirequest = CompositeSearchConstants.mlworkbenchapirequest;
        mlvectorListRequest = CompositeSearchConstants.mlvectorListRequest;
        jobname = CompositeSearchConstants.jobname;
        mlkeywordapi = CompositeSearchConstants.mlkeywordapi;
        mlvectorapi = CompositeSearchConstants.mlvectorapi;
        // Connect to cassandra
        Cluster cluster = Cluster.builder()
                .addContactPoints(serverIP)
                .build();

        session = cluster.connect(keyspace);
    }
    // Insert to cassandra
    public Map<String,Object> insertintoCassandra(Map<String,Object> obj, String identifier) {
        String action = obj.get("action").toString();
        String cqlquery = "";
        BoundStatement bound = null;


        if(StringUtils.isNotBlank(action)) {
            if(action.equalsIgnoreCase("update-es-index")) {
                obj = getContentDefinition(obj ,identifier);
                cqlquery = "UPDATE content_data SET textbook_name = ? ,level1_concept = ? , level2_concept = ? , level3_concept = ?  , level1_name = ? , level2_name = ? , level3_name = ? , source = ? , source_url = ? WHERE content_id = ?";
                PreparedStatement prepared = preparestaement(session,cqlquery);
                bound = prepared.bind(textbook_name,ml_level1,ml_level2,ml_level3,level1_name ,level2_name,level3_name,source,source_url,identifier);
            } else if(action.equalsIgnoreCase("update-ml-keywords")) {
                cqlquery = "UPDATE content_data SET ml_keywords = ? , ml_content_text = ? WHERE content_id = ?";
                 ml_contentText = obj.get("ml_contentText") != null ? obj.get("ml_contentText").toString() : null;
                 ml_Keywords = obj.get("ml_Keywords") != null ? (List<String>) obj.get("ml_Keywords") : null;

                makepostreqForVectorApi(ml_contentText,identifier);
                PreparedStatement prepared = preparestaement(session,cqlquery);
                bound = prepared.bind(ml_Keywords,ml_contentText,identifier);
            }
            else  if(action.equalsIgnoreCase("update-ml-contenttextvector")) {
                cqlquery = "UPDATE content_data SET ml_content_text_vector = ? WHERE content_id = ?";
                 ml_contentTextVectorList = obj.get("ml_contentTextVector") != null ? (List<List<Double>>) obj.get("ml_contentTextVector") : null;
                if(ml_contentTextVectorList != null)
                {
                    ml_contentTextVector = new HashSet<Double>(ml_contentTextVectorList.get(0));

                }
                PreparedStatement prepared = preparestaement(session,cqlquery);
                bound = prepared.bind(ml_contentTextVector,identifier);
            }
        }
        if(cqlquery != "" && bound != null) {
            session.execute(bound);
        }
        return obj;
    }
    PreparedStatement  preparestaement(Session session, String query) {
        return session.prepare(query);
    }
    Map<String,Object> getContentDefinition(Map<String,Object> newmap , String identifer) {
        try {
            String content = Postman.getContent(contentreadapi,identifer);
            JSONObject obj = new JSONObject(content);
            JSONObject contentobj = (JSONObject) (((JSONObject)obj.get("result")).get("content"));
            extractFieldstobeinserted(contentobj);
            makepostreqForMlAPI(contentobj);
            newmap = filterData(newmap,contentobj);

        }catch (Exception e) {
            System.out.println(e);
        }
        return newmap;
    }
    //Getting Fields to be inserted into cassandra
    private void extractFieldstobeinserted(JSONObject contentobj) {
        if(contentobj.has("level1Concept")) {
            ml_level1 =  (List<String>)contentobj.get("level1Concept") ;
        }
        ml_level2 = contentobj.has("level2Concept")  ? (List<String>)contentobj.get("level2Concept") : null;
        ml_level3 = contentobj.has("level3Concept")  ? (List<String>)contentobj.get("level3Concept") : null;
        textbook_name = contentobj.has("textbook_name")  ? (List<String>)contentobj.get("textbook_name") : null;
        level1_name = contentobj.has("level1Name")  ? (List<String>)contentobj.get("level1Name") : null;
        level2_name = contentobj.has("level2Name")  ? (List<String>)contentobj.get("level2Name") : null;
        level3_name = contentobj.has("level3Name")  ? (List<String>)contentobj.get("level3Name") : null;
        source = contentobj.has("source")  ? (List<String>)contentobj.get("source") : null;
        source_url = contentobj.has("sourceURL")  ? contentobj.get("sourceURL").toString() : null;
    }

    // POST reqeuest for ml keywords api
    void makepostreqForMlAPI(JSONObject contentdef) {
        JSONObject obj = new JSONObject(mlworkbenchapirequest);
        JSONObject req = ((JSONObject)(obj.get("request")));
        JSONObject input = (JSONObject)req.get("input");
        JSONArray content = (JSONArray)input.get("content");
        content.put(contentdef);
        req.put("job",jobname);
        String resp = Postman.POST(obj.toString(),mlkeywordapi);
    }

    // Filter the params of content defintion to add in ES
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
    public  void makepostreqForVectorApi(String contentText,String identifier) {
        try {
            JSONObject obj = new JSONObject(mlworkbenchapirequest);
            JSONObject req = ((JSONObject) (obj.get("request")));
            JSONArray text = (JSONArray) req.get("text");
            req.put("cid",identifier);
            text.put(contentText);
            String resp = Postman.POST(obj.toString(),mlvectorapi);
            JSONObject respobj = new JSONObject(resp);
            String status = (((JSONObject)respobj.get("result")).get("status")).toString();
            System.out.println("Vector list api status is " + status);
        }
        catch (Exception e) {
           System.out.println(e);
        }
    }

}
