package org.sunbird.graph.model.node;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.sunbird.common.Platform;
import org.sunbird.common.dto.Property;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.common.exception.ServerException;
import org.sunbird.graph.common.DateUtils;
import org.sunbird.graph.common.mgr.BaseGraphManager;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.enums.RelationTypes;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.enums.SystemProperties;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.Relation;
import org.sunbird.graph.exception.GraphEngineErrorCodes;
import org.sunbird.graph.exception.GraphRelationErrorCodes;
import org.sunbird.graph.model.IRelation;
import org.sunbird.graph.model.cache.CategoryCache;
import org.sunbird.graph.model.cache.DefinitionCache;
import org.sunbird.graph.model.relation.RelationHandler;
import org.sunbird.telemetry.logger.TelemetryManager;

import akka.dispatch.Futures;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.Promise;

import static java.util.stream.Collectors.toSet;

public class DataNode extends AbstractNode {

	private String objectType;
	private List<Relation> inRelations;
	private List<Relation> outRelations;

	public DataNode(BaseGraphManager manager, String graphId, String nodeId, String objectType,
			Map<String, Object> metadata) {
		super(manager, graphId, nodeId, metadata);
		this.objectType = objectType;
	}

	public DataNode(BaseGraphManager manager, String graphId, Node node, boolean skipRelationUpdate) {
		super(manager, graphId, node.getIdentifier(), node.getMetadata());
		this.objectType = node.getObjectType();
		this.inRelations = node.getInRelations();
		this.outRelations = node.getOutRelations();
	}

	public DataNode(BaseGraphManager manager, String graphId, Node node) {
		super(manager, graphId, node.getIdentifier(), node.getMetadata());
		this.objectType = node.getObjectType();
		this.inRelations = node.getInRelations();
		this.outRelations = node.getOutRelations();
		if (this.inRelations != null) {
			for (Relation relation : this.inRelations)
				relation.setEndNodeId(getNodeId());
		}
		if (this.outRelations != null) {
			int index = 0;
			for (Relation relation : this.outRelations) {
				if (StringUtils.equalsIgnoreCase(RelationTypes.SEQUENCE_MEMBERSHIP.relationName(),
						relation.getRelationType())) {
					Map<String, Object> metadata = relation.getMetadata();
					if (null != metadata && !metadata.isEmpty()) {
						Integer idx = null;
						try {
							String str = (String) metadata.get(SystemProperties.IL_SEQUENCE_INDEX.name());
							idx = Integer.parseInt(str);
						} catch (Exception e) {
						}
						if (null != idx && index < idx.intValue())
							index = idx.intValue() + 1;
						else
							++index;
					}
				}
				relation.setStartNodeId(getNodeId());
			}
			for (Relation relation : this.outRelations) {
				if (StringUtils.equalsIgnoreCase(RelationTypes.SEQUENCE_MEMBERSHIP.relationName(),
						relation.getRelationType())) {
					Map<String, Object> metadata = (null == relation.getMetadata()) ? new HashMap<String, Object>()
							: relation.getMetadata();
					Integer idx = null;
					try {
						String str = (String) metadata.get(SystemProperties.IL_SEQUENCE_INDEX.name());
						idx = Integer.parseInt(str);
					} catch (Exception e) {
					}
					if (null == idx) {
						idx = index++;
						// metadata.put(SystemProperties.IL_SEQUENCE_INDEX.name(),
						// idx);
						relation.setMetadata(metadata);
					}
				}
			}
		}
	}

	public void removeExternalFields() {
		DefinitionDTO dto = DefinitionCache.getDefinitionNode(graphId, objectType);
		if (null != dto) {
			List<MetadataDefinition> properties = dto.getProperties();
			if (null != properties && !properties.isEmpty()) {
				for (MetadataDefinition def : properties) {
					if (StringUtils.equalsIgnoreCase("external", def.getDataType())) {
						if (null != metadata && metadata.containsKey(def.getPropertyName()))
							metadata.put(def.getPropertyName(), null);
					}
				}
			}
		}
	}

