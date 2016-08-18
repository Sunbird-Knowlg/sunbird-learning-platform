package org.ekstep.language.mgr.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.common.enums.LanguageObjectTypes;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.mgr.IWordChainsManager;
import org.ekstep.language.util.BaseLanguageManager;
import org.ekstep.language.util.IWordChainConstants;
import org.ekstep.language.util.IWordnetConstants;
import org.ekstep.language.wordchain.evaluators.WordIdEvaluator;
import org.ekstep.language.wordchain.traverser.ITraverser;
import org.ekstep.language.wordchain.traverser.TraverserFactory;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Path;
import com.ilimi.graph.dac.model.SubGraph;
import com.ilimi.graph.engine.router.GraphEngineManagers;

@Component
public class WordChainManager extends BaseLanguageManager
		implements IWordChainsManager, IWordnetConstants, IWordChainConstants {

	private static Logger LOGGER = LogManager.getLogger(WordChainManager.class);

	public Comparator<Map<String, Object>> wordChainsComparator = new Comparator<Map<String, Object>>() {
		@Override
		public int compare(Map<String, Object> wordChain1, Map<String, Object> wordChain2) {
			return ((Double) wordChain2.get("score")).compareTo((Double) wordChain1.get("score"));
		}
	};

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Response getWordChain(int wordChainsLimit, List<Map> words, Node ruleNode, String graphId) throws Exception {
		Map<String, Object> ruleNodeMetadata = ruleNode.getMetadata();

		int maxdefinedDepth = getIntValue(ruleNodeMetadata.get(ATTRIB_MAX_CHAIN_LENGTH));
		int maxDepth = maxdefinedDepth + (maxdefinedDepth - 1);
		
		
		int minDepth = getIntValue(ruleNodeMetadata.get(ATTRIB_MIN_CHAIN_LENGTH));
		minDepth = minDepth + (minDepth - 1);

		int startWordsLength = getIntValue(ruleNodeMetadata.get(ATTRIB_START_WORDS_SIZE));
		String relation = (String) ruleNodeMetadata.get(ATTRIB_RULE_TYPE);
		String traverserClass = (String) ruleNodeMetadata.get(ATTRIB_TRAVERSER_CLASS);

		Response wordChainResponse = OK();

		List<Map> topWords = new ArrayList<Map>();
		if (words.size() > startWordsLength) {
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


		List<Map<String, Object>> wordChains = new ArrayList<Map<String, Object>>();
		List<Map> wordChainWords = new ArrayList<Map>();
		
		WordIdEvaluator wordIdEvaluator = new WordIdEvaluator(ids);
		ITraverser wordTraverser = TraverserFactory.getTraverser(traverserClass);
		
		
		//Individual traversal
		
		for (Map topWord : topWords) {
			
			String identifier = (String) topWord.get(LanguageParams.identifier.name());
			com.ilimi.graph.dac.model.Traverser searchTraverser = new com.ilimi.graph.dac.model.Traverser(graphId,
					identifier);
			wordTraverser.setTraversalDescription(searchTraverser.getBaseTraversalDescription());
			wordTraverser.enhanceTraversalDescription(maxDepth, minDepth, graphId);
			wordTraverser.setEvaluator(wordIdEvaluator);
			
			searchTraverser.setTraversalDescription(wordTraverser.getTraversalDescription());
			Request request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "traverse");
			request.put(GraphDACParams.traversal_description.name(), searchTraverser);
			
			Response traverseResponse = getResponse(request, LOGGER);

			if (checkError(traverseResponse)) {
				return traverseResponse;
			}

			SubGraph subGraph = (SubGraph) traverseResponse.get(GraphDACParams.sub_graph.name());

			List<Path> paths = subGraph.getPaths();
			for (Path finalPath : paths) {
				Map wordChain = processPath(finalPath, wordScore, relation);
				if (wordChain != null) {
					wordChains.add(wordChain);
				}
			}
		}
		
		//All nodes traversal
		
		/*List<String> topWordIds = new ArrayList<String>();
		if (ids.size() > startWordsLength) {
			topWordIds = ids.subList(0, startWordsLength);
		} else {
			topWordIds = ids;
		}

		com.ilimi.graph.dac.model.Traverser searchTraverser = new com.ilimi.graph.dac.model.Traverser(graphId,
				topWordIds);
		searchTraverser.setTraversalDescription(wordTraverser.getTraversalDescription());
		Request request = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "traverse");
		request.put(GraphDACParams.traversal_description.name(), searchTraverser);
		
		Response traverseResponse = getResponse(request, LOGGER);

		if (checkError(traverseResponse)) {
			return traverseResponse;
		}

		SubGraph subGraph = (SubGraph) traverseResponse.get(GraphDACParams.sub_graph.name());

		List<Path> paths = subGraph.getPaths();
		for (Path finalPath : paths) {
			Map wordChain = processPath(finalPath, wordScore, wordIdMap, relation);
			if (wordChain != null) {
				wordChains.add(wordChain);
			}
		}*/

		Collections.sort(wordChains, wordChainsComparator);

		List<Map<String, Object>> finalWordChains = new ArrayList<Map<String, Object>>();
		if (wordChains.size() > wordChainsLimit) {
			finalWordChains = wordChains.subList(0, wordChainsLimit);
		} else {
			finalWordChains = wordChains;
		}
		wordChainResponse.put("relations", finalWordChains);

		java.util.Set<String> finalWordIds = new HashSet<String>();
		for (Map<String, Object> wordChain : finalWordChains) {
			List<String> word_ids = (List<String>) wordChain.get(ATTRIB_WORD_CHAIN_LIST);
			finalWordIds.addAll(word_ids);
		}

		for (String wordId : finalWordIds) {
			wordChainWords.add(wordIdMap.get(wordId));
		}

		wordChainResponse.put("words", wordChainWords);
		return wordChainResponse;
	}

	private int getIntValue(Object object) {
		int value;
		if(object instanceof Double){
			value = ((Double)object).intValue();
		}
		else{
			value = (int) object;
		}
		return value;
	}

	public Map<String, Object> processPath(Path path, Map<String, Double> wordScore,
			String relation) throws Exception {
		List<String> wordChain = new ArrayList<String>();
		Double totalScore = 0.0;
		Double averageScore = 0.0;
		int chainLength = 0;
		Map<String, Object> wordChainRecord = new HashMap<String, Object>();
		String title = "";
		for (Node node : path.getNodes()) {
			Map<String, Object> nodeMetadata = node.getMetadata();
			String objectType = node.getObjectType();
			if (objectType.equalsIgnoreCase(LanguageObjectTypes.Word.name())) {
				if (node.getIdentifier() != null) {
					String identifier = node.getIdentifier();
					if (wordChain.contains(identifier)) {
						break;
					}
					wordChain.add(identifier);

					Double score = wordScore.get(identifier);
					if (score == null) {
						score = 0.0;
					}
					totalScore = totalScore + score;
					chainLength++;
				}
			} else if (objectType.equalsIgnoreCase(LanguageObjectTypes.Phonetic_Boundary.name())) {
				if (nodeMetadata.containsKey(ATTRIB_PHONETIC_BOUNDARY_TYPE)) {
					String type = (String) nodeMetadata.get(ATTRIB_PHONETIC_BOUNDARY_TYPE);
					if (type.equalsIgnoreCase(PHONETIC_BOUNDARY_TYPE_RHYMING_SOUND)) {
						if (nodeMetadata.containsKey(ATTRIB_PHONETIC_BOUNDARY_LEMMA)) {
							String lemma = (String) nodeMetadata.get(ATTRIB_PHONETIC_BOUNDARY_LEMMA);
							title = lemma;
						}
					}
				}
			}
		}
		if (wordChain.size() == 1) {
			return null;
		}
		
		averageScore = totalScore/chainLength;
		BigDecimal tmp = new BigDecimal(averageScore);
		tmp = tmp.setScale(2, RoundingMode.HALF_UP);
		averageScore = tmp.doubleValue();
		
		wordChainRecord.put(ATTRIB_WORD_CHAIN_TITLE, title);
		wordChainRecord.put(ATTRIB_WORD_CHAIN_LIST, wordChain);
		wordChainRecord.put(ATTRIB_WORD_CHAIN_SCORE, averageScore);
		wordChainRecord.put(ATTRIB_WORD_CHAIN_RELATION, relation);
		return wordChainRecord;
	}
}
