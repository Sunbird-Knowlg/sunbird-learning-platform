package org.sunbird.itemset.handler;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.sunbird.assessment.store.AssessmentStore;
import org.sunbird.common.dto.Response;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.learning.util.ControllerUtil;

public class QuestionPaperGeneratorUtil {

	private static ControllerUtil controllerUtil = new ControllerUtil();
	private static AssessmentStore assessmentStore = new AssessmentStore();
	private static final String TAXONOMY_ID = "domain";
    private static final String NODE_LIST = "node_list";
    private static final String BODY = "body";
    private static final String EDITORSTATE = "editorstate";
    private static final List<String> externalPropsToFetch = Arrays.asList(BODY, EDITORSTATE);
    
	public static Map<String, Object> getMetadataFromNeo4j(List<String> identifiers) {
		Response response = controllerUtil.getDataNodes(TAXONOMY_ID, identifiers);
		List<Node> nodes = (List<Node>) response.get(NODE_LIST);
		if (CollectionUtils.isNotEmpty(nodes)) {
			return nodes.stream().collect(Collectors.toMap(node -> node.getIdentifier(), node -> (Node) node));
		}
		return null;
	}

	public static Map<String, Object> getExternalPropsData(List<String> identifiers) {
		return assessmentStore.getItems(identifiers, externalPropsToFetch);
	}
}
