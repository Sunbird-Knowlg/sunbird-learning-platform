package org.ekstep.language.mgr.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ekstep.language.mgr.IWordChainsManager;
import org.ekstep.language.util.WordUtil;
import org.ekstep.language.wordchain.evaluators.WordIdEvaluator;
import org.ekstep.language.wordchain.traverser.ITraverserInterface;
import org.ekstep.language.wordchain.traverser.TraverserFactory;
import org.neo4j.graphdb.traversal.Traverser;
import org.springframework.beans.factory.annotation.Autowired;

import com.ilimi.graph.dac.model.Node;

public class WordChainManager implements IWordChainsManager {
	
	@Autowired
	private WordUtil wordUtil;

	@SuppressWarnings("rawtypes")
	@Override
	public void getWordChain(String traversalId, int wordChainsLimit, List<Map> words, Node ruleNode, String graphId) {
		int minDepth=2;
		int maxDepth=5;
		int topWordsLength = 2;
		
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
		wordTraverser.setEvaluator(wordIdEvaluator);
		
		for(Map topWord: topWords){
			String identifier = (String) topWord.get("identifier");
			org.neo4j.graphdb.Node wordNode = wordUtil.getNeo4jNodeByUniqueId(graphId, identifier);
			if(wordNode != null){
				Traverser traverser = wordTraverser.traverse(wordNode, maxDepth, minDepth, graphId);
				
			}
		}
		
	}
	
}
