package org.ilimi.wordchain;

import static com.ilimi.graph.dac.util.Neo4jGraphUtil.NODE_LABEL;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.graphdb.traversal.Uniqueness;

import com.ilimi.common.mgr.BaseManager;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.model.Filter;
import com.ilimi.graph.dac.model.MetadataCriterion;
import com.ilimi.graph.dac.model.SearchConditions;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.dac.model.Sort;

public class WordChainsTraversalsTest extends BaseManager{
	
	private final String attrib_lemma = "lemma";
	private final String OBJECT_TYPE_WORD = "Word";
	//private final String attrib_alphabet = "alphabet";
	private String graphId = "wcpnew";
	private int TRAVERSAL_DEPTH = 8;

	@Test
	public void traverse() throws Exception {
		
		System.out.println("Sample 1: ");
		System.out.println("Theme: " + "Animals, Birds, Nature, Insect/Fish, Plants and Trees");
		System.out.println("Category: " + " Thing");
		System.out.println("Paths: ");
		
		GraphDatabaseService graphDb = getGraphDb(graphId);
		
		long startTime = System.currentTimeMillis();
		SearchCriteria sc = new SearchCriteria();
		sc.setNodeType(SystemNodeTypes.DATA_NODE.name());
		sc.setObjectType(OBJECT_TYPE_WORD);
		sc.sort(new Sort(SystemProperties.IL_UNIQUE_ID.name(), Sort.SORT_ASC));

		List<Filter> filters = new ArrayList<Filter>();

		filters.add(new Filter("theme", SearchConditions.OP_IN,
				Arrays.asList(new String[] { "Animals", "Birds", "Nature", "Insect/Fish", "Plants and Trees" })));
		filters.add(new Filter("category", SearchConditions.OP_EQUAL, "Thing"));

		if (null != filters && !filters.isEmpty()) {
			MetadataCriterion mc = MetadataCriterion.create(filters);
			sc.addMetadata(mc);
		}
		
		
		List<Node> nodes = searchNodes(sc, graphDb);
		
		for (Node node : nodes) {
			getTraversalPath(graphDb, node);
		}
		long endTime = System.currentTimeMillis();
		System.out.println("Total time taken for Sample 1: " + (endTime - startTime) / 1000 + "s");
	}
	
	
	//@Test
	public void traverseSample2() throws Exception {
		
		System.out.println("Sample 2: ");
		System.out.println("Categories: " + "Place, Person, Quality");
		System.out.println("Pos: " + "noun, verb, adjective");
		System.out.println("Paths: ");
		
		GraphDatabaseService graphDb = getGraphDb(graphId);
		
		long startTime = System.currentTimeMillis();
		SearchCriteria sc = new SearchCriteria();
		sc.setNodeType(SystemNodeTypes.DATA_NODE.name());
		sc.setObjectType(OBJECT_TYPE_WORD);
		sc.sort(new Sort(SystemProperties.IL_UNIQUE_ID.name(), Sort.SORT_ASC));

		List<Filter> filters = new ArrayList<Filter>();

		filters.add(new Filter("category", SearchConditions.OP_IN,
				Arrays.asList(new String[] { "Place", "Person" , "Quality" })));
		filters.add(new Filter("pos", SearchConditions.OP_IN, Arrays.asList(new String[] { "noun", "verb", "adjective" })));

		if (null != filters && !filters.isEmpty()) {
			MetadataCriterion mc = MetadataCriterion.create(filters);
			sc.addMetadata(mc);
		}
		
		List<Node> nodes = searchNodes(sc, graphDb);
		
		
		for (Node node : nodes) {
			getTraversalPath(graphDb, node);
		}
		long endTime = System.currentTimeMillis();
		System.out.println("Total time taken for Sample 2: " + (endTime - startTime) / 1000 + "s");
	}
	
