package org.ekstep.language.mgr.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ekstep.language.common.enums.LanguageObjectTypes;
import org.ekstep.language.mgr.IWordChainsManager;
import org.ekstep.language.util.IWordnetConstants;
import org.ekstep.language.util.WordUtil;
import org.ekstep.language.wordchain.WordChainRelations;
import org.ekstep.language.wordchain.evaluators.WordIdEvaluator;
import org.ekstep.language.wordchain.traverser.ITraverserInterface;
import org.ekstep.language.wordchain.traverser.TraverserFactory;
import org.ilimi.wordchain.Rels;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.graphdb.traversal.Uniqueness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.util.Neo4jGraphFactory;

@Component
public class WordChainManager implements IWordChainsManager, IWordnetConstants {
	
	@Autowired
	private WordUtil wordUtil;

	@SuppressWarnings("rawtypes")
	@Override
	public void getWordChain(String traversalId, int wordChainsLimit, List<Map> words, Node ruleNode, String graphId) {
		int minDepth=2;
		int maxDepth=10;
		int topWordsLength = 50;
		
		List<Map> topWords = new ArrayList<Map>(words.subList(0, topWordsLength));
		
		List<String> ids = new ArrayList<String>();
		Map<String, Double> wordScore = new HashMap<String, Double>();
		Map<String, Map> wordIdMap = new HashMap<String, Map>();
		for(Map word: words){
			String id = (String) word.get("identifier");
			ids.add(id);
			Double score = (Double) word.get("score");
			wordScore.put(id, score);
			wordIdMap.put(id, word);
		}
		
		WordIdEvaluator wordIdEvaluator = new WordIdEvaluator(ids);
		ITraverserInterface wordTraverser =  TraverserFactory.getTraverser(traversalId);
		wordTraverser.createTraversalDescription(maxDepth, minDepth, graphId);
		wordTraverser.setEvaluator(wordIdEvaluator);
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
		
		for(Map topWord: topWords){
			String identifier = (String) topWord.get("identifier");
			org.neo4j.graphdb.Node wordNode = wordUtil.getNeo4jNodeByUniqueId(graphId, identifier);
			if(wordNode != null){
				Traverser traverser = wordTraverser.traverse(wordNode);
				getTraversalPath(traverser, graphDb);
			}
		}
		
	}
	
	/*private Traverser getTraverser(final org.neo4j.graphdb.Node person, GraphDatabaseService graphDb) {
		TraversalDescription td = graphDb.traversalDescription().depthFirst()
				.relationships(WordChainRelations.endsWith, Direction.OUTGOING).relationships(WordChainRelations.startsWith, Direction.OUTGOING)
				.uniqueness(Uniqueness.NODE_GLOBAL).uniqueness(Uniqueness.RELATIONSHIP_GLOBAL)
				// .uniqueness( Uniqueness.RELATIONSHIP_PATH )
				// .uniqueness( Uniqueness.NODE_PATH)
				//.evaluator(Evaluators.excludeStartPosition())
				.evaluator(Evaluators.toDepth(10))
				.evaluator(new Evaluator() {

					@Override
					public Evaluation evaluate(final Path path) {
						//path.
						org.neo4j.graphdb.Node endNode = path.endNode();
						if (endNode.hasProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name())) {
							String objectType = (String) endNode
									.getProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name());
						}

						if (endNode.hasProperty(ATTRIB_LEMMA)) {
							String lemma = (String) endNode.getProperty(ATTRIB_LEMMA);
							if (lemma.equalsIgnoreCase("eagle")) {
								//return Evaluation.EXCLUDE_AND_PRUNE;
							}
						}
						return Evaluation.INCLUDE_AND_CONTINUE;
					}
				});
		return td.traverse(person);
	}
*/
	
	public void getTraversalPath(Traverser pathsTraverser, GraphDatabaseService graphDb) {
		Transaction tx = null;
		try {
			tx = graphDb.beginTx();
			List<Path> finalPaths = new ArrayList<Path>();
			Path previousPath = null;
			int previousPathLength = 0;

			for (Path traversedPath : pathsTraverser) {
				if (traversedPath.length() > previousPathLength) {
					previousPath = traversedPath;
					previousPathLength = traversedPath.length();
				} else if (traversedPath.length() == previousPathLength) {
					if (previousPath != null) {
						finalPaths.add(previousPath);
					}
					previousPath = traversedPath;
					previousPathLength = traversedPath.length();
				} else {
					if (previousPath != null) {
						finalPaths.add(previousPath);
						previousPath = null;
					}
				}
			}

			if (previousPath != null) {
				finalPaths.add(previousPath);
				previousPath = null;
			}

			for (Path finalPath : finalPaths) {
				render(finalPath);
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (null != tx){
				tx.failure();
			}
		} finally {
			if (null != tx){
				tx.close();
			}
		}
	}
	
	public void render(Path path) {
		Iterator<PropertyContainer> pcIteraor = path.iterator();
		List<String> wordChain = new ArrayList<String>();
		while (pcIteraor.hasNext()) {
			PropertyContainer pc = pcIteraor.next();
			if (pc instanceof org.neo4j.graphdb.Node){
				org.neo4j.graphdb.Node node = (org.neo4j.graphdb.Node) pc;
				if (node.hasProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name())) {
					String objectType = (String) node.getProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name());
					if(objectType.equalsIgnoreCase(LanguageObjectTypes.Word.name())){
						if (node.hasProperty(SystemProperties.IL_UNIQUE_ID.name())) {
							String identifier = (String) node.getProperty(SystemProperties.IL_UNIQUE_ID.name());
							wordChain.add(identifier);
						}
					}
				}
			}
		}
		System.out.println(wordChain);
	}
}
