package org.ekstep.itemset.handler;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.assessment.handler.AssessmentItemFactory;
import org.ekstep.assessment.handler.IAssessmentHandler;
import org.ekstep.assessment.store.AssessmentStore;
import org.ekstep.assessment.util.QuestionTemplateHandler;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.dac.model.Relation;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.telemetry.logger.TelemetryManager;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QuestionPaperGenerator {
    private static ControllerUtil controllerUtil = new ControllerUtil();
    private static AssessmentStore assessmentStore = new AssessmentStore();
    private static final String ASSESSMENT_OBJECT_TYPE = "AssessmentItem";
    private static final String TAXONOMY_ID = "domain";
    private static final String NODE_LIST = "node_list";
    private static final String BODY = "body";
    private static final List<String> externalPropsToFetch = Arrays.asList(BODY);
    private static final String TYPE = "type";
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String TEMP_FILE_LOCATION = Platform.config.hasPath("lp.assessment.tmp_file_location") ? Platform.config.getString("lp.assessment.tmp_file_location") : "/tmp/";
    private static final String HTML_PREFIX = "html_";
    private static final String INDEX = "IL_SEQUENCE_INDEX";
    private static final String ANSWER = "answer";
    private static final String HTMLEXT = ".html";
    private static final String TEMPLATE_NAME = Platform.config.hasPath("lp.assessment.template_name") ? Platform.config.getString("lp.assessment.template_name") :"questionSetTemplate.vm";

    public static File generateQuestionPaper(Node node) {
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

    private static Map<String, Object> getAssessmentDataMap(Map<String, Object> childData) {
        List<String> identifiers = new ArrayList<>(childData.keySet());
        Map<String, Object> nodeMap = QuestionPaperGeneratorUtil.getMetadataFromNeo4j(identifiers);
        Map<String, Object> bodyMap = QuestionPaperGeneratorUtil.getExternalPropsData(identifiers);
        if (MapUtils.isNotEmpty(nodeMap) && MapUtils.isNotEmpty(bodyMap)) {
            Map<String, Object> assessmentMap = new HashMap<>(bodyMap);
            assessmentMap.forEach((key, value) -> ((Map<String, Object>)assessmentMap.get(key)).put(TYPE, ((String)((Node)nodeMap.get(key)).getMetadata().get("type")) ));
            assessmentMap.forEach((key, value) -> ((Map<String, Object>)assessmentMap.get(key)).put(INDEX, childData.get(key)));
            assessmentMap.forEach((key, value) -> ((Map<String, Object>)assessmentMap.get(key)).put(ANSWER, ((Node)nodeMap.get(key)).getMetadata().get("responseDeclaration")));
            return assessmentMap;
        }
        TelemetryManager.error("Question Paper not generated because : typeMap and/or bodyMap is null.");
        return null;
    }
    
    private static Map<String, Object> sortByIndex(Map<String, Object> hm) 
    { 
        // Create a list from elements of HashMap 
        List<Map.Entry<String, Object> > list = new LinkedList(hm.entrySet()); 
  
        // Sort the list 
        Collections.sort(list, new Comparator<Map.Entry<String, Object> >() { 
            public int compare(Map.Entry<String, Object> o1,  
                               Map.Entry<String, Object> o2) 
            { 
                return ((Long)((Map<String, Object>)o1.getValue()).get("index")).compareTo((Long)((Map<String, Object>)o2.getValue()).get("index")); 
            } 
        }); 
          
        // put data from sorted list to hashmap  
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
                    Map<String, Object> answerMap = mapper.readValue((String)valueMap.get(ANSWER), new TypeReference<Map<String, Object>>(){});
                    htmlDataMap.put("answer", handler.populateAnswer(answerMap));
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
        velocityContext.put("title", (String)itemSet.getMetadata().get("title"));
        assessmentMap.entrySet().forEach(question -> {
            strBuilder.append("<div class='question-section'>");
            strBuilder.append("<div class='question-count'>" + ((Map<String, Object>) question.getValue()).get("index") + ".</div>");
            strBuilder.append(((Map<String, Object>) question.getValue()).get("question"));
            strBuilder.append("</div>");
        });

        velocityContext.put("questions", strBuilder.toString());
        StringBuilder answerString = new StringBuilder();
        assessmentMap.keySet().forEach(key -> {
            answerString.append("<div class='question-section'>");
            answerString.append("<div class='question-count'>" + ((Map<String, Object>) assessmentMap.get(key)).get("index") + ".</div>");
            answerString.append("<div class='answer'>" + (((Map<String, Object>) assessmentMap.get(key)).get("answer")) + "</div>");
            answerString.append("</div>");
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
