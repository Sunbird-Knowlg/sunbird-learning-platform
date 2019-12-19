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
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.util.S3PropertyReader;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.dac.model.Relation;
import org.ekstep.learning.common.enums.ContentErrorCodes;
import org.ekstep.learning.util.CloudStore;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.telemetry.logger.TelemetryManager;

import java.io.File;
import java.io.FileWriter;
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
    private static final String CONTENT_FOLDER = "cloud_storage.content.folder";
    private static final String QUESTION_PAPER_FOLDER = "cloud_storage.question_paper.folder";
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String TEMP_FILE_LOCATION = Platform.config.hasPath("lp.assessment.tmp_file_location") ? Platform.config.getString("lp.assessment.tmp_file_location") : "/tmp/";
    private static final String HTML_PREFIX = "html_";

    public static String generateQuestionPaper(Node node) {
        List<String> questionIds = fetchChildIds(node);
        if (CollectionUtils.isNotEmpty(questionIds)) {
            Map<String, Object> assessmentData = getAssessmentDataMap(questionIds);
            Map<String, Object> htmlData = populateAssessmentData(assessmentData);
            String htmlString = generateHtmlString(null, htmlData);
            if (StringUtils.isNotEmpty(htmlString) && StringUtils.isNotBlank(htmlString)) {
                File htmlFile = generateHtmlFile(htmlString, node.getIdentifier());
                return uploadFileToCloud(htmlFile, QUESTION_PAPER_FOLDER);
            } else
                return null;
        }
        return null;
    }

    private static List<String> fetchChildIds(Node node) {
        return node.getOutRelations().stream()
                .filter(relation -> StringUtils.equalsIgnoreCase(ASSESSMENT_OBJECT_TYPE, relation.getEndNodeObjectType()))
                .map(Relation::getEndNodeId)
                .collect(Collectors.toList());
    }

    private static Map<String, Object> getAssessmentDataMap(List<String> identifiers) {
        Map<String, Object> nodeMap = getMetadataFromNeo4j(identifiers);
        Map<String, Object> cassandraMap = getExternalPropsData(identifiers);
        if (MapUtils.isNotEmpty(nodeMap) && MapUtils.isNotEmpty(nodeMap)) {
            Map<String, Object> assessmentMap = new HashMap<>(cassandraMap);
            assessmentMap.forEach((key, value) -> nodeMap.merge(key, value, (v1, v2) -> ((Map<String, Object>) cassandraMap.get(key)).put(TYPE, v2)));
            return assessmentMap;
        }
        return new HashMap<>();
    }

    private static Map<String, Object> getMetadataFromNeo4j(List<String> identifiers) {
        Response response = controllerUtil.getDataNodes(TAXONOMY_ID, identifiers);
        List<Node> nodes = (List<Node>) response.get(NODE_LIST);
        if (CollectionUtils.isNotEmpty(nodes)) {
            return nodes.stream().collect(Collectors.toMap(node -> node.getIdentifier(), node -> (String) ((Node) node).getMetadata().get(TYPE)));
        }
        return new HashMap<>();
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
        assessmentMap.forEach((key, value) -> populateData(assessmentHtmlMap, key, value));
        return assessmentHtmlMap;
    }

    private static void populateData(Map<String, Object> assessmentHtmlMap, String key, Object value) {
        Map<String, Object> valueMap = (Map<String, Object>) value;
        IAssessmentHandler handler = AssessmentItemFactory.getHandler((String) (valueMap.get(TYPE)));
        try {
            Map<String, Object> bodyMap = mapper.readValue((String) valueMap.get(BODY), new TypeReference<Map<String, String>>() {
            });
            Map<String, Object> htmlDataMap = new HashMap<String, Object>() {{
                put("questions", handler.populateQuestions(bodyMap));
                put("answers", handler.populateAnswers(bodyMap));
                put("options", handler.populateOptions(bodyMap));
            }};
            assessmentHtmlMap.put(key, htmlDataMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static String generateHtmlString(File htmlTemp, Map<String, Object> assessmentMap) {
        String htmlTemplate = "      <div class=\"questions\">\n" +
                "            q_placeholder\n" +
                "      </div>\n" +
                "      <div class=\"answers\" style=\"page-break-before: always\">\n" +
                "            a_placeholder\n" +
                "      </div>\n";
        StringBuilder questionString = new StringBuilder();
        assessmentMap.entrySet().forEach(question -> {
            questionString.append("<div class=\"question\">\n")
                    .append(((Map<String, String>) ((Map<String, Object>) question.getValue()).get("question")).get("text"))
                    .append("<img src=\"" + ((Map<String, Object>) ((Map<String, Object>) question.getValue()).get("question")).get("image") + "\" alt = \"\"/>\n")
                    .append("<div class=\"row\">\n");
            ((List<Map<String, Object>>) ((Map<String, Object>) question.getValue()).get("options")).forEach(option -> {
                questionString.append("<div class=\"column\">\n").append(option.get("text")).append("<img src=\"" + option.get("image") + "\" alt = \"\"/>\n").append("</div>\n");
            });
            questionString.append("</div>\n");
            questionString.append("</div>\n");
        });
        htmlTemplate = htmlTemplate.replace("q_placeholder", questionString.toString());
        StringBuilder answersString = new StringBuilder();
        assessmentMap.keySet().forEach(key -> {
            answersString.append("<div class=\"answer\">")
                    .append(((Map<String, String>) ((Map<String, Object>) assessmentMap.get(key)).get("question")).get("text"))
                    .append("<img src=\"" + ((Map<String, Object>) ((Map<String, Object>) assessmentMap.get(key)).get("question")).get("image") + "\" alt = \"\"/>\n")
                    .append("<p>" + ((Map<String, Object>) assessmentMap.get(key)).get("text") + "</p>\n")
                    .append("<img src=\"" + ((Map<String, Object>) assessmentMap.get(key)).get("image") + " \" alt=\"\"/>\n")
                    .append("</div>\n");
        });
        htmlTemplate = htmlTemplate.replace("a_placeholder", answersString.toString());
        return htmlTemplate;
    }

    private static File generateHtmlFile(String htmlString, String assessmentSetId) {
        File htmlFile = new File(TEMP_FILE_LOCATION + assessmentSetId + "_" + getFileName(HTML_PREFIX));
        try (FileWriter fw = new FileWriter(htmlFile)) {
            fw.write(htmlString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return htmlFile;
    }

    private static String uploadFileToCloud(File pdfFile, String objectFolderName) {
        try {
            String folder = S3PropertyReader.getProperty(CONTENT_FOLDER);
            folder = folder + "/" + S3PropertyReader.getProperty(objectFolderName);
            String[] urlArray = CloudStore.uploadFile(folder, pdfFile, true);
            return urlArray[1];
        } catch (Exception e) {
            TelemetryManager.error("Error while uploading the file.", e);
            throw new ServerException(ContentErrorCodes.ERR_CONTENT_UPLOAD_FILE.name(),
                    "Error while uploading the File.", e);
        }
    }

    public static String getFileName(String prefix) {
        return prefix + System.currentTimeMillis();
    }

}