	@Override
	public Node toNode() {
		Node node = new Node(getNodeId(), getSystemNodeType(), getFunctionalObjectType());
		node.setMetadata(this.metadata);
		node.setInRelations(inRelations);
		node.setOutRelations(outRelations);
		return node;
	}

	@Override
	public String getSystemNodeType() {
		return SystemNodeTypes.DATA_NODE.name();
	}

	@Override
	public String getFunctionalObjectType() {
		return this.objectType;
	}

	public Node getNodeObject(Request req) {
		Request request = new Request(req);
		request.put(GraphDACParams.node_id.name(), getNodeId());
		Response res = searchMgr.getNodeByUniqueId(request);
		Node node = null;
		if (null != res.get(GraphDACParams.node.name()))
			node = (Node) res.get(GraphDACParams.node.name());
		return node;
	}

	public List<Relation> getNewRelationList() {
		List<Relation> relations = new ArrayList<Relation>();
		if (null != getInRelations() && !getInRelations().isEmpty()) {
			for (Relation inRel : getInRelations()) {
				inRel.setEndNodeId(getNodeId());
				relations.add(inRel);
			}
		}
		if (null != getOutRelations() && !getOutRelations().isEmpty()) {
			for (Relation outRel : getOutRelations()) {
				outRel.setStartNodeId(getNodeId());
				relations.add(outRel);
			}
		}
		return relations;
	}

	public Future<List<String>> deleteRelations(final Request request, final ExecutionContext ec,
			List<Relation> delRels) {
		final Promise<List<String>> promise = Futures.promise();
		Future<List<String>> relFuture = promise.future();
		List<IRelation> relations = new ArrayList<IRelation>();
		if (null != delRels && !delRels.isEmpty()) {
			for (Relation rel : delRels) {
				IRelation relation = RelationHandler.getRelation(manager, getGraphId(), rel.getStartNodeId(),
						rel.getRelationType(), rel.getEndNodeId(), rel.getMetadata());
				relations.add(relation);
			}
		}
		if (null != relations && !relations.isEmpty()) {
			List<String> futures = new ArrayList<String>();
			for (IRelation rel : relations) {
				String msg = rel.deleteRelation(request);
				futures.add(msg);
			}
			List<String> messages = new ArrayList<String>();

			if (!futures.isEmpty()) {
				for (String msg : futures) {
					if (StringUtils.isNotBlank(msg)) {
						messages.add(msg);
					}
				}
			}
			promise.success(messages);
		} else {
			promise.success(null);
		}
		return relFuture;
	}

	public Future<List<String>> createRelations(final Request request, final ExecutionContext ec,
			List<Relation> addRels) {
		final Promise<List<String>> promise = Futures.promise();
		Future<List<String>> relFuture = promise.future();
		final List<IRelation> relations = new ArrayList<IRelation>();
		if (null != addRels && !addRels.isEmpty()) {
			for (Relation rel : addRels) {
				IRelation relation = RelationHandler.getRelation(manager, getGraphId(), rel.getStartNodeId(),
						rel.getRelationType(), rel.getEndNodeId(), rel.getMetadata());
				relations.add(relation);
			}
		}
		if (null != relations && !relations.isEmpty()) {
			List<List<String>> futures = new ArrayList<List<String>>();
			for (IRelation rel : relations) {
				Map<String, List<String>> msgFuture = rel.validateRelation(request);
				futures.addAll(msgFuture.values());
			}
			List<String> messages = new ArrayList<String>();
			if (null != futures) {
				for (List<String> list : futures) {
					if (null != list && !list.isEmpty()) {
						messages.addAll(list);
					}
				}
			}

			if (messages.isEmpty()) {
				createRelations(relations, request, ec, promise);
			} else {
				promise.success(messages);
			}
		} else {
			promise.success(null);
		}
		return relFuture;
	}

