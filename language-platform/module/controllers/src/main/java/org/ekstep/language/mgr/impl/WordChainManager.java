package org.ekstep.language.mgr.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.common.LanguageMap;
import org.ekstep.language.common.enums.LanguageErrorCodes;
import org.ekstep.language.common.enums.LanguageObjectTypes;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.mgr.IWordChainsManager;
import org.ekstep.language.util.BaseLanguageManager;
import org.ekstep.language.util.IWordChainConstants;
import org.ekstep.language.util.IWordnetConstants;
import org.ekstep.language.util.WordUtil;
import org.ekstep.language.wordchain.evaluators.WordIdEvaluator;
import org.ekstep.language.wordchain.traverser.ITraverserInterface;
import org.ekstep.language.wordchain.traverser.TraverserFactory;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.Traverser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.dac.model.Sort;
import com.ilimi.graph.dac.util.Neo4jGraphFactory;
import com.ilimi.graph.engine.router.GraphEngineManagers;

@Component
public class WordChainManager extends BaseLanguageManager implements IWordChainsManager, IWordnetConstants, IWordChainConstants {

	@Autowired
	private WordUtil wordUtil;
	
	private static Logger LOGGER = LogManager.getLogger(WordChainManager.class);
	
	public Comparator<Map<String, Object>> wordChainsComparator = new Comparator<Map<String, Object>>() {
		@Override
	    public int compare(Map<String, Object> wordChain1, Map<String, Object> wordChain2) {
	        return ((Double)wordChain2.get("score")).compareTo((Double)wordChain1.get("score"));
	    }
	};
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Response getWordChain(String traversalId, int wordChainsLimit, List<Map> words, Node ruleNode,
			String graphId) throws Exception {
		Map<String, Object> ruleNodeMetadata = ruleNode.getMetadata();
		int maxDepth = (int) ruleNodeMetadata.get(ATTRIB_MAX_CHAIN_LENGTH);
		maxDepth = maxDepth + (maxDepth-1);
		
		int minDepth = (int) ruleNodeMetadata.get(ATTRIB_MIN_CHAIN_LENGTH);
		minDepth = minDepth + (minDepth-1);
		
		int startWordsLength = (int) ruleNodeMetadata.get(ATTRIB_START_WORDS_SIZE);
		
		String relation = (String) ruleNodeMetadata.get(ATTRIB_RULE_TYPE);
		
		Response wordChainResponse = OK();

		List<Map> topWords = new ArrayList<Map>();
		if(words.size() > startWordsLength){
			topWords = words.subList(0, startWordsLength);
		} else {
			topWords = words;
		}

		List<String> ids = new ArrayList<String>();
		Map<String, Double> wordScore = new HashMap<String, Double>();
		Map<String, Map> wordIdMap = new HashMap<String, Map>();
		for (Map word : words) {
			String id = (String) word.get(LanguageParams.identifier.name());
			ids.add(id);
			Double score = (Double) word.get("score");
			wordScore.put(id, score);
			wordIdMap.put(id, word);
		}

		WordIdEvaluator wordIdEvaluator = new WordIdEvaluator(ids);
		ITraverserInterface wordTraverser = TraverserFactory.getTraverser(traversalId);
		wordTraverser.createTraversalDescription(maxDepth, minDepth, graphId);
		wordTraverser.setEvaluator(wordIdEvaluator);
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);

		List<Map<String, Object>> wordChains = new ArrayList<Map<String, Object>>();
		List<Map> wordChainWords = new ArrayList<Map>();
		for (Map topWord : topWords) {
			String identifier = (String) topWord.get(LanguageParams.identifier.name());
			org.neo4j.graphdb.Node wordNode = wordUtil.getNeo4jNodeByUniqueId(graphId, identifier);
			if (wordNode != null) {
				Traverser traverser = wordTraverser.traverse(wordNode);
				List<Path> finalPaths = getFinalPaths(traverser, graphDb);
				for (Path finalPath : finalPaths) {
					Map wordChain = processPath(finalPath, wordScore, wordIdMap, graphDb, relation);
					if(wordChain != null){
						wordChains.add(wordChain);
					}
				}
			}
		}
		
