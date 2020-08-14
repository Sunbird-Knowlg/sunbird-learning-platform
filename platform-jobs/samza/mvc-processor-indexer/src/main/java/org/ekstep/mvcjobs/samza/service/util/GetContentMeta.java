package org.ekstep.mvcjobs.samza.service.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ekstep.common.Platform;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.searchindex.util.HTTPUtil;

import java.util.HashMap;
import java.util.Map;

public class GetContentMeta {
    private static JobLogger LOGGER = new JobLogger(GetContentMeta.class);

    public  static  Map<String,Object> getContentMetaData(Map<String,Object> newmap , String identifer) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String contentreadapiurl = "";
        try {
            contentreadapiurl = Platform.config.hasPath("kp.content_service.base_url") ? Platform.config.getString("kp.content_service.base_url") : "" ;
            LOGGER.info("MVCProcessorCassandraIndexer :: getContentMetaData :::  Making API call to read content " + contentreadapiurl + "/content/v3/read/");
            String content = HTTPUtil.makeGetRequest(contentreadapiurl + "/content/v3/read/" +identifer);
            LOGGER.info("MVCProcessorCassandraIndexer :: getContentMetaData ::: retrieved content meta " + content);
            Map<String,Object> obj = mapper.readValue(content,Map.class);
            Map<String,Object> contentobj = (HashMap<String,Object>) (((HashMap<String,Object>)obj.get("result")).get("content"));
            newmap = filterData(newmap,contentobj);

        }catch (Exception e) {
            LOGGER.info("MVCProcessorCassandraIndexer :: getContentDefinition ::: Error in getContentDefinitionFunction " + e);
            throw new Exception("Get content metdata failed");
        }
        return newmap;
    }
    public static   Map<String,Object> filterData(Map<String,Object> obj ,Map<String,Object> content) {
        String elasticSearchParamArr[] = {"organisation","channel","framework","board","medium","subject","gradeLevel","name","description","language","appId","appIcon","appIconLabel","contentEncoding","identifier","node_id","nodeType","mimeType","resourceType","contentType","allowedContentTypes","objectType","posterImage","artifactUrl","launchUrl","previewUrl","streamingUrl","downloadUrl","status","pkgVersion","source","lastUpdatedOn","ml_contentText","ml_contentTextVector","ml_Keywords","level1Name","level1Concept","level2Name","level2Concept","level3Name","level3Concept","textbook_name","sourceURL","label","all_fields"};
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
}
