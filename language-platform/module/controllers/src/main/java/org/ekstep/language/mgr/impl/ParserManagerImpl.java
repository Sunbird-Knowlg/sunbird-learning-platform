package org.ekstep.language.mgr.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.enums.RelationTypes;
import org.ekstep.graph.dac.enums.SystemNodeTypes;
import org.ekstep.graph.dac.model.Filter;
import org.ekstep.graph.dac.model.MetadataCriterion;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.dac.model.Relation;
import org.ekstep.graph.dac.model.SearchConditions;
import org.ekstep.graph.dac.model.SearchCriteria;
import org.ekstep.graph.engine.router.GraphEngineManagers;
import org.ekstep.language.common.enums.LanguageErrorCodes;
import org.ekstep.language.measures.entity.ComplexityMeasures;
import org.ekstep.language.mgr.IParserManager;
import org.ekstep.language.util.BaseLanguageManager;
import org.ekstep.language.util.IWordnetConstants;
import org.ekstep.language.util.LanguageUtil;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.logger.PlatformLogger;

/**
 * The Class ParserManagerImpl, provides functinality of parsing the content and
 * return the equivalent meaning of the words found in graph
 *
 * @author rayulu
 */
@Component
public class ParserManagerImpl extends BaseLanguageManager implements IParserManager, IWordnetConstants {

	/** The logger. */
	

