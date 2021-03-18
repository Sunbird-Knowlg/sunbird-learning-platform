package org.sunbird.graph.engine.mgr.impl;

import static java.util.stream.Collectors.toSet;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.sunbird.common.Platform;
import org.sunbird.common.dto.Property;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.dto.ResponseParams;
import org.sunbird.common.dto.ResponseParams.StatusType;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.common.exception.ServerException;
import org.sunbird.graph.common.DateUtils;
import org.sunbird.graph.common.enums.GraphHeaderParams;
import org.sunbird.graph.dac.enums.AuditProperties;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.enums.SystemProperties;
import org.sunbird.graph.dac.mgr.IGraphDACNodeMgr;
import org.sunbird.graph.dac.mgr.IGraphDACSearchMgr;
import org.sunbird.graph.dac.mgr.impl.Neo4JBoltNodeMgrImpl;
import org.sunbird.graph.dac.mgr.impl.Neo4JBoltSearchMgrImpl;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.Relation;
import org.sunbird.graph.exception.GraphEngineErrorCodes;
import org.sunbird.graph.exception.GraphRelationErrorCodes;
import org.sunbird.graph.model.Graph;
import org.sunbird.graph.model.cache.CategoryCache;
import org.sunbird.graph.model.cache.DefinitionCache;
import org.sunbird.graph.model.node.DataNode;
import org.sunbird.graph.model.node.DefinitionDTO;
import org.sunbird.graph.model.node.MetadataDefinition;
import org.sunbird.telemetry.logger.TelemetryManager;

import akka.actor.ActorRef;
import scala.concurrent.ExecutionContext;

public class NodeManager {
	
	private IGraphDACSearchMgr searchMgr = new Neo4JBoltSearchMgrImpl();
	private IGraphDACNodeMgr nodeMgr = new Neo4JBoltNodeMgrImpl();

