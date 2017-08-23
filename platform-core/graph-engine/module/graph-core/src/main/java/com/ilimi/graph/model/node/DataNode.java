package com.ilimi.graph.model.node;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.graph.service.common.DACErrorCodeConstants;
import org.ekstep.graph.service.common.DACErrorMessageConstants;

import com.ilimi.common.dto.Property;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.cache.actor.GraphCacheActorPoolMgr;
import com.ilimi.graph.cache.actor.GraphCacheManagers;
import com.ilimi.graph.common.DateUtils;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.dac.router.GraphDACActorPoolMgr;
import com.ilimi.graph.dac.router.GraphDACManagers;
import com.ilimi.graph.exception.GraphEngineErrorCodes;
import com.ilimi.graph.exception.GraphRelationErrorCodes;
import com.ilimi.graph.model.IRelation;
import com.ilimi.graph.model.cache.DefinitionCache;
import com.ilimi.graph.model.collection.Tag;
import com.ilimi.graph.model.relation.RelationHandler;

import akka.actor.ActorRef;
import akka.dispatch.Futures;
import akka.dispatch.Mapper;
import akka.dispatch.OnComplete;
import akka.pattern.Patterns;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.Promise;

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
//						metadata.put(SystemProperties.IL_SEQUENCE_INDEX.name(), idx);
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

	public Future<Node> getNodeObject(Request req) {
		ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
		Request request = new Request(req);
		request.setManagerName(GraphDACManagers.DAC_SEARCH_MANAGER);
		request.setOperation("getNodeByUniqueId");
		request.put(GraphDACParams.node_id.name(), getNodeId());
		request.put(GraphDACParams.get_tags.name(), true);
		Future<Object> response = Patterns.ask(dacRouter, request, timeout);
		Future<Node> message = response.map(new Mapper<Object, Node>() {
			@Override
			public Node apply(Object parameter) {
				if (null != parameter && parameter instanceof Response) {
					Response res = (Response) parameter;
					Node node = (Node) res.get(GraphDACParams.node.name());
					return node;
				}
				return null;
			}
		}, manager.getContext().dispatcher());
		return message;
	}

	public Future<List<String>> addTags(final Request req, List<String> tags) {
		final Promise<List<String>> promise = Futures.promise();
		Future<List<String>> tagsFuture = promise.future();
		if (null != tags && !tags.isEmpty()) {
			final ExecutionContext ec = manager.getContext().dispatcher();
			List<Future<String>> tagFutures = new ArrayList<Future<String>>();
			final List<String> tagIds = new ArrayList<String>();
			for (String tagName : tags) {
				Tag tag = new Tag(manager, graphId, tagName, null, null);
				tagIds.add(tag.getNodeId());
				Future<String> tagFuture = tag.upsert(req);
				tagFutures.add(tagFuture);
			}
			Future<Iterable<String>> tagSequence = Futures.sequence(tagFutures, ec);
			tagSequence.onComplete(new OnComplete<Iterable<String>>() {
				@Override
				public void onComplete(Throwable e, Iterable<String> arg0) {
					List<String> messages = new ArrayList<String>();
					if (null != e) {
						messages.add(e.getMessage());
						promise.success(messages);
					} else {
						if (null != arg0) {
							for (String msg : arg0) {
								if (StringUtils.isNotBlank(msg)) {
									messages.add(msg);
								}
							}
						}
						if (messages.isEmpty()) {
							ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
							Request request = new Request(req);
							request.setManagerName(GraphDACManagers.DAC_GRAPH_MANAGER);
							request.setOperation("addIncomingRelations");
							request.put(GraphDACParams.start_node_id.name(), tagIds);
							request.put(GraphDACParams.relation_type.name(),
									RelationTypes.SET_MEMBERSHIP.relationName());
							request.put(GraphDACParams.end_node_id.name(), getNodeId());
							Future<Object> response = Patterns.ask(dacRouter, request, timeout);
							response.onComplete(new OnComplete<Object>() {
								@Override
								public void onComplete(Throwable arg0, Object arg1) throws Throwable {
									List<String> messages = new ArrayList<String>();
									if (null != arg0) {
										messages.add(arg0.getMessage());
									} else {
										if (arg1 instanceof Response) {
											Response res = (Response) arg1;
											if (manager.checkError(res)) {
												messages.add(manager.getErrorMessage(res));
											} else {
												for (String tagId : tagIds)
													updateTagCache(req, tagId, getNodeId());
											}
										} else {
											messages.add("Error adding tags");
										}
									}
									promise.success(messages);
								}
							}, ec);
						} else {
							promise.success(messages);
						}
					}
				}
			}, ec);
		} else {
			promise.success(null);
		}
		return tagsFuture;
	}

	public Future<List<String>> removeTags(final Request req, List<String> tags) {
		final Promise<List<String>> promise = Futures.promise();
		Future<List<String>> tagsFuture = promise.future();
		if (null != tags && !tags.isEmpty()) {
			ExecutionContext ec = manager.getContext().dispatcher();
			final List<String> tagIds = new ArrayList<String>();
			for (String tagName : tags) {
				Tag tag = new Tag(manager, graphId, tagName, null, null);
				tagIds.add(tag.getNodeId());
			}
			ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
			Request request = new Request(req);
			request.setManagerName(GraphDACManagers.DAC_GRAPH_MANAGER);
			request.setOperation("deleteIncomingRelations");
			request.put(GraphDACParams.start_node_id.name(), tagIds);
			request.put(GraphDACParams.relation_type.name(), RelationTypes.SET_MEMBERSHIP.relationName());
			request.put(GraphDACParams.end_node_id.name(), getNodeId());
			Future<Object> response = Patterns.ask(dacRouter, request, timeout);
			response.onComplete(new OnComplete<Object>() {
				@Override
				public void onComplete(Throwable arg0, Object arg1) throws Throwable {
					List<String> messages = new ArrayList<String>();
					if (null != arg0) {
						messages.add(arg0.getMessage());
					} else {
						if (arg1 instanceof Response) {
							Response res = (Response) arg1;
							if (manager.checkError(res)) {
								messages.add(manager.getErrorMessage(res));
							} else {
								for (String tagId : tagIds)
									removeTagMember(req, tagId, getNodeId());
							}
						} else {
							messages.add("Error deleting tags");
						}
					}
					promise.success(messages);
				}
			}, ec);
		} else {
			promise.success(null);
		}
		return tagsFuture;
	}

	private void updateTagCache(Request req, String tagId, String memberId) {
		ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
		Request request = new Request(req);
		request.setManagerName(GraphCacheManagers.GRAPH_CACHE_MANAGER);
		request.setOperation("addTagMember");
		request.put(GraphDACParams.tag_id.name(), tagId);
		request.put(GraphDACParams.member_id.name(), memberId);
		cacheRouter.tell(request, manager.getSelf());
	}

	private void removeTagMember(Request req, String tagId, String memberId) {
		ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
		Request request = new Request(req);
		request.setManagerName(GraphCacheManagers.GRAPH_CACHE_MANAGER);
		request.setOperation("removeTagMember");
		request.put(GraphDACParams.tag_id.name(), tagId);
		request.put(GraphDACParams.member_id.name(), memberId);
		cacheRouter.tell(request, manager.getSelf());
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
			List<Future<String>> futures = new ArrayList<Future<String>>();
			for (IRelation rel : relations) {
				Future<String> msg = rel.deleteRelation(request);
				futures.add(msg);
			}
			Future<Iterable<String>> deleteFuture = Futures.sequence(futures, ec);
			deleteFuture.onComplete(new OnComplete<Iterable<String>>() {
				@Override
				public void onComplete(Throwable arg0, Iterable<String> arg1) throws Throwable {
					List<String> messages = new ArrayList<String>();
					if (null != arg0) {
						messages.add(arg0.getMessage());
					} else {
						if (null != arg1) {
							for (String msg : arg1) {
								if (StringUtils.isNotBlank(msg)) {
									messages.add(msg);
								}
							}
						}
					}
					promise.success(messages);
				}
			}, ec);
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
			List<Future<List<String>>> futures = new ArrayList<Future<List<String>>>();
			for (IRelation rel : relations) {
				Future<Map<String, List<String>>> msgFuture = rel.validateRelation(request);
				Future<List<String>> listFuture = manager.convertFuture(msgFuture);
				futures.add(listFuture);
			}
			Future<Iterable<List<String>>> sequence = Futures.sequence(futures, ec);
			Future<List<String>> relValidationFuture = sequence.map(new Mapper<Iterable<List<String>>, List<String>>() {
				@Override
				public List<String> apply(Iterable<List<String>> parameter) {
					List<String> messages = new ArrayList<String>();
					if (null != parameter) {
						for (List<String> list : parameter) {
							if (null != list && !list.isEmpty()) {
								messages.addAll(list);
							}
						}
					}
					return messages;
				}
			}, ec);
			relValidationFuture.onComplete(new OnComplete<List<String>>() {
				@Override
				public void onComplete(Throwable arg0, List<String> arg1) throws Throwable {
					List<String> messages = new ArrayList<String>();
					if (null != arg0) {
						messages.add(arg0.getMessage());
						promise.success(messages);
					} else {
						if (null == arg1 || arg1.isEmpty()) {
							createRelations(relations, request, ec, promise);
						} else {
							promise.success(arg1);
						}
					}
				}
			}, ec);
		} else {
			promise.success(null);
		}
		return relFuture;
	}

	private void createRelations(List<IRelation> relations, final Request request, ExecutionContext ec,
			final Promise<List<String>> promise) {
		if (null != relations && !relations.isEmpty()) {
			List<Future<String>> futures = new ArrayList<Future<String>>();
			for (IRelation rel : relations) {
				Future<String> msg = rel.createRelation(request);
				futures.add(msg);
			}
			Future<Iterable<String>> createFuture = Futures.sequence(futures, ec);
			createFuture.onComplete(new OnComplete<Iterable<String>>() {
				@Override
				public void onComplete(Throwable arg0, Iterable<String> arg1) throws Throwable {
					List<String> messages = new ArrayList<String>();
					if (null != arg0) {
						messages.add(arg0.getMessage());
					} else {
						if (null != arg1) {
							for (String msg : arg1) {
								if (StringUtils.isNotBlank(msg)) {
									messages.add(msg);
								}
							}
						}
					}
					promise.success(messages);
				}
			}, ec);

		} else {
			promise.success(null);
		}
	}

	@Override
	public void removeProperty(Request req) {
		try {
			ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
			Request request = new Request(req);
			request.setManagerName(GraphDACManagers.DAC_NODE_MANAGER);
			request.setOperation("removePropertyValue");
			request.copyRequestValueObjects(req.getRequest());
			Future<Object> response = Patterns.ask(dacRouter, request, timeout);
			manager.returnResponse(response, getParent());
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
				ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
				Request request = new Request(req);
				request.setManagerName(GraphDACManagers.DAC_NODE_MANAGER);
				request.setOperation("updatePropertyValue");
				request.copyRequestValueObjects(req.getRequest());
				Future<Object> response = Patterns.ask(dacRouter, request, timeout);
				manager.returnResponse(response, getParent());
			} catch (Exception e) {
				manager.ERROR(e, getParent());
			}
		}
	}

	public Future<Response> createNode(final Request req) {
        ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
        Request request = new Request(req);
        request.setManagerName(GraphDACManagers.DAC_NODE_MANAGER);
        request.setOperation("addNode");
        request.put(GraphDACParams.node.name(), toNode());
        Future<Object> response = Patterns.ask(dacRouter, request, timeout);
        Future<Response> message = response.map(new Mapper<Object, Response>() {
            @Override
            public Response apply(Object parameter) {
            	try {
	                if (parameter instanceof Response) {
	                    Response res = (Response) parameter;
	                    if (manager.checkError(res)) {
	                        return res;
	                    } else {
	                        String identifier = (String) res.get(GraphDACParams.node_id.name());
	                        String versionKey = (String) res.get(GraphDACParams.versionKey.name());
	                        if (manager.validateRequired(identifier) && manager.validateRequired(versionKey)) {
	                            setNodeId(identifier);
	                            setVersionKey(versionKey);
	                        } else {
	                        	return manager.getErrorResponse(GraphEngineErrorCodes.ERR_GRAPH_CREATE_NODE_ERROR.name(), 
	                        			"Error creating node in the graph", ResponseCode.SERVER_ERROR);
	                        }
	                    }
	                } else {
	                	return manager.getErrorResponse(GraphEngineErrorCodes.ERR_GRAPH_CREATE_NODE_ERROR.name(), 
                    			"Error creating node in the graph", ResponseCode.SERVER_ERROR);
	                }
            	} catch (Exception ex) {
					throw new ServerException(DACErrorCodeConstants.SERVER_ERROR.name(),
							DACErrorMessageConstants.OBJECT_CASTING_ERROR + " | [Object Cast Error].");
            	}
                return null;
            }
        }, manager.getContext().dispatcher());
        return message;
    }

	public Future<Response> updateNode(Request req) {
		try {
			checkMetadata(metadata);
			ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
			Request request = new Request(req);
			request.setManagerName(GraphDACManagers.DAC_NODE_MANAGER);
			request.setOperation("updateNode");
			request.put(GraphDACParams.node.name(), toNode());
			Future<Object> response = Patterns.ask(dacRouter, request, timeout);
			Future<Response> message = response.map(new Mapper<Object, Response>() {
				@Override
				public Response apply(Object parameter) {
					if (parameter instanceof Response) {
						Response res = (Response) parameter;
						if (manager.checkError(res)) {
							return res;
						} else {
							String identifier = (String) res.get(GraphDACParams.node_id.name());
	                        String versionKey = (String) res.get(GraphDACParams.versionKey.name());
	                        if (manager.validateRequired(identifier) && manager.validateRequired(versionKey)) {
	                            setNodeId(identifier);
	                            setVersionKey(versionKey);
	                        } else {
	                        	return manager.getErrorResponse(GraphEngineErrorCodes.ERR_GRAPH_UPDATE_NODE_ERROR.name(), 
	                        			"Error updating node in the graph", ResponseCode.SERVER_ERROR);
	                        }
						}
					} else {
						return manager.getErrorResponse(GraphEngineErrorCodes.ERR_GRAPH_UPDATE_NODE_ERROR.name(), 
                    			"Error updating node in the graph", ResponseCode.SERVER_ERROR);
					}
					return null;
				}
			}, manager.getContext().dispatcher());
			return message;
		} catch (Exception e) {
			return Futures.successful(manager.getErrorResponse(GraphEngineErrorCodes.ERR_GRAPH_UPDATE_NODE_ERROR.name(), 
        			e.getMessage(), ResponseCode.SERVER_ERROR));
		}
	}

	@Override
	public Future<Map<String, List<String>>> validateNode(Request req) {
		checkMetadata(metadata);
		try {
			final ExecutionContext ec = manager.context().dispatcher();
			final List<String> messages = new ArrayList<String>();
			if (StringUtils.isBlank(objectType)) {
				messages.add("Object type not set for node: " + getNodeId());
				Future<List<String>> message = Futures.successful(messages);
				return getMessageMap(message, ec);
			} else {
				DefinitionDTO dto = DefinitionCache.getDefinitionNode(graphId, objectType);
				if (null != dto) {
					validateMetadata(dto.getProperties(), messages);
					validateRelations(dto.getInRelations(), "incoming", messages);
					validateRelations(dto.getOutRelations(), "outgoing", messages);
				} else {
					messages.add("Definition node not found for Object Type: " + objectType);
				}
				Map<String, List<String>> map = new HashMap<String, List<String>>();
				map.put(getNodeId(), messages);
				return Futures.successful(map);
			}
		} catch (Exception e) {
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

	private void validateMetadata(List<MetadataDefinition> defs, List<String> messages) {
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
