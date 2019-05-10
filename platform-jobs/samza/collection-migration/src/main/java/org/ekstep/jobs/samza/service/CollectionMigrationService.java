package org.ekstep.jobs.samza.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.samza.config.Config;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.exception.ServerException;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.enums.RelationTypes;
import org.ekstep.graph.dac.enums.SystemProperties;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.dac.model.Relation;
import org.ekstep.graph.engine.router.GraphEngineManagers;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.jobs.samza.exception.PlatformErrorCodes;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.util.FailedEventsUtil;
import org.ekstep.jobs.samza.util.JSONUtils;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.learning.hierarchy.store.HierarchyStore;
import org.ekstep.learning.router.LearningRequestRouterPool;
import org.ekstep.learning.util.ControllerUtil;


import java.util.*;
import java.util.stream.Collectors;

public class CollectionMigrationService implements ISamzaService {

	protected static final String DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX = ".img";
	private static JobLogger LOGGER = new JobLogger(CollectionMigrationService.class);
	private Config config = null;
	private ObjectMapper mapper = new ObjectMapper();
	private SystemStream systemStream = null;
	private ControllerUtil util = new ControllerUtil();
	private HierarchyStore hierarchyStore = null;
	private List<String> publishedStatus = Arrays.asList("Live", "Unlisted");

	public void initialize(Config config) throws Exception {
		this.config = config;
		JSONUtils.loadProperties(config);
		LOGGER.info("Service config initialized.");
		LearningRequestRouterPool.init();
		LOGGER.info("Akka actors initialized");
		systemStream = new SystemStream("kafka", config.get("output.failed.events.topic.name"));
		LOGGER.info("Stream initialized for Failed Events");
		hierarchyStore = new HierarchyStore();
	}