	private void createRelations(List<IRelation> relations, final Request request, ExecutionContext ec,
			final Promise<List<String>> promise) {
		if (null != relations && !relations.isEmpty()) {
			List<String> futures = new ArrayList<String>();
			for (IRelation rel : relations) {
				String msg = rel.createRelation(request);
				futures.add(msg);
			}

			List<String> messages = new ArrayList<String>();
			if (!futures.isEmpty()) {
				for (String msg : futures) {
					if (StringUtils.isNotBlank(msg)) {
						messages.add(msg);
					}
				}
			}
			promise.success(messages);
		} else {
			promise.success(null);
		}
	}

	@Override
	public void removeProperty(Request req) {
		try {
			Request request = new Request(req);
			request.copyRequestValueObjects(req.getRequest());
			Response response = nodeMgr.removePropertyValue(request);
			manager.returnResponse(Futures.successful(response), getParent());
		} catch (Exception e) {
			manager.ERROR(e, getParent());
		}
	}

	@Override
	public void setProperty(Request req) {
		Property property = (Property) req.get(GraphDACParams.metadata.name());
		if (!manager.validateRequired(property)) {
			throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_UPDATE_NODE_MISSING_REQ_PARAMS.name(),
					"Required parameters are missing...");
		} else {
			checkMetadata(property.getPropertyName(), property.getPropertyValue());
			try {
				Request request = new Request(req);
				request.copyRequestValueObjects(req.getRequest());
				Response response = nodeMgr.updatePropertyValue(request);
				manager.returnResponse(Futures.successful(response), getParent());
			} catch (Exception e) {
				manager.ERROR(e, getParent());
			}
		}
	}

	public Response createNode(final Request req) {
		Request request = new Request(req);
		request.put(GraphDACParams.node.name(), toNode());
		Response res = nodeMgr.addNode(request);

		if (manager.checkError(res)) {
			return res;
		} else {
			String identifier = (String) res.get(GraphDACParams.node_id.name());
			String versionKey = (String) res.get(GraphDACParams.versionKey.name());
			if (manager.validateRequired(identifier) && manager.validateRequired(versionKey)) {
				setNodeId(identifier);
				setVersionKey(versionKey);
				return res;
			} else {
				return manager.getErrorResponse(GraphEngineErrorCodes.ERR_GRAPH_CREATE_NODE_ERROR.name(),
						"Error creating node in the graph", ResponseCode.SERVER_ERROR);
			}
		}
	}

	public Response updateNode(Request req) {
		checkMetadata(metadata);
		Request request = new Request(req);
		request.put(GraphDACParams.node.name(), toNode());
		Response response = nodeMgr.updateNode(request);
		if (manager.checkError(response)) {
			return response;
		} else {
			String identifier = (String) response.get(GraphDACParams.node_id.name());
			String versionKey = (String) response.get(GraphDACParams.versionKey.name());
			if (manager.validateRequired(identifier) && manager.validateRequired(versionKey)) {
				setNodeId(identifier);
				setVersionKey(versionKey);
				return response;
			} else {
				return manager.getErrorResponse(GraphEngineErrorCodes.ERR_GRAPH_UPDATE_NODE_ERROR.name(),
						"Error updating node in the graph", ResponseCode.SERVER_ERROR);
			}
		}
	}

	@Override
	public Map<String, List<String>> validateNode(Request req) {
		checkMetadata(metadata);
		try {
			final List<String> messages = new ArrayList<String>();
			Map<String, List<String>> map = new HashMap<String, List<String>>();
			if (StringUtils.isBlank(objectType)) {
				messages.add("Object type not set for node: " + getNodeId());
			} else {
				DefinitionDTO dto = DefinitionCache.getDefinitionNode(graphId, objectType);
				if (null != dto) {
					validateMetadata(dto.getProperties(), messages);
					validateRelations(dto.getInRelations(), "incoming", messages);
					validateRelations(dto.getOutRelations(), "outgoing", messages);
				} else {
					messages.add("Definition node not found for Object Type: " + objectType);
				}
			}
			map.put(getNodeId(), messages);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_GET_PROPERTY.name(), e.getMessage(), e);
		}
	}

	public List<String> validateNode(DefinitionDTO dto) {
		List<String> messages = new ArrayList<String>();
		if (StringUtils.isBlank(objectType)) {
			messages.add("Object type not set for node: " + getNodeId());
		} else {
			validateMetadata(dto.getProperties(), messages);
			List<RelationDefinition> inRelDefs = dto.getInRelations();
			validateRelations(inRelDefs, "incoming", messages);
			List<RelationDefinition> outRelDefs = dto.getOutRelations();
			validateRelations(outRelDefs, "outgoing", messages);
		}
		return messages;
	}

	public List<String> validateNode(Map<String, Node> defNodesMap) {
		try {
			List<String> messages = new ArrayList<String>();
			if (StringUtils.isBlank(objectType)) {
				messages.add("Object type not set for node: " + getNodeId());
			} else {
				if (null == defNodesMap)
					defNodesMap = new HashMap<String, Node>();
				Node defNode = defNodesMap.get(objectType);
				if (null == defNode) {
					messages.add("Definition node not found for Object Type: " + objectType);
				} else {
					DefinitionNode definitionNode = new DefinitionNode(getManager(), defNode);
					validateMetadata(definitionNode.getIndexedMetadata(), messages);
					validateMetadata(definitionNode.getNonIndexedMetadata(), messages);
					List<RelationDefinition> inRelDefs = definitionNode.getInRelations();
					validateRelations(inRelDefs, "incoming", messages);
					List<RelationDefinition> outRelDefs = definitionNode.getOutRelations();
					validateRelations(outRelDefs, "outgoing", messages);
				}
			}
			return messages;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_GET_PROPERTY.name(), e.getMessage(), e);
		}
	}

	private void validateRelations(List<RelationDefinition> relDefs, String direction, List<String> messages) {
		List<RelationDefinition> requiredRels = new ArrayList<RelationDefinition>();
		if (null != relDefs && !relDefs.isEmpty()) {
			for (RelationDefinition def : relDefs) {
				if (def.isRequired()) {
					requiredRels.add(def);
				}
			}
		}
		List<Relation> rels = null;
		if (StringUtils.equals("incoming", direction)) {
			rels = getInRelations();
		} else {
			rels = getOutRelations();
		}
		if (null != rels && !rels.isEmpty()) {
			for (Relation rel : rels) {
				if (!RelationTypes.isValidRelationType(rel.getRelationType()))
					messages.add("Relation " + rel.getRelationType() + " is not supported");
			}
		}
		if (!requiredRels.isEmpty()) {
			if (null == rels || rels.isEmpty()) {
				messages.add("Required " + direction + " relations are missing");
			} else {
				for (RelationDefinition def : requiredRels) {
					boolean found = false;
					for (Relation rel : rels) {
						if (StringUtils.equals(def.getRelationName(), rel.getRelationType())) {
							found = true;
							break;
						}
					}
					if (!found) {
						messages.add("Required " + direction + " relation " + def.getRelationName() + " is missing");
					}
				}
			}
		}
	}

	private void validateMetadataProperties(List<MetadataDefinition> defs, List<String> messages) {
		List<String> invalidProps = new ArrayList<>();
		List<String> validObjectTypes = Platform.config.hasPath("restrict.metadata.objectTypes") ?
				Arrays.asList(Platform.config.getString("restrict.metadata.objectTypes").split(",")) : Collections.emptyList();
		if (validObjectTypes.contains(objectType)) {
			Set<String> properties = defs.stream().map(MetadataDefinition::getPropertyName).collect(toSet());
			metadata.keySet().forEach(e  -> {
				if (!properties.contains(e) && !SystemProperties.isSystemProperty(e)) invalidProps.add(e);
			});
			if (!invalidProps.isEmpty()) messages.add("Invalid Properties : " + invalidProps.toString());
		}
	}

	private void validateMetadata(List<MetadataDefinition> defs, List<String> messages) {
		validateMetadataProperties(defs, messages);
		if (null != defs && !defs.isEmpty()) {
			for (MetadataDefinition def : defs) {
				String propName = def.getPropertyName();
				if (StringUtils.isNotBlank(propName)) {
					Object value = getPropertyValue(propName);
					if (null == value) {
						if (null != def.getDefaultValue() && StringUtils.isNotBlank(def.getDefaultValue().toString()))
							value = setPropertyValue(def, propName, def.getDefaultValue());
					} else if (value instanceof String) {
						if (StringUtils.isBlank((String) value) && null != def.getDefaultValue()
								&& StringUtils.isNotBlank(def.getDefaultValue().toString()))
							value = setPropertyValue(def, propName, def.getDefaultValue());
					}
					if (def.isRequired()) {
						if (null == value)
							messages.add("Required Metadata " + propName + " not set");
						else if (value instanceof String) {
							if (StringUtils.isBlank((String) value))
								messages.add("Required Metadata " + propName + " not set");
						}
					}
					checkDataType(value, def, messages);
				}
			}
		}
	}

	private Object getPropertyValue(String propName) {
		if (null != metadata && !metadata.isEmpty() && StringUtils.isNotBlank(propName)) {
			return metadata.get(propName);
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	private Object setPropertyValue(MetadataDefinition def, String propName, Object value) {
		if (StringUtils.isNotBlank(propName) && null != value) {
			if (null == metadata)
				metadata = new HashMap<String, Object>();
			String datatype = def.getDataType();
			if (null != value) {
				if (StringUtils.equalsIgnoreCase("list", datatype)
						|| StringUtils.equalsIgnoreCase("multi-select", datatype)) {
					if (value instanceof List) {
						value = ((List) value).toArray();
					} else if (!(value instanceof Object[])) {
						value = new String[] { value.toString() };
					}
				} else if (StringUtils.equalsIgnoreCase("boolean", datatype)) {
					try {
						Boolean b = new Boolean(value.toString());
						value = b;
					} catch (Exception e) {
					}
				} else if (StringUtils.equalsIgnoreCase("number", datatype)) {
					try {
						BigDecimal bd = new BigDecimal(value.toString());
						value = bd.doubleValue();
					} catch (Exception e) {
					}
				}
			}
			metadata.put(propName, value);
		}
		return value;
	}

	@SuppressWarnings("rawtypes")
	private void checkDataType(Object value, MetadataDefinition def, List<String> messages) {
		if (null != value) {
			String propName = def.getPropertyName();
			String dataType = def.getDataType();
			List<Object> range = def.getRange();

			// TODO: the below if condition is to allow term and termlist as
			// datatypes.
			if (StringUtils.isNotBlank(dataType)
					&& MetadataDefinition.PLATFORM_OBJECTS_AS_DATA_TYPE.contains(dataType.toLowerCase())) {
				if (def.getRangeValidation()) {
					String framework = (String) this.metadata.get("framework");
					if (StringUtils.isBlank(framework)) {
						messages.add("Please provide framework.");
						return;
					} else {
						if (StringUtils.endsWithIgnoreCase(dataType, "list")) {
							dataType = "multi-select";
						} else {
							dataType = "select";
						}
						List<Object> terms = CategoryCache.getTerms(framework, propName);
						if (null != terms && !terms.isEmpty()) {
							range = terms;
							TelemetryManager.log("Setting range from terms for data validation. framework: " + framework
									+ ", category: " + propName);
						} else {
							messages.add("Please select a valid framework. This framework doesn't have category: "
									+ propName);
							return;
						}
					}
				} else {
					return;
				}
			}

			if (StringUtils.equalsIgnoreCase("text", dataType) && !(value instanceof String)) {
				messages.add("Metadata " + propName + " should be a String value");
			} else if (StringUtils.equalsIgnoreCase("boolean", dataType) && !(value instanceof Boolean)) {
				messages.add("Metadata " + propName + " should be a Boolean value");
			} else if (StringUtils.equalsIgnoreCase("number", dataType) && !(value instanceof Number)) {
				messages.add("Metadata " + propName + " should be a Numeric value");
			} else if (StringUtils.equalsIgnoreCase("date", dataType)) {
				if (value instanceof Date)
					value = setPropertyValue(def, propName, DateUtils.format((Date) value));
				else {
					value = DateUtils.format(DateUtils.parse(value.toString()));
					value = setPropertyValue(def, propName, value);
				}
			} else if (StringUtils.equalsIgnoreCase("select", dataType)) {
				if (null == range || range.isEmpty())
					messages.add("Metadata " + propName + " should be one of: " + range);
				else {
					if (!checkRangeValue(range, value))
						messages.add("Metadata " + propName + " should be one of: " + range);
				}
			} else if (StringUtils.equalsIgnoreCase("multi-select", dataType)) {
				if (null == range || range.isEmpty()) {
					messages.add("Metadata " + propName + " should be one of: " + range);
				} else {
					if (!(value instanceof Object[]) && !(value instanceof List))
						messages.add("Metadata " + propName + " should be a list of values from: " + range);
					else {
						if (value instanceof Object[]) {
							int length = Array.getLength(value);
							for (int i = 0; i < length; i++) {
								if (!checkRangeValue(range, Array.get(value, i))) {
									messages.add("Metadata " + propName + " should be one of: " + range);
									break;
								}
							}
						} else if (value instanceof List) {
							List list = (List) value;
							for (Object object : list) {
								if (!checkRangeValue(range, object)) {
									messages.add("Metadata " + propName + " should be one of: " + range);
									break;
								}
							}
						}
					}
				}
			} else if (StringUtils.equalsIgnoreCase("list", dataType)) {
				if (!(value instanceof Object[]) && !(value instanceof List))
					messages.add("Metadata " + propName + " should be a list");
			} else if (StringUtils.equalsIgnoreCase("json", dataType)) {
				ObjectMapper mapper = new ObjectMapper();
				try {
					mapper.readValue((String) value, Map.class);
				} catch (Exception e) {
					try {
						mapper.readValue((String) value, List.class);
					} catch (Exception ex) {
						try {
							mapper.readValue(mapper.writeValueAsString(value), Map.class);
						} catch (Exception e2) {
							try {
								mapper.readValue(mapper.writeValueAsString(value), List.class);
							} catch (Exception ex2) {
								messages.add("Metadata " + propName + " should be a valid JSON");
							}
						}
					}
				}
			} else if (StringUtils.equalsIgnoreCase("xml", dataType)) {
				// TODO need to implement the xml validation.
			}
		}
	}

	private boolean checkRangeValue(List<Object> range, Object value) {
		boolean found = false;
		for (Object rangeVal : range) {
			if (rangeVal instanceof String) {
				if (StringUtils.equalsIgnoreCase((String) value, (String) rangeVal)) {
					found = true;
					break;
				}
			} else {
				if (rangeVal == value) {
					found = true;
					break;
				}
			}
		}
		return found;
	}

	public List<Relation> getInRelations() {
		return inRelations;
	}

	public void setInRelations(List<Relation> inRelations) {
		this.inRelations = inRelations;
	}

	public List<Relation> getOutRelations() {
		return outRelations;
	}

	public void setOutRelations(List<Relation> outRelations) {
		this.outRelations = outRelations;
	}

}
