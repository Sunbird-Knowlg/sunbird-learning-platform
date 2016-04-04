package org.ekstep.language.mgr.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.common.enums.LanguageErrorCodes;
import org.ekstep.language.measures.entity.ComplexityMeasures;
import org.ekstep.language.mgr.IParserManager;
import org.ekstep.language.util.LanguageUtil;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.model.Filter;
import com.ilimi.graph.dac.model.MetadataCriterion;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.dac.model.SearchConditions;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.engine.router.GraphEngineManagers;

@Component
public class ParserManagerImpl extends BaseLanguageManager implements IParserManager {

    private static Logger LOGGER = LogManager.getLogger(ParserManagerImpl.class.getName());

    private String objectType = "Word";

    public Response parseContent(String languageId, String content, Boolean wordSuggestions, Boolean relatedWords,
            Boolean translations, Boolean equivalentWords, Integer limit) {
        List<String> tokens = LanguageUtil.getTokens(content);
        if (null == limit || limit.intValue() <= 0)
            limit = 10;
        List<Node> nodes = searchWords(languageId, tokens);
        Map<String, Node> nodeMap = new HashMap<String, Node>();
        Map<String, Set<String>> synsetIdMap = new HashMap<String, Set<String>>();
        Map<String, Map<String, Object>> returnMap = new HashMap<String, Map<String, Object>>();
        if (null != nodes && !nodes.isEmpty()) {
            System.out.println("Number of words: " + nodes.size());
            for (Node node : nodes) {
                Set<String> list = getSynsetIds(node);
                if (null != node.getMetadata() && !node.getMetadata().isEmpty()) {
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
                        System.out.println(e.getMessage());
                    }
                    returnMap.put(lemma, wordMap);
                    if (null != list && !list.isEmpty()) {
                        synsetIdMap.put(lemma, list);
                        getNodes(nodeMap, languageId, list, limit);
                    }
                }
            }
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
        Request req = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
                GraphDACParams.search_criteria.name(), sc);
        req.put(GraphDACParams.get_tags.name(), true);
        Response listRes = getResponse(req, LOGGER);
        if (checkError(listRes))
            throw new ServerException(LanguageErrorCodes.ERR_PARSER_ERROR.name(), "Search failed");
        else {
            List<Node> nodes = (List<Node>) listRes.get(GraphDACParams.node_list.name());
            return nodes;
        }
    }

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
            Response listRes = getResponse(req, LOGGER);
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

    private Set<String> getSynsetIds(Node node) {
        if (null != node && null != node.getInRelations() && !node.getInRelations().isEmpty()) {
            Set<String> synsetIds = new HashSet<String>();
            for (Relation rel : node.getInRelations()) {
                if (StringUtils.equalsIgnoreCase(RelationTypes.SYNONYM.relationName(), rel.getRelationType())) {
                    synsetIds.add(rel.getStartNodeId());
                }
            }
            return synsetIds;
        }
        return null;
    }

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

    @SuppressWarnings("unchecked")
    private void updateEquivalentWords(Map<String, Map<String, Object>> returnMap, Map<String, Set<String>> synsetIdMap,
            Map<String, Node> nodeMap, Map<String, Set<String>> lemmaMap) {
        for (Entry<String, Map<String, Object>> entry : returnMap.entrySet()) {
            String lemma = entry.getKey();
            Map<String, Object> wordMap = entry.getValue();
            Set<String> synsetIds = synsetIdMap.get(lemma);
            if (null != synsetIds && !synsetIds.isEmpty()) {
                Set<String> words = (Set<String>) wordMap.get("equivalentWords");
                if (null == words)
                    words = new HashSet<String>();
                for (String synsetId : synsetIds) {
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
                }
                if (null != words && !words.isEmpty())
                    wordMap.put("equivalentWords", words);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void updateRelatedWords(String languageId, Map<String, Map<String, Object>> returnMap,
            Map<String, Set<String>> synsetIdMap, Map<String, Set<String>> lemmaMap, Map<String, Node> nodeMap, int limit) {
        for (Entry<String, Map<String, Object>> entry : returnMap.entrySet()) {
            String lemma = entry.getKey();
            System.out.println("Getting related words for : " + lemma);
            Map<String, Object> wordMap = entry.getValue();
            Set<String> synsetIds = synsetIdMap.get(lemma);
            if (null != synsetIds && !synsetIds.isEmpty()) {
                Set<String> words = (Set<String>) wordMap.get("relatedWords");
                if (null == words)
                    words = new HashSet<String>();
                Set<String> hypernymIds = new HashSet<String>();
                for (String synsetId : synsetIds) {
                    Node synset = nodeMap.get(synsetId);
                    if (null != synset) {
                        Set<String> list = getHypernymIds(synset);
                        if (null != list && !list.isEmpty()) {
                            hypernymIds.addAll(list);
                        }
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
                        System.out.println("hyponymIds count: " + hyponymIds.size());
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
                    System.out.println("Related words count: " + words.size());
                    wordMap.put("relatedWords", words);
                }
            }
        }
    }

    private Set<String> removeWordFromList(Set<String> words, String word) {
        Set<String> list = new HashSet<String>(words);
        list.remove(word);
        return list;
    }

}