	/** The object type. */
	private String objectType = "Word";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.language.mgr.IParserManager#parseContent(java.lang.String,
	 * java.lang.String, java.lang.Boolean, java.lang.Boolean,
	 * java.lang.Boolean, java.lang.Boolean, java.lang.Integer)
	 */
	public Response parseContent(String languageId, String content, Boolean wordSuggestions, Boolean relatedWords,
			Boolean translations, Boolean equivalentWords, Integer limit) {
		List<String> tokens = LanguageUtil.getTokens(content);
		if (null == limit || limit.intValue() <= 0)
			limit = 10;
		List<Node> nodes = searchWords(languageId, tokens);
		Map<String, Node> nodeMap = new HashMap<String, Node>();
		Map<String, String> synsetIdMap = new HashMap<String, String>();
		Map<String, Map<String, Object>> returnMap = new HashMap<String, Map<String, Object>>();
		if (null != nodes && !nodes.isEmpty()) {
			Set<String> synsetIds = new HashSet<String>();
			PlatformLogger.log("Number of words: " + nodes.size());
			for (Node node : nodes) {
				if (null != node.getMetadata() && !node.getMetadata().isEmpty()) {
					String primarySynsetId = getPrimarySynsetId(node);
					Map<String, Object> wordMap = new HashMap<String, Object>();
					String lemma = (String) node.getMetadata().get("lemma");
					try {
						Double orthoComplexity = (Double) node.getMetadata().get("orthographic_complexity");
						Double phonicComplexity = (Double) node.getMetadata().get("phonologic_complexity");
						if (null != orthoComplexity || null != phonicComplexity) {
							ComplexityMeasures measures = new ComplexityMeasures(orthoComplexity, phonicComplexity);
							wordMap.put("measures", measures);
						}
					} catch (Exception e) {
						PlatformLogger.log("Exception",e.getMessage(), e);
					}
					returnMap.put(lemma, wordMap);
					if (StringUtils.isNotBlank(primarySynsetId)) {
						synsetIdMap.put(lemma, primarySynsetId);
						synsetIds.add(primarySynsetId);
					}
				}
			}
			getNodes(nodeMap, languageId, synsetIds, synsetIds.size());
			if (null != synsetIdMap && !synsetIdMap.isEmpty() && (equivalentWords || relatedWords)) {
				Map<String, Set<String>> lemmaMap = new HashMap<String, Set<String>>();
				if (equivalentWords)
					updateEquivalentWords(returnMap, synsetIdMap, nodeMap, lemmaMap);
				if (relatedWords)
					updateRelatedWords(languageId, returnMap, synsetIdMap, lemmaMap, nodeMap, limit);
			}
		}
		Response response = OK();
		response.getResult().putAll(returnMap);
		return response;
	}

	/**
	 * Search words.
	 *
	 * @param languageId
	 *            the language id
	 * @param words
	 *            the words
	 * @return the list
	 */
	@SuppressWarnings("unchecked")
	private List<Node> searchWords(String languageId, List<String> words) {
		SearchCriteria sc = new SearchCriteria();
		sc.setNodeType(SystemNodeTypes.DATA_NODE.name());
		sc.setObjectType(objectType);
		List<Filter> filters = new ArrayList<Filter>();
		if (null != words && !words.isEmpty()) {
			for (String word : words)
				filters.add(new Filter("lemma", SearchConditions.OP_EQUAL, word));
		}
		MetadataCriterion mc = MetadataCriterion.create(filters);
		mc.setOp(SearchConditions.LOGICAL_OR);
		sc.addMetadata(mc);
		MetadataCriterion mc2 = MetadataCriterion
				.create(Arrays.asList(new Filter(ATTRIB_STATUS, SearchConditions.OP_EQUAL, "Live")));
		sc.addMetadata(mc2);
		Request req = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
				GraphDACParams.search_criteria.name(), sc);
		req.put(GraphDACParams.get_tags.name(), true);
		Response listRes = getResponse(req);
		if (checkError(listRes))
			throw new ServerException(LanguageErrorCodes.ERR_PARSER_ERROR.name(), "Search failed");
		else {
			List<Node> nodes = (List<Node>) listRes.get(GraphDACParams.node_list.name());
			return nodes;
		}
	}

	/**
	 * Gets the nodes.
	 *
	 * @param nodeMap
	 *            the node map
	 * @param languageId
	 *            the language id
	 * @param ids
	 *            the ids
	 * @param limit
	 *            the limit
	 * @return the nodes
	 */
	@SuppressWarnings("unchecked")
	private void getNodes(Map<String, Node> nodeMap, String languageId, Set<String> ids, int limit) {
		List<String> identifiers = new ArrayList<String>();
		for (String id : ids) {
			if (!nodeMap.containsKey(id))
				identifiers.add(id);
		}
		if (identifiers.size() > limit) {
			List<String> resizedList = resizeList(identifiers, limit);
			identifiers = new ArrayList<String>(resizedList);
		}
		if (null != identifiers && identifiers.size() > 0) {
			Request req = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "getDataNodes",
					GraphDACParams.node_ids.name(), identifiers);
			Response listRes = getResponse(req);
			if (checkError(listRes))
				throw new ServerException(LanguageErrorCodes.ERR_PARSER_ERROR.name(), "Error getting synonyms");
			else {
				List<Node> nodes = (List<Node>) listRes.get(GraphDACParams.node_list.name());
				if (null != nodes && !nodes.isEmpty()) {
					for (Node node : nodes) {
						nodeMap.put(node.getIdentifier(), node);
					}
				}
			}
		}
	}

	/**
	 * Gets the primary synset id.
	 *
	 * @param node
	 *            the node
	 * @return the primary synset id
	 */
	private String getPrimarySynsetId(Node node) {
		if (null != node && null != node.getInRelations() && !node.getInRelations().isEmpty()) {
			String primaryMeaningId = (String) node.getMetadata().get(ATTRIB_PRIMARY_MEANING_ID);
			if (StringUtils.isNotBlank(primaryMeaningId))
				return primaryMeaningId;
			else
				return node.getInRelations().get(0).getStartNodeId();
		}
		return null;
	}

	/**
	 * Gets the hypernym ids.
	 *
	 * @param node
	 *            the node
	 * @return the hypernym ids
	 */
	private Set<String> getHypernymIds(Node node) {
		if (null != node && null != node.getOutRelations() && !node.getOutRelations().isEmpty()) {
			Set<String> hypernymIds = new HashSet<String>();
			for (Relation rel : node.getOutRelations()) {
				if (StringUtils.equalsIgnoreCase(RelationTypes.HYPERNYM.relationName(), rel.getRelationType())) {
					hypernymIds.add(rel.getEndNodeId());
				}
			}
			return hypernymIds;
		}
		return null;
	}

	/**
	 * Gets the hyponym ids.
	 *
	 * @param node
	 *            the node
	 * @return the hyponym ids
	 */
	private Set<String> getHyponymIds(Node node) {
		if (null != node && null != node.getInRelations() && !node.getInRelations().isEmpty()) {
			Set<String> hypernymIds = new HashSet<String>();
			for (Relation rel : node.getInRelations()) {
				if (StringUtils.equalsIgnoreCase(RelationTypes.HYPERNYM.relationName(), rel.getRelationType())) {
					hypernymIds.add(rel.getStartNodeId());
				}
			}
			return hypernymIds;
		}
		return null;
	}

	/**
	 * Gets the word lemmas.
	 *
	 * @param node
	 *            the node
	 * @return the word lemmas
	 */
	private Set<String> getWordLemmas(Node node) {
		if (null != node && null != node.getOutRelations() && !node.getOutRelations().isEmpty()) {
			Set<String> lemmas = new HashSet<String>();
			for (Relation rel : node.getOutRelations()) {
				if (StringUtils.equalsIgnoreCase(RelationTypes.SYNONYM.relationName(), rel.getRelationType())) {
					Map<String, Object> metadata = rel.getEndNodeMetadata();
					if (null != metadata && !metadata.isEmpty()) {
						String lemma = (String) metadata.get("lemma");
						if (StringUtils.isNotBlank(lemma))
							lemmas.add(lemma);
					}
				}
			}
			return lemmas;
		}
		return null;
	}

	/**
	 * Resize list.
	 *
	 * @param list
	 *            the list
	 * @param limit
	 *            the limit
	 * @return the list
	 */
	private List<String> resizeList(List<String> list, int limit) {
		if (null != list && !list.isEmpty()) {
			if (list.size() > limit) {
				List<String> resizedList = new ArrayList<String>();
				Iterator<String> iter = list.iterator();
				int index = 0;
				while (index < limit) {
					resizedList.add(iter.next());
					index += 1;
				}
				return resizedList;
			} else {
				return list;
			}
		}
		return list;
	}

	/**
	 * Update equivalent words.
	 *
	 * @param returnMap
	 *            the return map
	 * @param synsetIdMap
	 *            the synset id map
	 * @param nodeMap
	 *            the node map
	 * @param lemmaMap
	 *            the lemma map
	 */
	@SuppressWarnings("unchecked")
	private void updateEquivalentWords(Map<String, Map<String, Object>> returnMap, Map<String, String> synsetIdMap,
			Map<String, Node> nodeMap, Map<String, Set<String>> lemmaMap) {
		for (Entry<String, Map<String, Object>> entry : returnMap.entrySet()) {
			String lemma = entry.getKey();
			Map<String, Object> wordMap = entry.getValue();
			String synsetId = synsetIdMap.get(lemma);
			if (StringUtils.isNotBlank(synsetId)) {
				Set<String> words = (Set<String>) wordMap.get("equivalentWords");
				if (null == words)
					words = new HashSet<String>();
				Node synset = nodeMap.get(synsetId);
				if (null != synset) {
					Set<String> lemmas = lemmaMap.get(synsetId);
					if (null == lemmas) {
						lemmas = getWordLemmas(synset);
						if (null != lemmas)
							lemmaMap.put(synsetId, lemmas);
					}
					if (null != lemmas && !lemmas.isEmpty()) {
						Set<String> eqWords = removeWordFromList(lemmas, lemma);
						if (null != eqWords && !eqWords.isEmpty())
							words.addAll(eqWords);
					}
				}
				if (null != words && !words.isEmpty())
					wordMap.put("equivalentWords", words);
			}
		}
	}

	/**
	 * Update related words.
	 *
	 * @param languageId
	 *            the language id
	 * @param returnMap
	 *            the return map
	 * @param synsetIdMap
	 *            the synset id map
	 * @param lemmaMap
	 *            the lemma map
	 * @param nodeMap
	 *            the node map
	 * @param limit
	 *            the limit
	 */
	@SuppressWarnings("unchecked")
	private void updateRelatedWords(String languageId, Map<String, Map<String, Object>> returnMap,
			Map<String, String> synsetIdMap, Map<String, Set<String>> lemmaMap, Map<String, Node> nodeMap, int limit) {
		for (Entry<String, Map<String, Object>> entry : returnMap.entrySet()) {
			String lemma = entry.getKey();
			PlatformLogger.log("Getting related words for : " + lemma);
			Map<String, Object> wordMap = entry.getValue();
			String synsetId = synsetIdMap.get(lemma);
			if (StringUtils.isNotBlank(synsetId)) {
				Set<String> words = (Set<String>) wordMap.get("relatedWords");
				if (null == words)
					words = new HashSet<String>();
				Set<String> hypernymIds = new HashSet<String>();
				Node synset = nodeMap.get(synsetId);
				if (null != synset) {
					Set<String> list = getHypernymIds(synset);
					if (null != list && !list.isEmpty()) {
						hypernymIds.addAll(list);
					}
				}
				if (null != hypernymIds && !hypernymIds.isEmpty()) {
					getNodes(nodeMap, languageId, hypernymIds, limit);
					Set<String> hyponymIds = new HashSet<String>();
					for (String hypernymId : hypernymIds) {
						Node hypernym = nodeMap.get(hypernymId);
						if (null != hypernym) {
							Set<String> list = getHyponymIds(hypernym);
							if (null != list && !list.isEmpty()) {
								hyponymIds.addAll(list);
							}
						}
					}
					if (null != hyponymIds && !hyponymIds.isEmpty()) {
						PlatformLogger.log("hyponymIds count: " + hyponymIds.size());
						getNodes(nodeMap, languageId, hyponymIds, limit);
						for (String hyponymId : hyponymIds) {
							Node hyponym = nodeMap.get(hyponymId);
							if (null != hyponym) {
								Set<String> lemmas = lemmaMap.get(hyponymId);
								if (null == lemmas) {
									lemmas = getWordLemmas(hyponym);
									if (null != lemmas)
										lemmaMap.put(hyponymId, lemmas);
								}
								if (null != lemmas && !lemmas.isEmpty()) {
									Set<String> relatedWords = removeWordFromList(lemmas, lemma);
									if (null != relatedWords && !relatedWords.isEmpty())
										words.addAll(relatedWords);
								}
							}
						}
					}
				}
				if (null != words && !words.isEmpty()) {
					PlatformLogger.log("Related words count: " + words.size());
					wordMap.put("relatedWords", words);
				}
			}
		}
	}

	/**
	 * Removes the word from list.
	 *
	 * @param words
	 *            the words
	 * @param word
	 *            the word
	 * @return the sets the
	 */
	private Set<String> removeWordFromList(Set<String> words, String word) {
		Set<String> list = new HashSet<String>(words);
		list.remove(word);
		return list;
	}

}
