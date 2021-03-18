package org.sunbird.graph.model.cache;

import java.util.ArrayList;
import java.util.List;

import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.graph.cache.mgr.impl.NodeCacheManager;
import org.sunbird.graph.common.enums.GraphHeaderParams;
import org.sunbird.graph.common.mgr.BaseGraphManager;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.mgr.IGraphDACSearchMgr;
import org.sunbird.graph.dac.mgr.impl.Neo4JBoltSearchMgrImpl;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.SearchCriteria;
import org.sunbird.graph.model.node.DefinitionDTO;
import org.sunbird.graph.model.node.RelationDefinition;

import akka.actor.ActorRef;

public class DefinitionCache extends BaseGraphManager {

	private static IGraphDACSearchMgr searchMgr = new Neo4JBoltSearchMgrImpl();

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
	
	private static DefinitionDTO getDefinitionFromCache(String graphId, String objectType) {
		DefinitionDTO dto = (DefinitionDTO) NodeCacheManager.getDefinitionNode(graphId, objectType);
		if (null == dto) {
			dto = getDefinitionNodeFromGraph(graphId, objectType);
			NodeCacheManager.saveDefinitionNode(graphId, objectType, dto);
		}
		return dto;
	}

	@SuppressWarnings("unchecked")
	private static DefinitionDTO getDefinitionNodeFromGraph(String graphId, String objectType) {
		try {
			Request request = new Request();
			request.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
			SearchCriteria sc = new SearchCriteria();
			sc.setNodeType(SystemNodeTypes.DEFINITION_NODE.name());
			sc.setObjectType(objectType);
			sc.setResultSize(1);
			request.put(GraphDACParams.search_criteria.name(), sc);

			Response res = searchMgr.searchNodes(request);

			List<Node> nodes = (List<Node>) res.get(GraphDACParams.node_list.name());
			if (null != nodes && !nodes.isEmpty()) {
				Node node = nodes.get(0);
				DefinitionDTO dto = new DefinitionDTO();
				dto.fromNode(node);
				return dto;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void invokeMethod(Request request, ActorRef parent) {

	}

	public static void updateDefinitionCache(String graphId, String objectType){
		DefinitionDTO dto = getDefinitionNodeFromGraph(graphId, objectType);
			NodeCacheManager.saveDefinitionNode(graphId, objectType, dto);
	}
}