	@Override
	public void processMessage(Map<String, Object> message, JobMetrics metrics, MessageCollector collector) throws Exception {
		if (null == message) {
			LOGGER.info("Ignoring the message because it is not valid for collection migration.");
			return;
		}
		Map<String, Object> edata = (Map<String, Object>) message.get("edata");
		Map<String, Object> object = (Map<String, Object>) message.get("object");

		if (!validateEdata(edata) || null == object) {
			LOGGER.info("Ignoring the message because it is not valid for collection migration.");
			return;
		}
		try {
			boolean migrationSuccess = true;
			String nodeId = (String) object.get("id");
			if (StringUtils.isNotBlank(nodeId)) {
				Node node = getNode(nodeId);
				if (null != node && validNode(node)) {
					Number version = (Number) node.getMetadata().get("version");
					if (version != null && version.intValue() >= 2) {
						LOGGER.info("Migration is already completed for Content ID: " + node.getIdentifier() + ". So, skipping this message.");
						return;
					}

					List<String> idsToDelete = new ArrayList<>();
					LOGGER.info("Initializing migration for collection ID: " + node.getIdentifier());
					DefinitionDTO definition = getDefinition("domain", "Content");

					// Live Collection Node hierarchy validation and identifying nodes to delete.
					Node liveNode = util.getNode("domain", nodeId);
					String status = (String) liveNode.getMetadata().get("status");
					if (publishedStatus.contains(status)) {
						Map<String, Object> liveHierarchy = hierarchyStore.getHierarchy(nodeId);
						if (MapUtils.isEmpty(liveHierarchy)) {
							LOGGER.info("LIVE MIGRATION: Hierarchy not available for Live Collection. Migrating: " + liveNode.getIdentifier());
							liveHierarchy = util.getHierarchyMap(liveNode.getGraphId(), nodeId, definition, null, null);
							this.hierarchyStore.saveOrUpdateHierarchy(nodeId, liveHierarchy);
							LOGGER.info("LIVE MIGRATION: completed for Live Collection: " + nodeId);
						} else {
							LOGGER.info("Hierarchy available for Live Collection: " + liveNode.getIdentifier());
						}
						List<Map<String, Object>> children = (List<Map<String, Object>>) liveHierarchy.get("children");
						getCollectionIdsToDelete(children, idsToDelete);
					}


					// Edit Node data structure migration and identifying nodes to delete.
					LOGGER.info("Fetching hierarchy from Neo4J DB for Collection: " + node.getIdentifier());
					Map<String, Object> editHierarchy = util.getHierarchyMap("domain", node.getIdentifier(), definition, "edit",null);
					LOGGER.info("Got hierarchy data from Neo4J DB for Collection: " + node.getIdentifier());
					if (MapUtils.isNotEmpty(editHierarchy)) {
						String nodeImgId = nodeId + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX;
						List<Map<String, Object>> children = (List<Map<String, Object>>) editHierarchy.get("children");
						getCollectionIdsToDelete(children, idsToDelete);
						LOGGER.info("Check for image node hierarchy already exist or not.");
						if (MapUtils.isEmpty(hierarchyStore.getHierarchy(nodeImgId))) {
							LOGGER.info("Saving hierarchy to Cassandra.");
							updateCollectionsInHierarchy(children);
							Map<String, Object> hierarchy = new HashMap<>();
							hierarchy.put("identifier", nodeId);
							hierarchy.put("children", children);
							hierarchyStore.saveOrUpdateHierarchy(nodeImgId, hierarchy);
							LOGGER.info("Saved hierarchy to Cassandra.");
						} else {
							LOGGER.info("SKIPPED CASSANDRA SAVE: Hierarchy already exists for image content: " + nodeImgId);
						}

						// Deleting all the collection nodes with visibility: Parent.
						LOGGER.info("Total number of collections to delete: " + idsToDelete.size());
						if (idsToDelete.size() > 0) {
							List<String> liveIds = new ArrayList<>();
							for (String id: idsToDelete) {
								if (StringUtils.endsWith(id, DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX)) {
									liveIds.add(id.replace(DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX, ""));
								}
							}
							idsToDelete.addAll(liveIds);
							LOGGER.info("Nodes to delete: " + mapper.writeValueAsString(idsToDelete));
							List<Response> delResponses = idsToDelete.stream().distinct()
									.map(id -> {
										return util.deleteNode("domain", id);
									}).collect(Collectors.toList());
							LOGGER.info("Nodes delete status: " + new ObjectMapper().writeValueAsString(delResponses));
							List<Response> failed = delResponses.stream()
									.filter(res -> res.getResponseCode() != ResponseCode.OK)
									.collect(Collectors.toList());
							if (failed.size() > 0) {
								migrationSuccess = false;
							}
						}


						// Relations update for Live Node.
						version = (Number) liveNode.getMetadata().get("version");
						LOGGER.info("Live collection node version: " + version);
						if (publishedStatus.contains(status) && (version == null || version.intValue() < 2)) {
							liveNode = util.getNode("domain", nodeId);
							System.out.println("Relations migration required for Content ID: " + nodeId);
							Map<String, Object> liveHierarchy = hierarchyStore.getHierarchy(nodeId);
							if (MapUtils.isNotEmpty(liveHierarchy)) {
								List<Map<String, Object>> leafNodes = getLeafNodes(liveHierarchy, 1);
								LOGGER.info("Total leaf nodes to create relation with root node are " + leafNodes.size());
								if (leafNodes.size() > 0) {
									List<Relation> relations = getRelations(nodeId, leafNodes);
									List<Relation> outRelations = liveNode.getOutRelations();
									if (CollectionUtils.isNotEmpty(outRelations)) {
										List<Relation> filteredRels = outRelations.stream().filter(relation -> {
											Map<String, Object> metadata = relation.getEndNodeMetadata();
											if (MapUtils.isNotEmpty(metadata)) {
												String visibility = (String) metadata.get("visibility");
												return (!StringUtils.equalsIgnoreCase("Parent", visibility));
											} else {
												return true;
											}
										}).collect(Collectors.toList());
										relations.addAll(filteredRels);
									}
									LOGGER.info("Updating out relations with " + new ObjectMapper().writeValueAsString(relations));
									liveNode.setOutRelations(relations);
								}
								liveNode.getMetadata().put("version", 2);
								Response response = util.updateNodeWithoutValidation(liveNode);
								LOGGER.info("Relations update response: " + mapper.writeValueAsString(response));
								if (!util.checkError(response)) {
									LOGGER.info("Updated the collection with new format of relations...");
								} else {
									migrationSuccess = false;
									LOGGER.info("Failed to update relations in new format.");
								}
							} else {
								LOGGER.info("Content Live node hierarchy is empty so, not creating relations for content: "+ nodeId);
							}
						} else {
							LOGGER.info("Content doesn't have Live or Unlisted node to migrate relations.");
						}

						if (migrationSuccess) {
							LOGGER.info("Updating the node version to 2 for collection ID: " + node.getIdentifier());
							node = getNode(nodeId);
							updateNodeVersion(node);
						} else {
							LOGGER.info("Migration failed for collection ID: " + node.getIdentifier() + ". Please check the above logs for more details.");
						}
					} else {
						LOGGER.info("There is no hierarchy data for the content ID: " + node.getIdentifier());
					}
				} else {
					metrics.incSkippedCounter();
					FailedEventsUtil.pushEventForRetry(systemStream, message, metrics, collector,
							PlatformErrorCodes.PROCESSING_ERROR.name(), new ServerException("ERR_COLLECTION_MIGRATION", "Please check neo4j connection or identifier to migrate."));
					LOGGER.info("Invalid Node Object. It is not a collection. Unable to process the event", message);
				}
			} else {
				metrics.incSkippedCounter();
				LOGGER.info("Invalid NodeId. Unable to process the event", message);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateNodeVersion(Node node) throws Exception {
		node.getMetadata().put("version", 2);
		Response response = util.updateNodeWithoutValidation(node);
		if (!util.checkError(response)) {
			LOGGER.info("Updated the node version to 2 for collection ID: " + node.getIdentifier());
			LOGGER.info("Migration completed for collection ID: " + node.getIdentifier());
		} else {
			LOGGER.error("Failed to update the node version to 2 for collection ID: " + node.getIdentifier() + " with error: " + response.getParams().getErrmsg(), response.getResult(), null);
			LOGGER.info("Migration failed for collection ID: " + node.getIdentifier() + ". Please check the above logs for more details.");
		}
	}

	private List<Map<String, Object>> getLeafNodes(Map<String, Object> hierarchy, int depth) {
		List<Map<String, Object>> children = (List<Map<String, Object>>) hierarchy.get("children");
		List<Map<String, Object>> leafNodes = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(children)) {
			int index = 1;
			for (Map<String, Object> child : children) {
				String visibility = (String) child.get("visibility");
				if (StringUtils.equalsIgnoreCase("Parent", visibility)) {
					int currentDepth = depth + 1;
					List<Map<String, Object>> nextLevelLeafNodes = getLeafNodes(child, currentDepth);
					leafNodes.addAll(nextLevelLeafNodes);
				} else {
					child.put("index", index);
					child.put("depth", depth);
					leafNodes.add(child);
					index++;
				}
			}
		}
		return leafNodes;
	}

	private List<Relation> getRelations(String rootId, List<Map<String, Object>> leafNodes) {
		List<Relation> relations = new ArrayList<>();
		for (Map<String, Object> leafNode : leafNodes) {
			String id = (String) leafNode.get("identifier");
			int index = 1;
			Number num = (Number) leafNode.get("index");
			if (num != null) {
				index = num.intValue();
			}
			Relation rel = new Relation(rootId, RelationTypes.SEQUENCE_MEMBERSHIP.relationName(), id);
			Map<String, Object> metadata = new HashMap<>();
			metadata.put(SystemProperties.IL_SEQUENCE_INDEX.name(), index);
			metadata.put("depth", leafNode.get("depth"));
			rel.setMetadata(metadata);
			relations.add(rel);
		}
		return relations;
	}

	private void getCollectionIdsToDelete(List<Map<String, Object>> children, List<String> idsToDelete) {
		if (CollectionUtils.isNotEmpty(children)) {
			children = children.stream().map(child ->  {
				if (StringUtils.equalsIgnoreCase((String) child.get("mimeType"), "application/vnd.ekstep.content-collection") && StringUtils.equalsIgnoreCase((String) child.get("visibility"), "Parent")) {
					idsToDelete.add((String) child.get("identifier"));
				}
				return child;
			}).collect(Collectors.toList());
			List<Map<String, Object>> nextChildren = children.stream()
					.map(child -> (List<Map<String, Object>>) child.get("children"))
					.filter(f -> CollectionUtils.isNotEmpty(f)).flatMap(f -> f.stream())
					.collect(Collectors.toList());
			if (CollectionUtils.isNotEmpty(nextChildren)) {
				getCollectionIdsToDelete(nextChildren, idsToDelete);
			}
		} else {
			LOGGER.info("Children is empty: "+ children);
		}
	}

	private void updateCollectionsInHierarchy(List<Map<String, Object>> children) {
		if (CollectionUtils.isNotEmpty(children)) {
			children = children.stream().map(child ->  {
				if (StringUtils.equalsIgnoreCase((String) child.get("mimeType"), "application/vnd.ekstep.content-collection") && StringUtils.equalsIgnoreCase((String) child.get("visibility"), "Parent")) {
					String id = ((String) child.get("identifier")).replaceAll(".img", "");
					child.put("status", "Draft");
					child.put("objectType", "Content");
					child.put("identifier", id);
				}
				return child;
			}).collect(Collectors.toList());
			List<Map<String, Object>> nextChildren = children.stream()
					.map(child -> (List<Map<String, Object>>) child.get("children"))
					.filter(f -> CollectionUtils.isNotEmpty(f)).flatMap(f -> f.stream())
					.collect(Collectors.toList());
			if (CollectionUtils.isNotEmpty(nextChildren)) {
				updateCollectionsInHierarchy(nextChildren);
			}
		} else {
			LOGGER.info("Children is empty: "+ children);
		}
	}

	/**
	 * Checking is it a valid node for migration.
	 *
	 * @param node
	 * @return boolean
	 */
	private boolean validNode(Node node) {
		Map<String, Object> metadata = node.getMetadata();
		String visibility = (String) metadata.get("visibility");
		String mimeType = (String) metadata.get("mimeType");
		return (StringUtils.equalsIgnoreCase("Default", visibility) && StringUtils.equalsIgnoreCase("application/vnd.ekstep.content-collection", mimeType));
	}

	private boolean validateEdata(Map<String, Object> edata) {
		String action = (String) edata.get("action");
		return (StringUtils.equalsIgnoreCase("collection-migration", action));
	}

	private Node getNode(String nodeId) {
		String imgNodeId = nodeId + DEFAULT_CONTENT_IMAGE_OBJECT_SUFFIX;
		Node node = util.getNode("domain", imgNodeId);
		if (null == node) {
			node = util.getNode("domain", nodeId);
		}
		return node;
	}

	protected DefinitionDTO getDefinition(String graphId, String objectType) {
		Request request = util.getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "getNodeDefinition",
				GraphDACParams.object_type.name(), objectType);
		Response response = util.getResponse(request);
		if (!util.checkError(response)) {
			DefinitionDTO definition = (DefinitionDTO) response.get(GraphDACParams.definition_node.name());
			return definition;
		}
		return null;
	}
}