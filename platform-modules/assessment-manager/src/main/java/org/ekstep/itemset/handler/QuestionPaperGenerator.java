package org.ekstep.itemset.handler;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.assessment.handler.AssessmentItemFactory;
import org.ekstep.assessment.handler.IAssessmentHandler;
import org.ekstep.assessment.store.AssessmentStore;
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
import java.util.HashMap;
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
    private static final String HTMLEXT = ".html";

    public static File generateQuestionPaper(Node node) {
        Map<String, Object> childDetails = fetchChildDetails(node);
        if (MapUtils.isNotEmpty(childDetails)) {
            Map<String, Object> assessmentData = getAssessmentDataMap(childDetails);
            Map<String, Object> htmlData = populateAssessmentData(assessmentData);
            String htmlString = generateHtmlString(null, htmlData, node);
            if ( StringUtils.isNotBlank(htmlString))
                return generateHtmlFile(htmlString, node.getIdentifier());
            TelemetryManager.error("HTML String is not generated for ItemSet :: " +  node.getIdentifier() + " :: " + htmlString);
        }
        TelemetryManager.error("ItemSet childDetails are empty for Itemset :: " +  node.getIdentifier() + childDetails);
        return null;
    }

    private static Map<String, Object> fetchChildDetails(Node node) {
        return node.getOutRelations().stream()
                .filter(relation -> StringUtils.equalsIgnoreCase(ASSESSMENT_OBJECT_TYPE, relation.getEndNodeObjectType()))
                .collect(Collectors.toMap(Relation::getEndNodeId, entry -> entry.getMetadata().get(INDEX)));
    }

    private static Map<String, Object> getAssessmentDataMap(Map<String, Object> childData) {
        List<String> identifiers = new ArrayList<>(childData.keySet());
        Map<String, Object> typeMap = getMetadataFromNeo4j(identifiers);
        Map<String, Object> bodyMap = getExternalPropsData(identifiers);
        if (MapUtils.isNotEmpty(typeMap) && MapUtils.isNotEmpty(bodyMap)) {
            Map<String, Object> assessmentMap = new HashMap<>(bodyMap);
            assessmentMap.forEach((key, value) -> ((Map<String, Object>)assessmentMap.get(key)).put(TYPE, typeMap.get(key)));
            assessmentMap.forEach((key, value) -> ((Map<String, Object>)assessmentMap.get(key)).put(INDEX, childData.get(key)));
            return assessmentMap;
        }
        TelemetryManager.error("Question Paper not generated because : typeMap and/or bodyMap is null.");
        return null;
    }

    private static Map<String, Object> getMetadataFromNeo4j(List<String> identifiers) {
        Response response = controllerUtil.getDataNodes(TAXONOMY_ID, identifiers);
        List<Node> nodes = (List<Node>) response.get(NODE_LIST);
        if (CollectionUtils.isNotEmpty(nodes)) {
            return nodes.stream().collect(Collectors.toMap(node -> node.getIdentifier(), node -> (String) ((Node) node).getMetadata().get(TYPE)));
        }
        return null;
    }

    private static Map<String, Object> getExternalPropsData(List<String> identifiers) {
        return assessmentStore.getItems(identifiers, externalPropsToFetch);
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
                    Map<String, Object> bodyMap = mapper.readValue(bodyString, new TypeReference<Map<String, Object>>() {
                    });
                    Map<String, Object> htmlDataMap = new HashMap<String, Object>();
                    htmlDataMap.put("question", handler.populateQuestion(bodyMap));
                    htmlDataMap.put("answer", handler.populateAnswer(bodyMap));
                    htmlDataMap.put("options", handler.populateOptions(bodyMap));
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
    		
    		String htmlTemplate = null;
    		if(MapUtils.isEmpty(assessmentMap)) {
    			return htmlTemplate;
    		}
		htmlTemplate = "<header>\n" + 
				"	<style type=\"text/css\">\n" + 
				"		body {\n" + 
				"			padding: 0;\n" + 
				"			margin: 0;\n" + 
				"		}\n" + 
				"\n" + 
				"		p {\n" + 
				"			margin: 0;\n" + 
				"		}\n" + 
				"\n" + 
				"		.questions-paper {\n" + 
				"			padding: 50px;\n" + 
				"		}\n" + 
				"\n" + 
				"		.question-header {\n" + 
				"			text-align: center;\n" + 
				"			padding: 16px 0;\n" + 
				"			border-bottom: 1px solid #999;\n" + 
				"		}\n" + 
				"\n" + 
				"		.question-header h2 {\n" + 
				"			margin: 0;\n" + 
				"		}\n" + 
				"\n" + 
				"		.main-container .question-section {\n" + 
				"			display: flex;\n" + 
				"			padding-top: 16px;\n" + 
				"		}\n" + 
				"\n" + 
				"		.main-container .question-section:last-child {\n" + 
				"			display: flex;\n" + 
				"			padding-bottom: 16px;\n" + 
				"		}\n" + 
				"\n" + 
				"		.question-count {\n" + 
				"			font-weight: 600;\n" + 
				"		}\n" + 
				"\n" + 
				"		.question-content {\n" + 
				"			padding: 0 8px;\n" + 
				"		}\n" + 
				"\n" + 
				"		.question-title {\n" + 
				"			font-size: 16px;\n" + 
				"			font-weight: 600;\n" + 
				"			padding-bottom: 8px;\n" + 
				"		}\n" + 
				"\n" + 
				"		.mcq-option {\n" + 
				"			padding-left: 16px;\n" + 
				"		}\n" + 
				"\n" + 
				"		.mcq-option p {\n" + 
				"			line-height: 24px;\n" + 
				"		}\n" + 
				"\n" + 
				"		.mcq-option p:before {\n" + 
				"			content: '\\2022';\n" + 
				"			margin-right: 8px;\n" + 
				"		}\n" + 
				"\n" + 
				"		.answer {\n" + 
				"			padding-left: 20px;\n" + 
				"		}\n" + 
				"\n" + 
				"		.answer p:before {\n" + 
				"			content: '\\2022';\n" + 
				"			margin-right: 8px;\n" + 
				"		}\n" + 
				"\n" + 
				"		.answer-sheet {\n" + 
				"			page-break-before: always;\n" + 
				"		}\n" + 
				"	</style>\n" + 
				"</header>\n" + 
				"\n" + 
				"\n" + 
				"<div class=\"questions-paper\">\n" + 
				"	<div class=\"question-sheet\">q_placeholder</div>\n" + 
				"\n" + 
				"	<div class=\"answer-sheet\">a_placeholder</div>\n" + 
				"</div>";
	    	
	    	StringBuilder questionString = new StringBuilder();
	    	questionString.append("<div class='question-header'>" + "<h2>" + (String)itemSet.getMetadata().get("title") + "</h2></div>");
	    	questionString.append("<div class='main-container'>");
		    
		    	assessmentMap.entrySet().forEach(question -> {
		    		questionString.append("<div class='question-section'>");
			    		questionString.append("<div class='question-count'>" 
			    				+  ((Map<String, Object>) question.getValue()).get("index")
			    				+ ".</div>");
			    		questionString.append("<div class='question-content'>");
				    		questionString.append("<div class='question-title'>\n" + 
				    				((Map<String, String>) ((Map<String, Object>) question.getValue()).get("question")).get("text") +
				    				"		</div>");
				    		((List<Map<String, Object>>) ((Map<String, Object>) question.getValue()).get("options")).forEach(option -> {
					    		questionString.append("<div data-simple-choice-interaction data-response-variable='responseValue' class='mcq-option'>\n" + 
					    				option.get("text") + "<img src=\"" + option.get("image") + "\" alt = \"\"/>\n" +
					    				"		</div>");	
				    		});
			    		questionString.append("</div>");
		    		questionString.append("</div>");
		    	});
		    
	    	questionString.append("</div>");
	    	htmlTemplate = htmlTemplate.replace("q_placeholder", questionString.toString());
	    	
	    	
	    	StringBuilder answerString = new StringBuilder();
	    	answerString.append("<div class='question-header'><h3>Answers</h3></div>");
	    	answerString.append("<div class=\"main-container\">");
		    	assessmentMap.keySet().forEach(key -> {
		    		answerString.append("<div class='question-section'>");
		    			answerString.append("<div class='question-count'>" 
			    				+  ((Map<String, Object>) assessmentMap.get(key)).get("index")
			    				+ ".</div>");
		    			answerString.append("<div class='question-content'>");
				    		answerString.append("<div class='question-title'><p>" + 
				    				((Map<String, String>)((Map<String, Object>) assessmentMap.get(key)).get("question")).get("text") + 
				    				"</p></div>");
				    		answerString.append("<div class='answer<p>'>" + 
				    				((Map<String, String>)((Map<String, Object>) assessmentMap.get(key)).get("answer")).get("text") + 
				    				"</div></p>");
			    		answerString.append("</div>");
		    		answerString.append("</div>");
		    	});
		answerString.append("</div>");
	    	htmlTemplate = htmlTemplate.replace("a_placeholder", answerString.toString());
   
        return htmlTemplate;
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