	//@Test
	public void traverseSample3() throws Exception {
		
		System.out.println("Sample 3: ");
		System.out.println("Themes: " + "Animals, Birds, Nature, Insect/Fish, Plants and Trees, Relations, Vehicles, Common Places");
		System.out.println("Categories: " + "Place, Person, Quality, Thing, Action/Event");
		System.out.println("Pos: " + "noun, verb, adjective, adverb, article, preposition");
		System.out.println("Paths: ");
		
		GraphDatabaseService graphDb = getGraphDb(graphId);
		
		long startTime = System.currentTimeMillis();
		SearchCriteria sc = new SearchCriteria();
		sc.setNodeType(SystemNodeTypes.DATA_NODE.name());
		sc.setObjectType(OBJECT_TYPE_WORD);
		sc.sort(new Sort(SystemProperties.IL_UNIQUE_ID.name(), Sort.SORT_ASC));

		List<Filter> filters = new ArrayList<Filter>();

		filters.add(new Filter("theme", SearchConditions.OP_IN,
				Arrays.asList(new String[] { "Animals", "Birds", "Nature", "Insect/Fish", "Plants and Trees", "Relations", "Vehicles", "Common Places" })));
		filters.add(new Filter("category", SearchConditions.OP_IN,
				Arrays.asList(new String[] { "Place", "Person", "Quality", "Thing", "Action/Event" })));
		filters.add(new Filter("pos", SearchConditions.OP_IN,
				Arrays.asList(new String[] {  "noun", "verb", "adjective", "adverb", "article", "preposition" })));

		if (null != filters && !filters.isEmpty()) {
			MetadataCriterion mc = MetadataCriterion.create(filters);
			sc.addMetadata(mc);
		}
		
		List<Node> nodes = searchNodes(sc, graphDb);
		
		for (Node node : nodes) {
			getTraversalPath(graphDb, node);
		}
		long endTime = System.currentTimeMillis();
		System.out.println("Total time taken for Sample 3: " + (endTime - startTime) / 1000 + "s");
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

	private Traverser getTraverser(final Node person, GraphDatabaseService graphDb) {
		TraversalDescription td = graphDb.traversalDescription().depthFirst()
				.relationships(Rels.endsWith, Direction.OUTGOING).relationships(Rels.startsWith, Direction.OUTGOING)
				.uniqueness(Uniqueness.NODE_GLOBAL).uniqueness(Uniqueness.RELATIONSHIP_GLOBAL)
				// .uniqueness( Uniqueness.RELATIONSHIP_PATH )
				// .uniqueness( Uniqueness.NODE_PATH)
				.evaluator(Evaluators.excludeStartPosition()).evaluator(Evaluators.toDepth(TRAVERSAL_DEPTH));
		return td.traverse(person);
	}

	@SuppressWarnings("unused")
	public void getTraversalPath(GraphDatabaseService graphDb, Node node) {
		Transaction tx = null;
		try {
			tx = graphDb.beginTx();
			int pathLength = 0;
			ArrayList<String> finalPaths = new ArrayList<String>();
			ArrayList<String> allPaths = new ArrayList<String>();
			Path longestPath = null;

			Traverser pathsTraverser = getTraverser(node, graphDb);
			for (Path traversedPath : pathsTraverser) {
				if (traversedPath.length() > pathLength) {
					longestPath = traversedPath;
					pathLength = traversedPath.length();
				}
				render(traversedPath);
				allPaths.add(traversedPath.toString());
			}

			/*
			 * String longestPathString = longestPath.toString();
			 * finalPaths.add(longestPathString); for (String path : allPaths) {
			 * String pathString = path.toString(); if
			 * (!longestPathString.contains(pathString)) {
			 * finalPaths.add(pathString); } }
			 */

			/*
			 * System.out.println("Final Paths:************"); for (String
			 * finalPath : finalPaths) { System.out.println(finalPath); }
			 */
		} catch (Exception e) {
			if (null != tx)
				tx.failure();
		} finally {
			if (null != tx)
				tx.close();
		}
	}

	private String toString(Node node) {
		if (node.hasProperty(attrib_lemma)) {
			return " " + node.getProperty(attrib_lemma) + " ";
		}
		/*
		 * else if(node.hasProperty(attrib_alphabet)){ return "(" +
		 * node.getProperty(attrib_alphabet) + ")"; }
		 */
		return "";
	}

	private String toString(Relationship r) {
		// return "-[" + r.getType().name() + "]->";
		return "->";
	}

	public void render(Path path) {
		StringBuilder sb = new StringBuilder();
		Iterator<PropertyContainer> pcIteraor = path.iterator();
		while (pcIteraor.hasNext()) {
			PropertyContainer pc = pcIteraor.next();
			if (pc instanceof Node)
				sb.append(toString((Node) pc));
			else
				sb.append(toString((Relationship) pc));
		}
		System.out.println(sb.toString());
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

	public List<Node> searchNodes(SearchCriteria sc, GraphDatabaseService graphDb) {
		Transaction tx = null;
		try {
			sc.setCountQuery(false);
			Map<String, Object> params = sc.getParams();
			String query = sc.getQuery();
			tx = graphDb.beginTx();
			Result result = graphDb.execute(query, params);
			List<Node> nodes = new ArrayList<Node>();
			if (null != result) {
				while (result.hasNext()) {
					Map<String, Object> map = result.next();
					if (null != map && !map.isEmpty()) {
						Object o = map.values().iterator().next();
						if (o instanceof org.neo4j.graphdb.Node) {
							org.neo4j.graphdb.Node dbNode = (org.neo4j.graphdb.Node) o;
							nodes.add(dbNode);
						}
					}
				}
				result.close();
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
}
