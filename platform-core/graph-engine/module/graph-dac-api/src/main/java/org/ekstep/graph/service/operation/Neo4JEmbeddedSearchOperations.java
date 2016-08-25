package org.ekstep.graph.service.operation;

import static com.ilimi.graph.dac.util.Neo4jGraphUtil.NODE_LABEL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.Evaluators;

import com.ilimi.common.dto.Property;
import com.ilimi.common.dto.Request;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.model.Graph;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.dac.model.RelationTraversal;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.dac.model.SubGraph;
import com.ilimi.graph.dac.model.Traverser;
import com.ilimi.graph.dac.util.Neo4jGraphFactory;
import com.ilimi.graph.dac.util.Neo4jGraphUtil;
import com.ilimi.graph.dac.util.RelationType;

public class Neo4JEmbeddedSearchOperations extends BaseOperations {

	private static Logger LOGGER = LogManager.getLogger(Neo4JEmbeddedSearchOperations.class.getName());

	public Node getNodeById(String graphId, Long nodeId, Boolean getTags, Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			org.neo4j.graphdb.Node neo4jNode = graphDb.getNodeById(nodeId);
			tx.success();
			Node node = new Node(graphId, neo4jNode);
			if (null != getTags && getTags.booleanValue())
				setTags(neo4jNode, node);
			tx.success();
			return node;
		}
	}

	public Node getNodeByUniqueId(String graphId, String nodeId, Boolean getTags, Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			org.neo4j.graphdb.Node neo4jNode = Neo4jGraphUtil.getNodeByUniqueId(graphDb, nodeId);
			Node node = new Node(graphId, neo4jNode);
			if (null != getTags && getTags.booleanValue())
				setTags(neo4jNode, node);
			tx.success();
			return node;
		}
	}

	public List<Node> getNodesByProperty(String graphId, Property property, Boolean getTags, Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			ResourceIterator<org.neo4j.graphdb.Node> nodes = graphDb.findNodes(NODE_LABEL, property.getPropertyName(),
					property.getPropertyValue());
			List<Node> nodeList = new ArrayList<Node>();
			if (null != nodes) {
				while (nodes.hasNext()) {
					org.neo4j.graphdb.Node neo4jNode = nodes.next();
					Node node = new Node(graphId, neo4jNode);
					if (null != getTags && getTags.booleanValue())
						setTags(neo4jNode, node);
					nodeList.add(node);
					nodes.close();
				}
				nodes.close();
			}
			tx.success();
			return nodeList;
		}
	}

	public List<Node> getNodeByUniqueIds(String graphId, SearchCriteria searchCriteria, Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			Map<String, Object> params = searchCriteria.getParams();
			String query = searchCriteria.getQuery();
			Result result = graphDb.execute(query, params);
			List<Node> nodes = new ArrayList<Node>();
			if (null != result) {
				while (result.hasNext()) {
					Map<String, Object> map = result.next();
					if (null != map && !map.isEmpty()) {
						Object o = map.values().iterator().next();
						if (o instanceof org.neo4j.graphdb.Node) {
							org.neo4j.graphdb.Node dbNode = (org.neo4j.graphdb.Node) o;
							Node node = new Node(graphId, dbNode);
							setTags(dbNode, node);
							nodes.add(node);
						}
					}
				}
				result.close();
			}
			tx.success();
			return nodes;
		}
	}

	public Property getNodeProperty(String graphId, String nodeId, String key, Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("nodeId", nodeId);
			Result result = graphDb.execute(
					"MATCH (n:NODE) WHERE n." + SystemProperties.IL_UNIQUE_ID.name() + "  in {nodeId} RETURN n." + key,
					params);
			Property property = new Property();
			if (null != result) {
				while (result.hasNext()) {
					Map<String, Object> map = result.next();
					if (null != map && !map.isEmpty()) {
						Object obj = map.values().iterator().next();
						property = new Property(key, obj);
					}
				}
				result.close();
			}
			tx.success();
			return property;
		}
	}

	public List<Node> getAllNodes(String graphId, Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			Iterable<org.neo4j.graphdb.Node> dbNodes = graphDb.getAllNodes();
			List<Node> nodes = new ArrayList<Node>();
			if (null != dbNodes && null != dbNodes.iterator()) {
				for (org.neo4j.graphdb.Node dbNode : dbNodes) {
					nodes.add(new Node(graphId, dbNode));
				}
			}
			tx.success();
			return nodes;
		}
	}

	public List<Relation> getAllRelations(String graphId, Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			Iterable<Relationship> dbRelations = graphDb.getAllRelationships();
			List<Relation> relations = new ArrayList<Relation>();
			if (null != dbRelations && null != dbRelations.iterator()) {
				for (Relationship dbRel : dbRelations) {
					relations.add(new Relation(graphId, dbRel));
				}
			}
			tx.success();
			return relations;
		}
	}

	public Property getRelationProperty(String graphId, String startNodeId, String relationType, String endNodeId,
			String key, Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			Object value = null;
			Relationship rel = Neo4jGraphUtil.getRelationship(graphDb, startNodeId, relationType, endNodeId);
			if (null != rel)
				value = rel.getProperty(key);
			tx.success();
			Property property = new Property(key, value);
			tx.success();
			return property;
		}
	}

	public Relation getRelation(String graphId, String startNodeId, String relationType, String endNodeId,
			Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			Relationship rel = Neo4jGraphUtil.getRelationship(graphDb, startNodeId, relationType, endNodeId);
			Relation relation = new Relation();
			if (null != rel)
				relation = new Relation(graphId, rel);
			tx.success();
			return relation;
		}
	}

	public Map<String, Object> checkCyclicLoop(String graphId, String startNodeId, String relationType,
			String endNodeId, Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			org.neo4j.graphdb.Node startNode = Neo4jGraphUtil.getNodeByUniqueId(graphDb, startNodeId);
			org.neo4j.graphdb.Node endNode = Neo4jGraphUtil.getNodeByUniqueId(graphDb, endNodeId);
			ResourceIterable<org.neo4j.graphdb.Node> pathNodes = graphDb.traversalDescription().breadthFirst()
					.relationships(new RelationType(relationType), Direction.OUTGOING)
					.evaluator(Evaluators.pruneWhereEndNodeIs(endNode)).traverse(startNode).nodes();

			Map<String, Object> voMap = new HashMap<String, Object>();
			if (null != pathNodes && null != pathNodes.iterator()) {
				for (org.neo4j.graphdb.Node node : pathNodes) {
					String uniqueId = (String) node.getProperty(SystemProperties.IL_UNIQUE_ID.name(), null);
					if (StringUtils.equals(endNodeId, uniqueId)) {
						voMap.put(GraphDACParams.loop.name(), new Boolean(true));
						voMap.put(GraphDACParams.message.name(),
								startNodeId + " and " + endNodeId + " are connected by relation: " + relationType);
						break;
					}
				}
				pathNodes.iterator().close();
			}
			if (voMap.get(GraphDACParams.loop.name()) == null) {
				voMap.put(GraphDACParams.loop.name(), false);
			}
			tx.success();
			return voMap;
		}
	}

	public List<Map<String, Object>> executeQuery(String graphId, String query, Map<String, Object> paramMap,
			Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			Map<String, Object> params = new HashMap<String, Object>();
			if (validateRequired(paramMap))
				params = paramMap;
			Result result = graphDb.execute(query, params);
			List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
			if (null != result) {
				while (result.hasNext()) {
					Map<String, Object> map = result.next();
					if (null != map && !map.isEmpty()) {
						resultList.add(map);
					}
				}
				result.close();
			}
			tx.success();
			return resultList;
		}
	}

	public List<Node> searchNodes(String graphId, SearchCriteria searchCriteria, Boolean getTags, Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			searchCriteria.setCountQuery(false);
			List<String> fields = searchCriteria.getFields();
			boolean returnNode = true;
			if (null != fields && !fields.isEmpty())
				returnNode = false;
			Map<String, Object> params = searchCriteria.getParams();
			String query = searchCriteria.getQuery();
			Result result = graphDb.execute(query, params);
			List<Node> nodes = new ArrayList<Node>();
			if (null != result) {
				while (result.hasNext()) {
					Map<String, Object> map = result.next();
					if (null != map && !map.isEmpty()) {
						if (returnNode) {
							Object o = map.values().iterator().next();
							if (o instanceof org.neo4j.graphdb.Node) {
								org.neo4j.graphdb.Node dbNode = (org.neo4j.graphdb.Node) o;
								Node node = new Node(graphId, dbNode);
								if (null != getTags && getTags.booleanValue())
									setTags(dbNode, node);
								nodes.add(node);
							}
						} else {
							Node node = new Node(graphId, map);
							nodes.add(node);
						}
					}
				}
				result.close();
			}
			tx.success();
			return nodes;
		}
	}

	public Long getNodesCount(String graphId, SearchCriteria searchCriteria, Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			searchCriteria.setCountQuery(true);
			Map<String, Object> params = searchCriteria.getParams();
			String query = searchCriteria.getQuery();
			Result result = graphDb.execute(query, params);
			Long count = (long) 0;
			if (null != result && result.hasNext()) {
				Map<String, Object> map = result.next();
				if (null != map && !map.isEmpty()) {
					for (Entry<String, Object> entry : map.entrySet()) {
						Object obj = entry.getValue();
						try {
							count = Long.valueOf(obj.toString());
						} catch (Exception e) {
						}
					}
				}
				result.close();
			}
			tx.success();
			return count;
		}
	}

	public SubGraph traverse(String graphId, Traverser traverser, Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			SubGraph subGraph = traverser.traverse();
			tx.success();
			return subGraph;
		}
	}

	public Graph traverseSubGraph(String graphId, Traverser traverser, Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			Graph subGraph = traverser.getSubGraph();
			tx.success();
			return subGraph;
		}
	}

	public Graph getSubGraph(String graphId, String startNodeId, String relationType, Integer depth, Request request) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId, request);
		try (Transaction tx = graphDb.beginTx()) {
			Traverser traverser = new Traverser(graphId, startNodeId);
			traverser.traverseRelation(new RelationTraversal(relationType, RelationTraversal.DIRECTION_OUT));
			if (null != depth && depth.intValue() > 0) {
				traverser.toDepth(depth);
			}
			Graph subGraph = traverser.getSubGraph();
			tx.success();
			return subGraph;
		}
	}

	private void setTags(org.neo4j.graphdb.Node neo4jNode, Node node) {
		Iterable<Relationship> inRels = neo4jNode.getRelationships(Direction.INCOMING);
		if (null != inRels) {
			List<String> tags = new ArrayList<String>();
			for (Relationship rel : inRels) {
				if (StringUtils.equals(RelationTypes.SET_MEMBERSHIP.relationName(), rel.getType().name())) {
					org.neo4j.graphdb.Node startNode = rel.getStartNode();
					String nodeType = (String) startNode.getProperty(SystemProperties.IL_SYS_NODE_TYPE.name(), null);
					if (StringUtils.equalsIgnoreCase(SystemNodeTypes.TAG.name(), nodeType)) {
						String tag = (String) startNode.getProperty(SystemProperties.IL_TAG_NAME.name(), null);
						if (StringUtils.isNotBlank(tag))
							tags.add(tag);
					}
				}
			}
			node.setTags(tags);
		}
	}

}
