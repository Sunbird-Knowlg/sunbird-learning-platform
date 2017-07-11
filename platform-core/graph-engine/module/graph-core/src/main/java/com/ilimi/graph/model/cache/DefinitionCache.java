package com.ilimi.graph.model.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.codehaus.jackson.map.ObjectMapper;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.graph.cache.actor.GraphCacheActorPoolMgr;
import com.ilimi.graph.cache.actor.GraphCacheManagers;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.common.mgr.Configuration;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.dac.router.GraphDACActorPoolMgr;
import com.ilimi.graph.dac.router.GraphDACManagers;
import com.ilimi.graph.model.node.DefinitionDTO;
import com.ilimi.graph.model.node.RelationDefinition;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class DefinitionCache extends BaseGraphManager {

	private static Timeout WAIT_TIMEOUT = new Timeout(Duration.create(30, TimeUnit.SECONDS));

	public static DefinitionDTO getDefinitionNode(String graphId, String objectType) {
		DefinitionDTO dto = getDefinitionFromCache(graphId, objectType);
		return dto;
	}

	public static List<String> getOutRelationObjectTypes(String graphId, String objectType) {
		List<String> objectTypes = new ArrayList<String>();
		DefinitionDTO dto = getDefinitionNode(graphId, objectType);
		if (null != dto) {
			List<RelationDefinition> rels = dto.getOutRelations();
			if (null != rels && !rels.isEmpty()) {
				for (RelationDefinition rel : rels) {
					List<String> types = rel.getObjectTypes();
					if (null != types && !types.isEmpty()) {
						for (String type : types) {
							objectTypes.add(rel.getRelationName() + ":" + type);
						}
					}
				}
			}
		}
		return objectTypes;
	}

	public static List<String> getInRelationObjectTypes(String graphId, String objectType) {
		List<String> objectTypes = new ArrayList<String>();
		DefinitionDTO dto = getDefinitionNode(graphId, objectType);
		if (null != dto) {
			List<RelationDefinition> rels = dto.getInRelations();
			if (null != rels && !rels.isEmpty()) {
				for (RelationDefinition rel : rels) {
					List<String> types = rel.getObjectTypes();
					if (null != types && !types.isEmpty()) {
						for (String type : types) {
							objectTypes.add(rel.getRelationName() + ":" + type);
						}
					}
				}
			}
		}
		return objectTypes;
	}
	
	@SuppressWarnings("unchecked")
	private static DefinitionDTO getDefinitionFromCache(String graphId, String objectType) {
		DefinitionDTO dto = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			ActorRef cacheRouter = GraphCacheActorPoolMgr.getCacheRouter();
			Request request = new Request();
			request.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
			request.setManagerName(GraphCacheManagers.GRAPH_CACHE_MANAGER);
			request.setOperation("getDefinitionNode");
			request.put(GraphDACParams.object_type.name(), objectType);
			Future<Object> future = Patterns.ask(cacheRouter, request, Configuration.TIMEOUT);
			Object obj = Await.result(future, WAIT_TIMEOUT.duration());
			if (obj instanceof Response) {
				Response res = (Response) obj;
				Map<String, Object> map = (Map<String, Object>) res.get(GraphDACParams.definition_node.name());
				if (null != map && !map.isEmpty()) {
					dto = mapper.convertValue(map, DefinitionDTO.class);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (null == dto)
			return getDefinitionNodeFromGraph(graphId, objectType);
		return dto;
	}

	@SuppressWarnings("unchecked")
	private static DefinitionDTO getDefinitionNodeFromGraph(String graphId, String objectType) {
		try {
			ActorRef dacRouter = GraphDACActorPoolMgr.getDacRouter();
			Request request = new Request();
			request.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
			request.setManagerName(GraphDACManagers.DAC_SEARCH_MANAGER);
			request.setOperation("searchNodes");
			SearchCriteria sc = new SearchCriteria();
			sc.setNodeType(SystemNodeTypes.DEFINITION_NODE.name());
			sc.setObjectType(objectType);
			sc.setResultSize(1);
			request.put(GraphDACParams.search_criteria.name(), sc);
			Future<Object> future = Patterns.ask(dacRouter, request, Configuration.TIMEOUT);
			Object obj = Await.result(future, WAIT_TIMEOUT.duration());
			if (obj instanceof Response) {
				Response res = (Response) obj;
				List<Node> nodes = (List<Node>) res.get(GraphDACParams.node_list.name());
				if (null != nodes && !nodes.isEmpty()) {
					Node node = nodes.get(0);
					DefinitionDTO dto = new DefinitionDTO();
					dto.fromNode(node);
					return dto;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void invokeMethod(Request request, ActorRef parent) {

	}
}