	public DefinitionDTO getNodeDefinition(Request request) {
        String objectType = (String) request.get(GraphDACParams.object_type.name());
        System.out.println("NodeManager:getNodeDefinition:: " + objectType);
        if (!validateRequired(objectType)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_MISSING_REQ_PARAMS.name(),
                    "GetNodeDefinition: Required parameters are missing...");
        } else {
            String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
            try {
                DefinitionDTO definition = DefinitionCache.getDefinitionNode(graphId, objectType);
                System.out.println("NodeManager:getNodeDefinition:definition:id:: " + definition.getIdentifier());
                System.out.println("NodeManager:getNodeDefinition:definition:properties:: " + definition.getProperties());
    			if (null != definition)
    				return definition; 
    			else {
    				throw new ServerException(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_NODE_NOT_FOUND.name(),
    						"Failed to get definition node");
    			}
            } catch (Exception e) {
            	throw new ServerException("Something went wrong while fetching definition" , e.getMessage(), e);

            }
        }
    }
	public Response getDataNode(Request request) {
        String nodeId = (String) request.get(GraphDACParams.node_id.name());
        if (!validateRequired(nodeId)) 
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_SEARCH_MISSING_REQ_PARAMS.name(),
                    "GetDataNode: Required parameters are missing...");
        else 
            try {
                return searchMgr.getNodeByUniqueId(request);
            } catch (Exception e) {
            	throw new ServerException("ERR_FETCH_NODE", "Error while fetch data.", e);
            }
    }

	public void deleteDataNode(Request request) throws ServerException {
		String nodeId = (String) request.get(GraphDACParams.node_id.name());
		if (!validateRequired(nodeId)) {
			throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_REMOVE_NODE_MISSING_REQ_PARAMS.name(),
					"Required parameters are missing...");
		} else {
			try {
				IGraphDACNodeMgr nodeMgr = new Neo4JBoltNodeMgrImpl();
				nodeMgr.deleteNode(request);
			} catch (Exception e) {
				throw new ServerException("ERR_DELETE_NODE", "Error while deleting data.", e);
			}
		}
	}

	public Response updateDataNode(Request request) {
		//final ActorRef parent = getSender();
		String graphId = (String) request.getContext().get(GraphHeaderParams.graph_id.name());
		String nodeId = (String) request.get(GraphDACParams.node_id.name());
		final Node dataNode = (Node) request.get(GraphDACParams.node.name());
		final Boolean skipValidations = (Boolean) request.get(GraphDACParams.skip_validations.name());
		Response updateResponse = null;
		if (!validateRequired(nodeId, dataNode)) {
			throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_UPDATE_NODE_MISSING_REQ_PARAMS.name(),
					"Required parameters are missing...");
		} else {
			System.out.println("***** Inside NodeManager:updateDataNode *****");
			dataNode.setIdentifier(nodeId);

			Response response = searchMgr.getNodeByUniqueId(request);
			System.out.println("***** Inside NodeManager:updateDataNode:response: " + response.getResponseCode());
			Node dbNode = null;
			if(checkError(response))
				return response;
			dbNode = (Node) response.get(GraphDACParams.node.name());


			//final DataNode datanode = new DataNode(this, graphId, node);
			final List<String> messages = new ArrayList<String>();
			final List<Relation> addRels = new ArrayList<Relation>();
			final List<Relation> delRels = new ArrayList<Relation>();
			final List<Node> dbNodes = new ArrayList<Node>();
			String date = DateUtils.formatCurrentDate();


			System.out.println("***** Inside NodeManager:updateDataNode:dbNode: " + dbNode.getIdentifier());
			if (null != dbNode && StringUtils.equals(SystemNodeTypes.DATA_NODE.name(), dbNode.getNodeType())) {
				if (null == dataNode.getMetadata()) {
					dataNode.setMetadata(new HashMap<String, Object>());
				}
				Map<String, Object> dbMetadata = dbNode.getMetadata();
				if (null != dbMetadata && !dbMetadata.isEmpty()) {
					System.out.println("***** Inside NodeManager:updateDataNode:dbMetadata: " + dbMetadata);
					dbMetadata.remove(GraphDACParams.versionKey.name());
					dbMetadata.remove(GraphDACParams.lastUpdatedBy.name());
					// add lastStatusChangedOn if status got changed
					String currentStatus = (String) dataNode.getMetadata().get(GraphDACParams.status.name());
					String previousStatus = (String) dbMetadata.get(GraphDACParams.status.name());
					if (StringUtils.isNotBlank(currentStatus) && !StringUtils.equalsIgnoreCase(currentStatus, previousStatus)) {
						dataNode.getMetadata().put(AuditProperties.lastStatusChangedOn.name(), date);
						dataNode.getMetadata().put(AuditProperties.prevStatus.name(), previousStatus);
					}
					for (Entry<String, Object> entry : dbMetadata.entrySet()) {
						if (!dataNode.getMetadata().containsKey(entry.getKey()))
							dataNode.getMetadata().put(entry.getKey(), entry.getValue());
					}
				}
				//getRelationsDelta(addRels, delRels, dbNode, dataNode);
				dbNodes.add(dbNode);
			}
			System.out.println("***** Inside NodeManager:updateDataNode:dbNodes: " + dbNodes.size());
			if (!dbNodes.isEmpty()) {
				// validate the node
				if (null == skipValidations || !skipValidations) {
					Map<String, List<String>> validationMap = validateNode(dataNode, graphId);
					if (!validationMap.isEmpty()) {
						for (List<String> list : validationMap.values()) {
							if (null != list && !list.isEmpty()) {
								for (String msg : list) {
									messages.add(msg);
								}
							}
						}
					}
				}
				System.out.println("***** Inside NodeManager:updateDataNode:messages: " + messages);
				if (messages.isEmpty()) {
					removeExternalFields(graphId, dataNode);
					System.out.println("***** Inside NodeManager:updateDataNode:pre***** ");
					updateResponse = updateNode(request);
					System.out.println("***** Inside NodeManager:updateDataNode:updateResponse: " + updateResponse.getResponseCode());
				} else {
					return getErrorResponse(GraphEngineErrorCodes.ERR_GRAPH_UPDATE_NODE_VALIDATION_FAILED.name(),
							"Node Metadata validation failed", ResponseCode.CLIENT_ERROR,
							GraphDACParams.messages.name(), messages);
				}

			} else {
				return getErrorResponse(GraphEngineErrorCodes.ERR_GRAPH_UPDATE_NODE_NOT_FOUND.name(), "Node Not Found",
						ResponseCode.RESOURCE_NOT_FOUND, GraphDACParams.messages.name(), messages);
			}
		}
		return updateResponse;
	}

	private Response getErrorResponse(String errorCode, String errorMessage, ResponseCode code, String responseIdentifier, Object vo) {
        Response response = new Response();
        response.put(responseIdentifier, vo);
        response.setParams(getErrorStatus(errorCode, errorMessage));
        response.setResponseCode(code);
        return response;

    }

	private boolean checkError(Response response) {
        ResponseParams params = response.getParams();
        if (null != params) {
            if (StringUtils.equals(StatusType.failed.name(), params.getStatus())) {
                return true;
            }
        }
        return false;
    }

	private Response updateNode(Request request) {
		Response response = nodeMgr.updateNode(request);
		if (!checkError(response)) {
			String identifier = (String) response.get(GraphDACParams.node_id.name());
			String versionKey = (String) response.get(GraphDACParams.versionKey.name());
			if (!validateRequired(identifier) || !validateRequired(versionKey)) {
				return getErrorResponse(GraphEngineErrorCodes.ERR_GRAPH_UPDATE_NODE_ERROR.name(),
						"Error updating node in the graph", ResponseCode.SERVER_ERROR);
			} 
		}
		return response;
	}

	private Response getErrorResponse(String errorCode, String errorMessage, ResponseCode code) {
    	Response response = new Response();
        response.setParams(getErrorStatus(errorCode, errorMessage));
        response.setResponseCode(code);
        return response;
    }
	private ResponseParams getErrorStatus(String errorCode, String errorMessage) {
        ResponseParams params = new ResponseParams();
        params.setErr(errorCode);
        params.setStatus(StatusType.failed.name());
        params.setErrmsg(errorMessage);
        return params;
    }

	private Map<String, List<String>> validateNode(Node node, String graphId) {
		checkMetadata(node.getMetadata());
		try {
			final List<String> messages = new ArrayList<String>();
			Map<String, List<String>> map = new HashMap<String, List<String>>();
			if (StringUtils.isBlank(node.getObjectType())) {
				messages.add("Object type not set for node: " + node.getIdentifier());
			} else {
				DefinitionDTO dto = DefinitionCache.getDefinitionNode(graphId, node.getObjectType());
				if (null != dto) {
					validateMetadata(dto.getProperties(), messages, node);

				} else {
					messages.add("Definition node not found for Object Type: " + node.getObjectType());
				}
			}
			map.put(node.getIdentifier(), messages);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServerException("ERR_VALIDATE_NODE", "Error while validating node metadata.", e);
		}
	}

	private void validateMetadata(List<MetadataDefinition> defs, List<String> messages, Node node) {
		validateMetadataProperties(defs, messages, node);
		if (null != defs && !defs.isEmpty()) {
			for (MetadataDefinition def : defs) {
				String propName = def.getPropertyName();
				if (StringUtils.isNotBlank(propName)) {
					Object value = getPropertyValue(propName, node);
					if (null == value) {
						if (null != def.getDefaultValue() && StringUtils.isNotBlank(def.getDefaultValue().toString()))
							value = setPropertyValue(def, propName, def.getDefaultValue(), node);
					} else if (value instanceof String) {
						if (StringUtils.isBlank((String) value) && null != def.getDefaultValue()
								&& StringUtils.isNotBlank(def.getDefaultValue().toString()))
							value = setPropertyValue(def, propName, def.getDefaultValue(), node);
					}
					if (def.isRequired()) {
						if (null == value)
							messages.add("Required Metadata " + propName + " not set");
						else if (value instanceof String) {
							if (StringUtils.isBlank((String) value))
								messages.add("Required Metadata " + propName + " not set");
						}
					}
					checkDataType(value, def, messages, node);
				}
			}
		}
	}

	private void validateMetadataProperties(List<MetadataDefinition> defs, List<String> messages, Node node) {
		List<String> invalidProps = new ArrayList<>();
		List<String> validObjectTypes = Platform.config.hasPath("restrict.metadata.objectTypes") ?
				Arrays.asList(Platform.config.getString("restrict.metadata.objectTypes").split(",")) : Collections.emptyList();
		if (validObjectTypes.contains(node.getObjectType())) {
			Set<String> properties = defs.stream().map(MetadataDefinition::getPropertyName).collect(toSet());
			node.getMetadata().keySet().forEach(e  -> {
				if (!properties.contains(e) && !SystemProperties.isSystemProperty(e)) invalidProps.add(e);
			});
			if (!invalidProps.isEmpty()) messages.add("Invalid Properties : " + invalidProps.toString());
		}
	}

	private Object getPropertyValue(String propName, Node node) {
		Map<String, Object> metadata = node.getMetadata();
		if (null != metadata && !metadata.isEmpty() && StringUtils.isNotBlank(propName)) {
			return metadata.get(propName);
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	private Object setPropertyValue(MetadataDefinition def, String propName, Object value, Node node) {
		if (StringUtils.isNotBlank(propName) && null != value) {
			if (null == node.getMetadata())
				node.setMetadata(new HashMap<String, Object>());

			Map<String, Object> metadata = node.getMetadata();
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

	private void checkDataType(Object value, MetadataDefinition def, List<String> messages,Node node) {
		if (null != value) {
			String propName = def.getPropertyName();
			String dataType = def.getDataType();
			List<Object> range = def.getRange();

			// TODO: the below if condition is to allow term and termlist as
			// datatypes.
			if (StringUtils.isNotBlank(dataType)
					&& MetadataDefinition.PLATFORM_OBJECTS_AS_DATA_TYPE.contains(dataType.toLowerCase())) {
				if (def.getRangeValidation()) {
					String framework = (String) node.getMetadata().get("framework");
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
					value = setPropertyValue(def, propName, DateUtils.format((Date) value), node);
				else {
					value = DateUtils.format(DateUtils.parse(value.toString()));
					value = setPropertyValue(def, propName, value, node);
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

	private void checkMetadata(Map<String, Object> metadata) {
        if (null != metadata && metadata.size() > 0) {
            for (Entry<String, Object> entry : metadata.entrySet()) {
                checkMetadata(entry.getKey(), entry.getValue(), metadata);
            }
        }
    }

	private void checkMetadata(String key, Object value, Map<String, Object> metadata) {
        if (SystemProperties.isSystemProperty(key)) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_INVALID_PROPERTY.name(), key + " is a reserved system property");
        }
        if (null != value) {
            ObjectMapper mapper = new ObjectMapper();
            if (value instanceof Map) {
                try {
                    value = new String(mapper.writeValueAsString(value));
                    if(null != metadata) 
                        metadata.put(key, value);
                } catch (Exception e) {
                    throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_INVALID_JSON.name(), "Invalid JSON for property:"+key, e);
                }
            } else if (value instanceof List) {
                List list = (List) value;
                Object[] array = getArray(key, list);
                if (null == array) {
                    if (list.isEmpty()) {
                        value = null;
                        if(null != metadata) 
                            metadata.put(key, value);
                    } else {
                        try {
                            value = new String(mapper.writeValueAsString(list));
                            if(null != metadata) 
                                metadata.put(key, value);
                        } catch (Exception e) {
                            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_INVALID_JSON.name(), "Invalid value for the property: "+key, e);
                        }
                    }
                } else {
                    value = array;
                    if (null != metadata)
                        metadata.put(key, array);
                }
            } else if (!(value instanceof String) && !(value instanceof String[]) && !(value instanceof Double)
                    && !(value instanceof double[]) && !(value instanceof Float) && !(value instanceof float[]) && !(value instanceof Long)
                    && !(value instanceof long[]) && !(value instanceof Integer) && !(value instanceof int[])
                    && !(value instanceof Boolean) && !(value instanceof boolean[])) {
                throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_INVALID_PROPERTY.name(), "Invalid data type for the property: "
                        + key);
            }
        }
    }

	private Object[] getArray(String key, List list) {
        Object[] array = null;
        try {
            if (null != list && !list.isEmpty()) {
                Object obj = list.get(0);
                if (obj instanceof String) {
                    array = list.toArray(new String[list.size()]);
                } else if (obj instanceof Double) {
                    array = list.toArray(new Double[list.size()]);
                } else if (obj instanceof Float) {
                    array = list.toArray(new Float[list.size()]);
                } else if (obj instanceof Long) {
                    array = list.toArray(new Long[list.size()]);
                } else if (obj instanceof Integer) {
                    array = list.toArray(new Integer[list.size()]);
                } else if (obj instanceof Boolean) {
                    array = list.toArray(new Boolean[list.size()]);
                } else if( obj instanceof Map) {
                    array = null;
                } else {
                    throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_INVALID_PROPERTY.name(), "Invalid data type for the property: " + key);
                }
            }
        } catch (Exception e) {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_INVALID_PROPERTY.name(), "Invalid data type for the property: " + key);
        }
        return array;
    }

	public void removeExternalFields(String graphId, Node node) {
		DefinitionDTO dto = DefinitionCache.getDefinitionNode(graphId, node.getObjectType());
		if (null != dto) {
			List<MetadataDefinition> properties = dto.getProperties();
			if (null != properties && !properties.isEmpty()) {
				for (MetadataDefinition def : properties) {
					if (StringUtils.equalsIgnoreCase("external", def.getDataType())) {
						Map<String, Object> metadata = node.getMetadata();
						if (null != metadata && metadata.containsKey(def.getPropertyName()))
							metadata.put(def.getPropertyName(), null);
					}
				}
			}
		}
	}

	private void getRelationsDelta(List<Relation> addRels, List<Relation> delRels, Node dbNode, Node node) {
		if (null == node.getInRelations()) {
			node.setInRelations(dbNode.getInRelations());
		} else {
			getNewRelationsList(dbNode.getInRelations(), node.getInRelations(), addRels, delRels);
		}
		if (null == node.getOutRelations()) {
			node.setOutRelations(dbNode.getOutRelations());
		} else {
			getNewRelationsList(dbNode.getOutRelations(), node.getOutRelations(), addRels, delRels);
		}
	}

	private void getNewRelationsList(List<Relation> dbRelations, List<Relation> newRelations, List<Relation> addRels,
			List<Relation> delRels) {
		List<String> relList = new ArrayList<String>();
		for (Relation rel : newRelations) {
			addRels.add(rel);
			String relKey = rel.getStartNodeId() + rel.getRelationType() + rel.getEndNodeId();
			if (!relList.contains(relKey))
				relList.add(relKey);
		}
		if (null != dbRelations && !dbRelations.isEmpty()) {
			for (Relation rel : dbRelations) {
				String relKey = rel.getStartNodeId() + rel.getRelationType() + rel.getEndNodeId();
				if (!relList.contains(relKey))
					delRels.add(rel);
			}
		}

	}

	private boolean validateRequired(Object... objects) {
        boolean valid = true;
        for (Object baseValueObject : objects) {
            if (null == baseValueObject) {
                valid = false;
                break;
            }
            if (baseValueObject instanceof String) {
                if (StringUtils.isBlank((String) baseValueObject)) {
                    valid = false;
                    break;
                }
            }
            if (baseValueObject instanceof List<?>) {
                List<?> list = (List<?>) baseValueObject;
                if (null == list || list.isEmpty()) {
                    valid = false;
                    break;
                }
            }
            if (baseValueObject instanceof Map<?, ?>) {
                Map<?, ?> map = (Map<?, ?>) baseValueObject;
                if (null == map || map.isEmpty()) {
                    valid = false;
                    break;
                }
            }
            if (baseValueObject instanceof Property) {
                Property property = (Property) baseValueObject;
                if (StringUtils.isBlank(property.getPropertyName())
                        || (null == property.getPropertyValue() && null == property.getDateValue())) {
                    valid = false;
                    break;
                }
            }
        }
        return valid;
    }

}
