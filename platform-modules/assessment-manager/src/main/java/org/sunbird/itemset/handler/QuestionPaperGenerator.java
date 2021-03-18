package org.sunbird.itemset.handler;


import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.assessment.handler.AssessmentItemFactory;
import org.sunbird.assessment.handler.IAssessmentHandler;
import org.sunbird.assessment.util.QuestionTemplateHandler;
import org.sunbird.common.Platform;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.Relation;
import org.sunbird.telemetry.logger.TelemetryManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QuestionPaperGenerator {
    private static final String ASSESSMENT_OBJECT_TYPE = "AssessmentItem";
    private static final String BODY = "body";
    private static final String TYPE = "type";
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String TEMP_FILE_LOCATION = Platform.config.hasPath("lp.assessment.tmp_file_location") ? Platform.config.getString("lp.assessment.tmp_file_location") : "/tmp/";
    private static final String HTML_PREFIX = "html_";
    private static final String INDEX = "IL_SEQUENCE_INDEX";
    private static final String ANSWER = "answer";
    private static final String HTMLEXT = ".html";
    private static final String TEMPLATE_NAME = Platform.config.hasPath("lp.assessment.template_name") ? Platform.config.getString("lp.assessment.template_name") :"questionSetTemplate.vm";

    public static File generateQuestionPaper(Node node) throws JsonParseException, JsonMappingException, IOException {
        Map<String, Object> childDetails = fetchChildDetails(node);
        if (MapUtils.isNotEmpty(childDetails)) {
            Map<String, Object> assessmentData = getAssessmentDataMap(childDetails);
            Map<String, Object> htmlData = populateAssessmentData(assessmentData);
            Map<String, Object> sortedHtmlData = sortByIndex(htmlData);
            String htmlString = generateHtmlString(null, sortedHtmlData, node);
            if ( StringUtils.isNotBlank(htmlString))
                return generateHtmlFile(htmlString, node.getIdentifier());
            TelemetryManager.error("HTML String is not generated for ItemSet :: " +  node.getIdentifier() + " :: " + htmlString);
        }
        TelemetryManager.error("ItemSet childDetails are empty for Itemset :: " +  node.getIdentifier() + childDetails);
        return null;
    }

    public static Map<String, Object> fetchChildDetails(Node node) {
        return node.getOutRelations().stream()
                .filter(relation -> StringUtils.equalsIgnoreCase(ASSESSMENT_OBJECT_TYPE, relation.getEndNodeObjectType()))
                .collect(Collectors.toMap(Relation::getEndNodeId, entry -> entry.getMetadata().get(INDEX)));
    }

    public static Map<String, Object> getAssessmentDataMap(Map<String, Object> childData) throws JsonParseException, JsonMappingException, IOException {
        List<String> identifiers = new ArrayList<>(childData.keySet());
        Map<String, Object> nodeMap = QuestionPaperGeneratorUtil.getMetadataFromNeo4j(identifiers);
        Map<String, Object> externalPropMap = QuestionPaperGeneratorUtil.getExternalPropsData(identifiers);
        if (MapUtils.isNotEmpty(nodeMap) && MapUtils.isNotEmpty(externalPropMap)) {
            Map<String, Object> assessmentMap = new HashMap<>(externalPropMap);
            for(String id : assessmentMap.keySet()) {
            		String type = ((String)((Node)nodeMap.get(id)).getMetadata().get("type"));
            		Map<String, Object> editorState = mapper.readValue((String)((Map<String, Object>) externalPropMap.get(id)).get("editorstate"), new TypeReference<Map<String, Object>>(){});//(Map<String, Object>)((Node)nodeMap.get(id)).getMetadata().get("editorState");
            		String answer = null;
            		if(StringUtils.isNotBlank((String)((Node)nodeMap.get(id)).getMetadata().get("responseDeclaration"))) {
            			Map<String, Object> answerMap = mapper.readValue((String)((Node)nodeMap.get(id)).getMetadata().get("responseDeclaration"), new TypeReference<Map<String, Object>>(){});
                		answer =  (String) ((Map<String, Object>) ((Map<String, Object>) answerMap.
            					getOrDefault("responseValue", new HashMap<String, Object>())).
            					getOrDefault("correct_response", new HashMap<String, Object>())).
            					getOrDefault("value", null);
            		}
            		
            		if(StringUtils.equalsIgnoreCase(type, "mcq") && MapUtils.isNotEmpty(editorState)) {
            			if(StringUtils.isNoneBlank(answer)) {
            				((Map<String, Object>)assessmentMap.get(id)).put(ANSWER, 
            						((Map<String, Object>)
            								((Map<String, Object>)
            										((List<Map<String, Object>>)editorState.get("options")).
            										get(Integer.parseInt(answer))).
            								getOrDefault("value", new HashMap<String, Object>())).
            						getOrDefault("body", ""));
            			}else {
            				((Map<String, Object>)assessmentMap.get(id)).put(ANSWER, "");
            			}
            		}else {
            			((Map<String, Object>)assessmentMap.get(id)).put(ANSWER, null != answer ? answer : "");
            		}
            		((Map<String, Object>)assessmentMap.get(id)).put(TYPE, type);
            		((Map<String, Object>)assessmentMap.get(id)).put(INDEX, childData.get(id));
            }
            return assessmentMap;
        }
        TelemetryManager.error("Question Paper not generated because : typeMap and/or bodyMap is null.");
        return null;
    }
    
    private static Map<String, Object> sortByIndex(Map<String, Object> hm) 
    { 
        List<Map.Entry<String, Object> > list = new LinkedList(hm.entrySet()); 
  
        Collections.sort(list, new Comparator<Map.Entry<String, Object> >() { 
            public int compare(Map.Entry<String, Object> o1,  
                               Map.Entry<String, Object> o2) 
            { 
                return ((Long)((Map<String, Object>)o1.getValue()).get("index")).compareTo((Long)((Map<String, Object>)o2.getValue()).get("index")); 
            } 
        }); 
          
        HashMap<String, Object> temp = new LinkedHashMap<String, Object>(); 
        for (Map.Entry<String, Object> aa : list) { 
            temp.put(aa.getKey(), aa.getValue()); 
        } 
        return temp; 
    } 

   


    /**
     * This will return a Map with id and Map<String, Object> containing data required to be populated in html template</String,>
     *
     * @param assessmentMap
     * @return
     */
    private static Map<String, Object> populateAssessmentData(Map<String, Object> assessmentMap) {
        Map<String, Object> assessmentHtmlMap = new HashMap<>();
        if (MapUtils.isNotEmpty(assessmentMap))
            assessmentMap.forEach((key, value) -> populateData(assessmentHtmlMap, key, value));
        return assessmentHtmlMap;
    }

    private static void populateData(Map<String, Object> assessmentHtmlMap, String key, Object value) {
        Map<String, Object> valueMap = (Map<String, Object>) value;
        IAssessmentHandler handler = AssessmentItemFactory.getHandler((String) (valueMap.get(TYPE)));
        if (null != handler) {
            try {
                String bodyString = (String) valueMap.get(BODY);
                if (StringUtils.isNotBlank(bodyString)) {
                    Map<String, Object> htmlDataMap = new HashMap<String, Object>();
                    htmlDataMap.put("question", handler.populateQuestion(bodyString));
                    htmlDataMap.put("answer", handler.populateAnswer((String)valueMap.get(ANSWER)));
                    htmlDataMap.put("index", valueMap.get(INDEX));
                    assessmentHtmlMap.put(key, htmlDataMap);
                } else
                    assessmentHtmlMap.put(key, new HashMap<>());
            } catch (Exception e) {
                TelemetryManager.error("Exception while fetching question data from body :: " + e.getMessage());
            }
        }
    }

    // TODO: Need to use index for questions and options
    private static String generateHtmlString(File htmlTemp, Map<String, Object> assessmentMap, Node itemSet) {
        if(MapUtils.isEmpty(assessmentMap)) {
            return null;
        }
        Map<String, Object> velocityContext = new HashMap<>();
        StringBuilder strBuilder = new StringBuilder();
        velocityContext.put("title", (String)itemSet.getMetadata().get("name"));
        assessmentMap.entrySet().forEach(question -> {
            strBuilder.append("<div class='question-section'>");
            strBuilder.append("<div class='question-count'>" + ((Map<String, Object>) question.getValue()).get("index") + ".&nbsp</div>");
            strBuilder.append(((Map<String, Object>) question.getValue()).get("question"));
            strBuilder.append("</div>");
        });

        velocityContext.put("questions", strBuilder.toString());
        StringBuilder answerString = new StringBuilder();
        assessmentMap.keySet().forEach(key -> {
            answerString.append("<div class='question-section'>");
            answerString.append("<div class='question-count'>" + ((Map<String, Object>) assessmentMap.get(key)).get("index") + ".&nbsp</div>");
            answerString.append(((Map<String, Object>) assessmentMap.get(key)).get("question"));
            answerString.append("</div>");
            answerString.append("<div class='answer'>" + (((Map<String, Object>) assessmentMap.get(key)).get("answer")) + "</div>");
        });
        velocityContext.put("answers", answerString.toString());
        return QuestionTemplateHandler.handleHtmlTemplate(TEMPLATE_NAME, velocityContext);
    }

    private static File generateHtmlFile(String htmlString, String assessmentSetId) {
        File htmlFile = new File(TEMP_FILE_LOCATION + assessmentSetId + "_" + getFileName(HTML_PREFIX) + HTMLEXT);
        try (FileWriter fw = new FileWriter(htmlFile)) {
            fw.write(htmlString);
        } catch (Exception e) {
            e.printStackTrace();
            if(htmlFile.exists())
            		htmlFile.delete();
        }
        return htmlFile;
    }


    public static String getFileName(String prefix) {
        return prefix + System.currentTimeMillis();
    }
}
