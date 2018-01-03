package org.ekstep.content.publish;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.exception.ClientException;
import org.ekstep.content.common.ContentErrorMessageConstants;
import org.ekstep.content.enums.ContentErrorCodeConstants;
import org.ekstep.content.enums.ContentWorkflowPipelineParams;
import org.ekstep.content.pipeline.initializer.InitializePipeline;
import org.ekstep.content.util.PublishWebHookInvoker;
import org.ekstep.graph.dac.enums.RelationTypes;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.dac.model.Relation;
import org.ekstep.graph.service.common.DACConfigurationConstants;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.util.ControllerUtil;
import org.ekstep.telemetry.handler.Level;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.ekstep.common.dto.NodeDTO;

public class PublishTask implements Runnable {

	private String contentId;
	private Map<String, Object> parameterMap;
	protected static final String DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX = ".img";
	private static final String COLLECTION_CONTENT_MIMETYPE = "application/vnd.ekstep.content-collection";
	private ControllerUtil util = new ControllerUtil();
	
	public PublishTask(String contentId, Map<String, Object> parameterMap) {
		this.contentId = contentId;
		this.parameterMap = parameterMap;
	}

	@Override
	public void run() {
		Node node = (Node) this.parameterMap.get(ContentWorkflowPipelineParams.node.name());
		try {
			publishContent(node);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// TODO: add try catch here.
	private void publishContent(Node node) throws Exception{
		TelemetryManager.log("Publish processing start for content", node.getIdentifier(), Level.INFO.name());
		if (StringUtils.equalsIgnoreCase((String) node.getMetadata().get("mimeType"), COLLECTION_CONTENT_MIMETYPE)) {
			List<NodeDTO> nodes = util.getNodesForPublish(node);
			if (!nodes.isEmpty()) {
				node.getMetadata().put(ContentWorkflowPipelineParams.compatibilityLevel.name(), getCompatabilityLevel(nodes));
			}
			Stream<NodeDTO> nodesToPublish = filterAndSortNodes(nodes);
			nodesToPublish.forEach(nodeDTO -> publishCollectionNode(nodeDTO, (String)node.getMetadata().get("publish_type")));
			
		}
		publishNode(node, (String) node.getMetadata().get("mimeType"));
		TelemetryManager.log("Publish processing done for content", node.getIdentifier(), Level.INFO.name());
		
		TelemetryManager.log("Content enrichment start for content", node.getIdentifier(), Level.INFO.name());
		
		String nodeId = node.getIdentifier().replace(".img", "");
		Node publishedNode = util.getNode("domain", nodeId);
		if(StringUtils.equalsIgnoreCase(((String)publishedNode.getMetadata().get("mimeType")), COLLECTION_CONTENT_MIMETYPE)) {
			String versionKey = Platform.config.getString(DACConfigurationConstants.PASSPORT_KEY_BASE_PROPERTY);
			publishedNode.getMetadata().put("versionKey", versionKey);
			processCollection(publishedNode);
			TelemetryManager.log("Content enrichment done for content", node.getIdentifier(), Level.INFO.name());
		}
	}
	
	private Map<String, Object> processChildren(Node node, String graphId, Map<String, Object> dataMap) throws Exception {
		List<String> children;
		children = getChildren(node);
		if(!children.isEmpty()){
			dataMap = new HashMap<String, Object>();
			for (String child : children) {
				Node childNode = util.getNode(graphId, child);
				dataMap = mergeMap(dataMap, processChild(childNode));
				processChildren(childNode, graphId, dataMap);
			}
		}
		return dataMap;
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Object> mergeMap(Map<String, Object> dataMap, Map<String, Object> childDataMap) throws Exception {
		if (dataMap.isEmpty()) {
			dataMap.putAll(childDataMap);
		} else {
			for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
				Set<Object> value = new HashSet<Object>();
				if (childDataMap.containsKey(entry.getKey())) {
					value.addAll((Collection<? extends Object>) childDataMap.get(entry.getKey()));
				}
				value.addAll((Collection<? extends Object>) entry.getValue());
				dataMap.replace(entry.getKey(), value);
			}
			if (!dataMap.keySet().containsAll(childDataMap.keySet())) {
				for (Map.Entry<String, Object> entry : childDataMap.entrySet()) {
					if (!dataMap.containsKey(entry.getKey())) {
						dataMap.put(entry.getKey(), entry.getValue());
					}
				}
			}
		}
		return dataMap;
	}
	
	private List<String> getChildren(Node node) throws Exception {
		List<String> children = new ArrayList<>();
		if(null != node.getOutRelations()){
			for (Relation rel : node.getOutRelations()) {
				if ("content".equalsIgnoreCase(rel.getEndNodeObjectType())) {
					children.add(rel.getEndNodeId());
				}
			}
		}
		return children;
	}
	
	private Map<String, Object> processChild(Node node) throws Exception {

		Map<String, Object> result = new HashMap<>();
		Set<Object> language = new HashSet<Object>();
		Set<Object> concepts = new HashSet<Object>();
		Set<Object> domain = new HashSet<Object>();
		Set<Object> grade = new HashSet<Object>();
		Set<Object> age = new HashSet<Object>();
		Set<Object> medium = new HashSet<Object>();
		Set<Object> subject = new HashSet<Object>();
		Set<Object> genre = new HashSet<Object>();
		Set<Object> theme = new HashSet<Object>();
		Set<Object> keywords = new HashSet<Object>();
		if (null != node.getMetadata().get("language")) {
			String[] langData = (String[]) node.getMetadata().get("language");
			language = new HashSet<Object>(Arrays.asList(langData));
			result.put("language", language);
		}
		if (null != node.getMetadata().get("domain")) {
			String[] domainData = (String[]) node.getMetadata().get("domain");
			domain = new HashSet<Object>(Arrays.asList(domainData));
			result.put("domain", domain);
		}
		if (null != node.getMetadata().get("gradeLevel")) {
			String[] gradeData = (String[]) node.getMetadata().get("gradeLevel");
			grade = new HashSet<Object>(Arrays.asList(gradeData));
			result.put("gradeLevel", grade);
		}
		if (null != node.getMetadata().get("ageGroup")) {
			String[] ageData = (String[]) node.getMetadata().get("ageGroup");
			age = new HashSet<Object>(Arrays.asList(ageData));
			result.put("ageGroup", age);
		}
		if (null != node.getMetadata().get("medium")) {
			String mediumData = (String) node.getMetadata().get("medium");
			medium = new HashSet<Object>(Arrays.asList(mediumData));
			result.put("medium", medium);
		}
		if (null != node.getMetadata().get("subject")) {
			String subjectData = (String) node.getMetadata().get("subject");
			subject = new HashSet<Object>(Arrays.asList(subjectData));
			result.put("subject", subject);
		}
		if (null != node.getMetadata().get("genre")) {
			String[] genreData = (String[]) node.getMetadata().get("genre");
			genre = new HashSet<Object>(Arrays.asList(genreData));
			result.put("genre", genre);
		}
		if (null != node.getMetadata().get("theme")) {
			String[] themeData = (String[]) node.getMetadata().get("theme");
			theme = new HashSet<Object>(Arrays.asList(themeData));
			result.put("theme", theme);
		}
		if (null != node.getMetadata().get("keywords")) {
			String[] keyData = (String[]) node.getMetadata().get("keywords");
			keywords = new HashSet<Object>(Arrays.asList(keyData));
			result.put("keywords", keywords);
		}
		for (Relation rel : node.getOutRelations()) {
			if ("Concept".equalsIgnoreCase(rel.getEndNodeObjectType())) {
				//LOGGER.info("EndNodeId as Concept ->" + rel.getEndNodeId());
				concepts.add(rel.getEndNodeId());
			}
		}
		if (null != concepts && !concepts.isEmpty()) {
			result.put("concepts", concepts);
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private void processCollection(Node node) throws Exception {

		String graphId = node.getGraphId();
		String contentId = node.getIdentifier();
		Map<String, Object> dataMap = null;
		dataMap = processChildren(node, graphId, dataMap);
		//LOGGER.debug("Children nodes process for collection - " + contentId);
		if(null != dataMap){
			for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
				if ("concepts".equalsIgnoreCase(entry.getKey()) || "keywords".equalsIgnoreCase(entry.getKey())) {
					continue;
				} else if ("subject".equalsIgnoreCase(entry.getKey())) {
					Set<Object> subject = (HashSet<Object>) entry.getValue();
					if (null != subject.iterator().next()) {
						node.getMetadata().put(entry.getKey(), (String) subject.iterator().next());
					}
				} else if ("medium".equalsIgnoreCase(entry.getKey())) {
					Set<Object> medium = (HashSet<Object>) entry.getValue();
					if (null != medium.iterator().next()) {
						node.getMetadata().put(entry.getKey(), (String) medium.iterator().next());
					}
				} else {
					Set<String> valueSet = (HashSet<String>) entry.getValue();
					String[] value = valueSet.toArray(new String[valueSet.size()]);
					node.getMetadata().put(entry.getKey(), value);
				}
			}
			Set<String> keywords = (HashSet<String>) dataMap.get("keywords");
			if (null != keywords && !keywords.isEmpty()) {
				if (null != node.getMetadata().get("keywords")) {
					Object object = node.getMetadata().get("keywords");
					if (object instanceof String[]) {
						String[] stringArray = (String[]) node.getMetadata().get("keywords");
						keywords.addAll(Arrays.asList(stringArray));
					} else if (object instanceof String) {
						String keyword = (String) node.getMetadata().get("keywords");
						keywords.add(keyword);
					}
				}
				List<String> keywordsList = new ArrayList<>();
				keywordsList.addAll(keywords);
				node.getMetadata().put("keywords", keywordsList);
			}
		}
		
		if(StringUtils.equalsIgnoreCase((String)node.getMetadata().get("visibility"), "Default")) {
			//processCollectionForTOC(node);
		}
		
		util.updateNode(node);
		if(null != dataMap) {
			if(null != dataMap.get("concepts")){
				List<String> concepts = new ArrayList<>();
				concepts.addAll((Collection<? extends String>) dataMap.get("concepts"));
				if (!concepts.isEmpty()) {
					util.addOutRelations(graphId, contentId, concepts, RelationTypes.ASSOCIATED_TO.relationName());
				}
			}
		}
	}
	
	private Integer getCompatabilityLevel(List<NodeDTO> nodes) {
		final Comparator<NodeDTO> comp = (n1, n2) -> Integer.compare( n1.getCompatibilityLevel(), n2.getCompatibilityLevel());
		Optional<NodeDTO> maxNode = nodes.stream().max(comp);
		if (maxNode.isPresent())
			return maxNode.get().getCompatibilityLevel();
		else 
			return 1;
	}

	private List<NodeDTO> dedup(List<NodeDTO> nodes) {
		List<String> ids = new ArrayList<String>();
		List<String> addedIds = new ArrayList<String>();
		List<NodeDTO> list = new ArrayList<NodeDTO>();
		for (NodeDTO node : nodes) {
			if(isImageNode(node.getIdentifier()) && !ids.contains(node.getIdentifier())) {
				ids.add(node.getIdentifier());
			}
		}
		for (NodeDTO node : nodes) {
			if(!ids.contains(node.getIdentifier()) && !ids.contains(getImageNodeID(node.getIdentifier()))) {
				ids.add(node.getIdentifier());
			}
		}
		
		for (NodeDTO node : nodes) {
			if(ids.contains(node.getIdentifier()) && !addedIds.contains(node.getIdentifier()) && !addedIds.contains(getImageNodeID(node.getIdentifier()))) {
				list.add(node);
				addedIds.add(node.getIdentifier());
			}
		}
		
		return list;
	}

	private boolean isImageNode(String identifier) {
		return StringUtils.endsWithIgnoreCase(identifier, DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX);
	}

	private String getImageNodeID(String identifier) {
		return identifier + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX;
	}

	public Stream<NodeDTO> filterAndSortNodes(List<NodeDTO> nodes) {
		return dedup(nodes)
				.stream()
				.filter(node -> !StringUtils.equalsIgnoreCase(node.getVisibility(), "Default"))
				.filter(node -> StringUtils.equalsIgnoreCase(node.getMimeType(), "application/vnd.ekstep.content-collection")
						|| StringUtils.equalsIgnoreCase(node.getStatus(), "Draft"))
				.filter(node -> StringUtils.equalsIgnoreCase(node.getVisibility(), "parent")).sorted(new Comparator<NodeDTO>() {
					@Override
					public int compare(NodeDTO o1, NodeDTO o2) {
						return o2.getDepth().compareTo(o1.getDepth());
					}
				});
	}

	public static void main(String[] args) {
		List<NodeDTO> nodes = new ArrayList<NodeDTO>();
		nodes.add(new NodeDTO("org.ekstep.lit.haircut.story", "Annual Haircut Day", 3, "Live", "application/vnd.ekstep.ecml-archive",
				"Default"));
		nodes.add(new NodeDTO("do_11226563129515212812", "send for review", 3, "Live", "application/vnd.ekstep.ecml-archive", "Default"));
		nodes.add(new NodeDTO("do_11229278865285120011", "My first story", 2, "Live", "application/vnd.ekstep.ecml-archive", "Default"));
		nodes.add(new NodeDTO("domain_58339", "The Ghatotkach", 2, "Live", "application/vnd.ekstep.ecml-archive", "Default"));
		nodes.add(new NodeDTO("domain_58339", "The Ghatotkach", 2, "Live", "application/vnd.ekstep.ecml-archive", "Default"));
		nodes.add(new NodeDTO("do_11229291440146841611", "Test Collection 1", 2, "Live", "application/vnd.ekstep.content-collection",
				"Default"));
		nodes.add(new NodeDTO("do_11229278865285120011", "My first story", 2, "Live", "application/vnd.ekstep.ecml-archive", "Default"));
		nodes.add(new NodeDTO("do_11229292215258316813", "Chapter 1", 1, "Live", "application/vnd.ekstep.content-collection", "Parent"));
		nodes.add(new NodeDTO("do_11229292248830771214", "Chapter 2", 1, "Live", "application/vnd.ekstep.content-collection", "Parent"));
		nodes.add(new NodeDTO("do_11229292248830771214.img", "Chapter 2", 1, "Draft", "application/vnd.ekstep.content-collection", "Parent"));
		nodes.add(new NodeDTO("do_11229292215258316813.img", "Chapter 1", 1, "Draft", "application/vnd.ekstep.content-collection", "Parent"));
		nodes.add(new NodeDTO("do_11229292183262822412", "Testbook 5", 0, "Live", "application/vnd.ekstep.content-collection", "Default"));
		
		
		PublishTask task = new PublishTask("", null);
		Stream<NodeDTO> stream = task.filterAndSortNodes(nodes);
		stream.forEach(nodeDTO -> System.out.println(nodeDTO.getIdentifier() + "," + nodeDTO.getMimeType()));
	}

	private void publishCollectionNode(NodeDTO node, String publishType) {
		Node graphNode = util.getNode("domain", node.getIdentifier());
		if(StringUtils.isNotEmpty(publishType)) {
			graphNode.getMetadata().put("publish_type", publishType);
		}
		publishNode(graphNode, node.getMimeType());
	}

	private void publishNode(Node node, String mimeType) {

		if (null == node)
			throw new ClientException(ContentErrorCodeConstants.INVALID_CONTENT.name(), ContentErrorMessageConstants.INVALID_CONTENT
					+ " | ['null' or Invalid Content Node (Object). Async Publish Operation Failed.]");
		String nodeId = node.getIdentifier().replace(".img", "");
		TelemetryManager.log("Publish processing start for node", nodeId, Level.INFO.name());
		try {
			setContentBody(node, mimeType);
			this.parameterMap.put(ContentWorkflowPipelineParams.node.name(), node);
			this.parameterMap.put(ContentWorkflowPipelineParams.ecmlType.name(), PublishManager.isECMLContent(mimeType));
			InitializePipeline pipeline = new InitializePipeline(PublishManager.getBasePath(nodeId, null), nodeId);
			pipeline.init(ContentWorkflowPipelineParams.publish.name(), this.parameterMap);
		} catch (Exception e) {
			TelemetryManager.log("Something Went Wrong While Performing 'Content Publish' Operation in Async Mode. | [Content Id: " + nodeId
					+ "]", e);
			node.getMetadata().put(ContentWorkflowPipelineParams.publishError.name(), e.getMessage());
			node.getMetadata().put(ContentWorkflowPipelineParams.status.name(), ContentWorkflowPipelineParams.Failed.name());
			util.updateNode(node);
			PublishWebHookInvoker.invokePublishWebKook(contentId, ContentWorkflowPipelineParams.Failed.name(), e.getMessage());
		}
	}

	private void setContentBody(Node node, String mimeType) {
		if (PublishManager.isECMLContent(mimeType)) {
			node.getMetadata().put(ContentAPIParams.body.name(), PublishManager.getContentBody(node.getIdentifier()));
		}
	}

}