		Collections.sort(wordChains, wordChainsComparator);
		
		//TODO: check better ways
		List<Map<String, Object>> finalWordChains = new ArrayList<Map<String, Object>>();
		if(wordChains.size() > wordChainsLimit){
			finalWordChains = wordChains.subList(0, wordChainsLimit);
		}
		else{
			finalWordChains = wordChains;
		}
		wordChainResponse.put("relations", finalWordChains);
		
		java.util.Set<String> finalWordIds =  new HashSet<String>();
		for(Map<String, Object> wordChain: finalWordChains){
			List<String> word_ids = (List<String>) wordChain.get(ATTRIB_WORD_CHAIN_LIST);
			finalWordIds.addAll(word_ids);
		}
		
		for(String wordId: finalWordIds){
			wordChainWords.add(wordIdMap.get(wordId));
		}
		
		wordChainResponse.put("words", wordChainWords);
		return wordChainResponse;
	}

	public List<Path> getFinalPaths(Traverser pathsTraverser, GraphDatabaseService graphDb) {
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

			return finalPaths;
		} catch (Exception e) {
			e.printStackTrace();
			if (null != tx) {
				tx.failure();
			}
		} finally {
			if (null != tx) {
				tx.close();
			}
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	public Map<String, Object> processPath(Path path, Map<String, Double> wordScore, Map<String, Map> wordIdMap,
			GraphDatabaseService graphDb, String relation) throws Exception {
		Iterator<PropertyContainer> pcIteraor = path.iterator();
		List<String> wordChain = new ArrayList<String>();
		Double totalScore = 0.0;
		Map<String, Object> wordChainRecord = new HashMap<String, Object>();
		String title = "";
		while (pcIteraor.hasNext()) {
			PropertyContainer pc = pcIteraor.next();
			if (pc instanceof org.neo4j.graphdb.Node) {
				Transaction tx = null;
				try {
					tx = graphDb.beginTx();
					org.neo4j.graphdb.Node node = (org.neo4j.graphdb.Node) pc;
					if (node.hasProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name())) {
						String objectType = (String) node.getProperty(SystemProperties.IL_FUNC_OBJECT_TYPE.name());
						if (objectType.equalsIgnoreCase(LanguageObjectTypes.Word.name())) {
							if (node.hasProperty(SystemProperties.IL_UNIQUE_ID.name())) {
								String identifier = (String) node.getProperty(SystemProperties.IL_UNIQUE_ID.name());
								if(wordChain.contains(identifier)){
									return null;
								}
								wordChain.add(identifier);
								
								Map word = wordIdMap.get(identifier);
								if(word == null){
									throw new Exception("Unable to find word in memoery for " + identifier);
								}
								Double score = wordScore.get(identifier);
								if (score == null) {
									throw new Exception("Unable to find score for " + identifier);
								}
								totalScore = totalScore + score;
							}
						}
						else if(objectType.equalsIgnoreCase(LanguageObjectTypes.Phonetic_Boundary.name())){
							if (node.hasProperty(ATTRIB_PHONETIC_BOUNDARY_TYPE)) {
								String type = (String) node.getProperty(ATTRIB_PHONETIC_BOUNDARY_TYPE);
								if(type.equalsIgnoreCase(PHONETIC_BOUNDARY_TYPE_RHYMING_SOUND)){
									if (node.hasProperty(ATTRIB_PHONETIC_BOUNDARY_LEMMA)) {
										String lemma = (String) node.getProperty(ATTRIB_PHONETIC_BOUNDARY_LEMMA);
										title = lemma;
									}
								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					if (null != tx) {
						tx.failure();
					}
				} finally {
					if (null != tx) {
						tx.close();
					}
				}
			}
		}
		wordChainRecord.put(ATTRIB_WORD_CHAIN_TITLE, title);
		wordChainRecord.put(ATTRIB_WORD_CHAIN_LIST, wordChain);
		wordChainRecord.put(ATTRIB_WORD_CHAIN_SCORE, totalScore);
		wordChainRecord.put(ATTRIB_WORD_CHAIN_RELATION, relation);
		return wordChainRecord;
	}
}
