package org.ilimi.wordchain;
import static com.ilimi.graph.dac.util.Neo4jGraphUtil.NODE_LABEL;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.tooling.GlobalGraphOperations;

import com.ilimi.graph.common.Identifier;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.router.Rels;
import com.ilimi.graph.dac.util.RelationType;

public class WordChainsRelationsTest {

	final String attrib_lemma = "lemma";
	final String attrib_alphabet = "alphabet";
	Map<String, List<Node>> startsWithMap = new HashMap<String, List<Node>>();
	Map<String, Node> startsWithRelationNodeMap = new HashMap<String, Node>();
	String graphId = "wcpnew";
	final String pbObjType = "PB";

	@Test
	public void createPBandFormRelations() throws Exception {

		GraphDatabaseService graphDb = getGraphDb(graphId);
		List<Node> nodes = getAllNodes(graphDb);
		formRelations(nodes, graphDb);

	}

	private void formRelations(List<Node> nodes, GraphDatabaseService graphDb) throws Exception {
		for (Node wordNode : nodes) {
			Transaction tx = null;
			String lemma = null;
			try {
				tx = graphDb.beginTx();
				if (wordNode.hasProperty(attrib_lemma)) {
					lemma = (String) wordNode.getProperty(attrib_lemma);
				}
				tx.success();
			} catch (Exception e) {
				if (null != tx)
					tx.failure();
			} finally {
				if (null != tx)
					tx.close();
			}

			if (lemma != null) {
				String endsWith = StringUtils.right(lemma, 1);

				Node startsWithRelationNode = startsWithRelationNodeMap.get(endsWith);
				if (startsWithRelationNode == null) {
					startsWithRelationNode = createSWRelationNode(endsWith, graphDb, graphId);
					if (startsWithRelationNode == null) {
						throw new Exception("Unable to create PB node");
					}
					startsWithRelationNodeMap.put(endsWith, startsWithRelationNode);
				}

				addRelation(wordNode, startsWithRelationNode, Rels.endsWith.name(), graphDb);

				List<Node> nodesStartsWith = startsWithMap.get(endsWith);
				if (nodesStartsWith == null) {
					nodesStartsWith = getNodeByPropertyStartsLike(attrib_lemma, endsWith, graphDb);
					startsWithMap.put(endsWith, nodesStartsWith);
				}
				for (Node startsWithNode : nodesStartsWith) {
					// System.out.println(startsWithNode.getProperty(attrib_lemma));
					addRelation(startsWithRelationNode, startsWithNode, Rels.startsWith.name(), graphDb);
				}
			}
		}
	}

	private Node createSWRelationNode(String endsWith, GraphDatabaseService graphDb, String graphId) {
		Transaction tx = null;
		try {
			tx = graphDb.beginTx();
			Node pbNode = graphDb.createNode(NODE_LABEL);
			pbNode.setProperty(SystemProperties.IL_UNIQUE_ID.name(), Identifier.getIdentifier(graphId, pbNode.getId()));
			pbNode.setProperty(SystemProperties.IL_SYS_NODE_TYPE.name(), "DATA_NODE");
			pbNode.setProperty("alphabet", endsWith);
			pbNode.setProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name(), pbObjType);
			tx.success();
			return pbNode;
		} catch (Exception e) {
			if (null != tx)
				tx.failure();
		} finally {
			if (null != tx)
				tx.close();
		}
		return null;
	}

	public static synchronized GraphDatabaseService getGraphDb(String graphId) {
		GraphDatabaseService graphDb = new GraphDatabaseFactory()
				.newEmbeddedDatabaseBuilder("/data/graphDB" + File.separator + graphId)
				.setConfig(GraphDatabaseSettings.allow_store_upgrade, "true")
				.setConfig(GraphDatabaseSettings.cache_type, "weak").newGraphDatabase();
		registerShutdownHook(graphDb);
		return graphDb;
	}

	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("Shutting down graph db...");
				graphDb.shutdown();
			}
		});
	}

	public List<Node> getAllNodes(GraphDatabaseService graphDb) {
		Transaction tx = null;
		try {
			tx = graphDb.beginTx();
			GlobalGraphOperations graphOps = GlobalGraphOperations.at(graphDb);
			Iterable<org.neo4j.graphdb.Node> dbNodes = graphOps.getAllNodes();
			List<Node> nodes = new ArrayList<Node>();
			if (null != dbNodes && null != dbNodes.iterator()) {
				for (org.neo4j.graphdb.Node dbNode : dbNodes) {
					nodes.add(dbNode);
				}
			}
			tx.success();
			return nodes;
		} catch (Exception e) {
			if (null != tx)
				tx.failure();
		} finally {
			if (null != tx)
				tx.close();
		}
		return null;
	}

	public List<Node> getNodeByPropertyStartsLike(String propertyName, String propertyValue,
			GraphDatabaseService graphDb) {
		Transaction tx = null;
		try {
			tx = graphDb.beginTx();
			List<Node> nodesResult = new ArrayList<Node>();
			Result result = graphDb
					.execute("Match (n:NODE) where n." + propertyName + " =~ '" + propertyValue + ".*' return n");
			if (null != result) {
				while (result.hasNext()) {
					Map<String, Object> map = result.next();
					if (null != map && !map.isEmpty()) {
						Node node = (Node) map.values().iterator().next();
						nodesResult.add(node);
					}
				}
				result.close();
			}
			tx.success();
			return nodesResult;
		} catch (Exception e) {
			if (null != tx)
				tx.failure();
		} finally {
			if (null != tx)
				tx.close();
		}
		return null;
	}

	public void addRelation(Node startNode, Node endNode, String relationType, GraphDatabaseService graphDb) {
		Transaction tx = null;
		try {
			tx = graphDb.beginTx();
			RelationType relation = new RelationType(relationType);

			Object endNodeId = endNode.getProperty(SystemProperties.IL_UNIQUE_ID.name());
			String endNodeIdStr = (null == endNodeId) ? null : endNodeId.toString();

			boolean found = false;

			Iterable<Relationship> relations = startNode.getRelationships(Direction.OUTGOING, relation);
			if (null != relations) {
				for (Relationship rel : relations) {
					Object relEndNodeId = rel.getEndNode().getProperty(SystemProperties.IL_UNIQUE_ID.name());
					String strEndNodeId = (null == relEndNodeId) ? null : relEndNodeId.toString();
					if (StringUtils.equals(endNodeIdStr, strEndNodeId)) {
						found = true;
						break;
					}
				}
			}

			if (!found) {
				startNode.createRelationshipTo(endNode, relation);
			}

			tx.success();
			tx.close();
		} catch (Exception e) {
			if (null != tx) {
				tx.failure();
				tx.close();
			}
		}
	}
	
	public List<Node> getNodesByProperty(GraphDatabaseService graphDb, String propertyName, String propertyValue) {
		Transaction tx = null;
		try {
			tx = graphDb.beginTx();
			ResourceIterator<org.neo4j.graphdb.Node> nodes = graphDb.findNodes(NODE_LABEL, propertyName, propertyValue);
			List<Node> nodeList = null;
			if (null != nodes) {
				nodeList = new ArrayList<Node>();
				while (nodes.hasNext()) {
					nodeList.add(nodes.next());
				}
				nodes.close();
			}
			tx.success();
			return nodeList;
		} catch (Exception e) {
			if (null != tx)
				tx.failure();
		} finally {
			if (null != tx)
				tx.close();
		}
		return null;
	}
}
