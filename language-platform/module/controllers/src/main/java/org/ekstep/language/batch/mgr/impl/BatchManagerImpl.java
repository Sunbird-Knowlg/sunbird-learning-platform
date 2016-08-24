package org.ekstep.language.batch.mgr.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.batch.mgr.IBatchManager;
import org.ekstep.language.common.enums.LanguageActorNames;
import org.ekstep.language.common.enums.LanguageOperations;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.measures.entity.WordComplexity;
import org.ekstep.language.util.BaseLanguageManager;
import org.ekstep.language.util.ControllerUtil;
import org.ekstep.language.util.IWordnetConstants;
import org.ekstep.language.util.WordUtil;
import org.ekstep.language.util.WordnetUtil;
import org.ekstep.language.wordchian.WordChainUtil;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ResourceNotFoundException;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.engine.router.GraphEngineManagers;

@Component
public class BatchManagerImpl extends BaseLanguageManager implements IBatchManager, IWordnetConstants {

    private ControllerUtil controllerUtil = new ControllerUtil();
    private WordUtil wordUtil = new WordUtil();
	private WordChainUtil wordChainUtil = new WordChainUtil();
    private static Logger LOGGER = LogManager.getLogger(IBatchManager.class.getName());

    private static final int BATCH = 1000;
    
    @Override
    @SuppressWarnings("unchecked")
	public Response correctWordnetData(String languageId) {
    	int startPosistion = 0;
        boolean found = true;
        while (found) {
        	List<Node> nodes = getAllWords(languageId, startPosistion, BATCH);
            if (null != nodes && !nodes.isEmpty()) {
            	List<String> words = new ArrayList<String>();
                Map<String, Node> nodeMap = new HashMap<String, Node>();
                controllerUtil.getNodeMap(nodes, nodeMap, words);
                Request langReq = getLanguageRequest(languageId, LanguageActorNames.LEXILE_MEASURES_ACTOR.name(),
                        LanguageOperations.getWordFeatures.name());
                langReq.put(LanguageParams.words.name(), words);
                Response langRes = getLanguageResponse(langReq, LOGGER);
                Map<String, WordComplexity> featureMap = new HashMap<String, WordComplexity>();
                if (!checkError(langRes)) {
                	featureMap = (Map<String, WordComplexity>) langRes.get(LanguageParams.word_features.name());
                	if (null != featureMap)
                		System.out.println("Word features returned for " + featureMap.size() + " words");
                }
            	for (Node node : nodes) {
            		Object pictures = null;
                    Object wordImages = (Object) node.getMetadata().get(ATTRIB_PICTURES);
                    String primaryMeaning = (String) node.getMetadata().get(ATTRIB_PRIMARY_MEANING_ID);
                    List<Relation> inRels = node.getInRelations();
                    int synsetCount = 0;
                    if (null != inRels && !inRels.isEmpty()) {
                        for (Relation rel : inRels) {
                            if (StringUtils.equalsIgnoreCase(rel.getRelationType(),
                                    RelationTypes.SYNONYM.relationName())
                                    && StringUtils.equalsIgnoreCase(rel.getStartNodeObjectType(), OBJECTTYPE_SYNSET)) {
                            	synsetCount += 1;
                            	String synsetId = rel.getStartNodeId();
                            	if (StringUtils.isBlank(primaryMeaning))
                            		primaryMeaning = synsetId;
                                if (StringUtils.equalsIgnoreCase(synsetId, primaryMeaning)) {
                                    pictures = (Object) rel.getStartNodeMetadata().get(ATTRIB_PICTURES);
                                }
                            }
                        }
                    }
                    node.getMetadata().put(ATTRIB_PRIMARY_MEANING_ID, primaryMeaning);
                    node.getMetadata().put(ATTRIB_PICTURES, pictures);
                    node.getMetadata().put(ATTRIB_WORD_IMAGES, wordImages);
                    node.getMetadata().put(ATTRIB_SYNSET_COUNT, synsetCount);
                    WordnetUtil.updatePOS(node);
                    if (null != featureMap && !featureMap.isEmpty()) {
                    	String lemma = (String) node.getMetadata().get(ATTRIB_LEMMA);
                    	WordComplexity wc = null;
                    	if (StringUtils.isNotBlank(lemma))
                    		wc = featureMap.get(lemma);
                    	if (null != wc) {
                            node.getMetadata().put("syllableCount", wc.getCount());
                            node.getMetadata().put("syllableNotation", wc.getNotation());
                            node.getMetadata().put("unicodeNotation", wc.getUnicode());
                            node.getMetadata().put("orthographic_complexity", wc.getOrthoComplexity());
                            node.getMetadata().put("phonologic_complexity", wc.getPhonicComplexity());
                            
    						try {
    							wordChainUtil.updateWordSet(languageId, node, wc);
    						} catch (Exception e) {
    							System.out.println("Update error : " + node.getIdentifier() + " : " + e.getMessage());
    						}
                        }
                    }
                    try {
                        wordUtil.getWordComplexity(node, languageId);
                    } catch (Exception e) {
                        System.out.println("Update wordcomplexity error : " + node.getIdentifier() + " : " + e.getMessage());
                        Request updateReq = getRequest(languageId, GraphEngineManagers.NODE_MANAGER, "updateDataNode");
                        updateReq.put(GraphDACParams.node.name(), node);
                        updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());
                        try {
                            getResponse(updateReq, LOGGER);
                        } catch (Exception ex) {
                            System.out.println("Update error : " + node.getIdentifier() + " : " + ex.getMessage());
                        }
                    }
            	}
            	System.out.println("CorrectWordnetData complete from " + startPosistion + " - " + BATCH + " words");
                startPosistion += BATCH;
            } else {
                found = false;
                break;
            }
        }
        return OK("status", "OK");
    }

    @Override
    public Response updatePictures(String languageId) {
        int startPosistion = 0;
        boolean found = true;
        while (found) {
            List<Node> nodes = getAllWords(languageId, startPosistion, BATCH);
            if (null != nodes && !nodes.isEmpty()) {
                for (Node node : nodes) {
                    Object pictures = null;
                    Object wordImages = (Object) node.getMetadata().get(ATTRIB_PICTURES);
                    String primaryMeaning = (String) node.getMetadata().get(ATTRIB_PRIMARY_MEANING_ID);
                    List<Relation> inRels = node.getInRelations();
                    if (null != inRels && !inRels.isEmpty()) {
                        for (Relation rel : inRels) {
                            if (StringUtils.equalsIgnoreCase(rel.getRelationType(),
                                    RelationTypes.SYNONYM.relationName())
                                    && StringUtils.equalsIgnoreCase(rel.getStartNodeObjectType(), OBJECTTYPE_SYNSET)) {
                                String synsetId = rel.getStartNodeId();
                                if (StringUtils.equalsIgnoreCase(synsetId, primaryMeaning)) {
                                    pictures = (Object) rel.getStartNodeMetadata().get(ATTRIB_PICTURES);
                                    break;
                                }
                            }
                        }
                    }
                    node.getMetadata().put(ATTRIB_PICTURES, pictures);
                    node.getMetadata().put(ATTRIB_WORD_IMAGES, wordImages);
                    Request updateReq = getRequest(languageId, GraphEngineManagers.NODE_MANAGER, "updateDataNode");
                    updateReq.put(GraphDACParams.node.name(), node);
                    updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());
                    try {
                        getResponse(updateReq, LOGGER);
                    } catch (Exception e) {
                        System.out.println("Update error : " + node.getIdentifier() + " : " + e.getMessage());
                    }
                }
                System.out.println("UpdatePictures complete from " + startPosistion + " - " + BATCH + " words");
                startPosistion += BATCH;
            } else {
                found = false;
                break;
            }
        }
        return OK("status", "OK");
    }

    @Override
    public Response cleanupWordNetData(String languageId) {
        int startPosistion = 0;
        boolean found = true;
        while (found) {
            List<Node> nodes = getAllWords(languageId, startPosistion, BATCH);
            if (null != nodes && !nodes.isEmpty()) {
                List<String> list = new ArrayList<String>();
                list.add(ATTRIB_SOURCE_IWN);
                for (Node node : nodes) {
                    if (isIWNWord(node.getInRelations())) {
                        if (!checkSourceMetadata(node)) {
                            WordnetUtil.updatePOS(node);
                            Object pos = node.getMetadata().get(ATTRIB_POS);
                            Node wordNode = new Node(node.getIdentifier(), node.getNodeType(), node.getObjectType());
                            wordNode.setGraphId(node.getGraphId());
                            Map<String, Object> metadata = new HashMap<String, Object>();
                            metadata.put(ATTRIB_SOURCES, list);
                            metadata.put(ATTRIB_POS, pos);
                            metadata.put(ATTRIB_STATUS, "Draft");
                            wordNode.setMetadata(metadata);
                            Request updateReq = getRequest(languageId, GraphEngineManagers.NODE_MANAGER,
                                    "updateDataNode");
                            updateReq.put(GraphDACParams.node.name(), wordNode);
                            updateReq.put(GraphDACParams.node_id.name(), wordNode.getIdentifier());
                            try {
                                getResponse(updateReq, LOGGER);
                            } catch (Exception e) {
                                System.out
                                        .println("Update error : " + wordNode.getIdentifier() + " : " + e.getMessage());
                            }
                        }
                    } else {
                        Request deleteReq = getRequest(languageId, GraphEngineManagers.NODE_MANAGER, "deleteDataNode");
                        deleteReq.put(GraphDACParams.node_id.name(), node.getIdentifier());
                        try {
                            getResponse(deleteReq, LOGGER);
                        } catch (Exception e) {
                            System.out.println("Delete error : " + node.getIdentifier() + " : " + e.getMessage());
                        }
                    }
                }
                System.out.println("cleanupWordNetData complete from " + startPosistion + " - " + BATCH + " words");
                startPosistion += BATCH;
            } else {
                found = false;
                break;
            }
        }
        return OK("status", "OK");
    }

    @Override
    @SuppressWarnings("unchecked")
	public Response updateWordChain(String languageId, Integer startPosition, Integer total) {
        int start = 0;
        if (null != startPosition && startPosition.intValue() > 0)
        	start = startPosition.intValue();
        int batch = BATCH;
        if (null != total && total < batch)
        	batch = total;
        int fetchCount = 0;
        boolean found = true;
        while (found) {
        	List<Node> nodes = getAllWords(languageId, start, batch);
        	fetchCount += batch;
            if (null != nodes && !nodes.isEmpty()) {
            	List<String> words = new ArrayList<String>();
                Map<String, Node> nodeMap = new HashMap<String, Node>();
                controllerUtil.getNodeMap(nodes, nodeMap, words);
                Request langReq = getLanguageRequest(languageId, LanguageActorNames.LEXILE_MEASURES_ACTOR.name(),
                        LanguageOperations.getWordFeatures.name());
                langReq.put(LanguageParams.words.name(), words);
                Response langRes = getLanguageResponse(langReq, LOGGER);
                Map<String, WordComplexity> featureMap = new HashMap<String, WordComplexity>();
                if (!checkError(langRes)) {
                	featureMap = (Map<String, WordComplexity>) langRes.get(LanguageParams.word_features.name());
                	if (null != featureMap)
                		System.out.println("Word features returned for " + featureMap.size() + " words");
                }
            	for (Node node : nodes) {
                    if (null != featureMap && !featureMap.isEmpty()) {
                    	String lemma = (String) node.getMetadata().get(ATTRIB_LEMMA);
                    	WordComplexity wc = null;
                    	if (StringUtils.isNotBlank(lemma))
                    		wc = featureMap.get(lemma);
                    	if (null != wc) {
    						try {
    							wordChainUtil.updateWordSet(languageId, node, wc);
    						} catch (Exception e) {
    							System.out.println("Update error : " + node.getIdentifier() + " : " + e.getMessage());
    						}
                        }
                    }
            	}
                start += batch;
                if (null != total && fetchCount >= total) {
                	found = false;
                	break;
                }

            } else {
                found = false;
                break;
            }
        }
        return OK("status", "OK");
    }
    
    @SuppressWarnings("rawtypes")
    private boolean checkSourceMetadata(Node node) {
        Map<String, Object> metadata = node.getMetadata();
        Object val = metadata.get(ATTRIB_SOURCES);
        if (null != val) {
            if (val instanceof Object[]) {
                for (Object obj : (Object[]) val) {
                    if (null != obj && StringUtils.equals(ATTRIB_SOURCE_IWN, obj.toString()))
                        return true;
                }
            } else if (val instanceof List) {
                for (Object obj : (List) val) {
                    if (null != obj && StringUtils.equals(ATTRIB_SOURCE_IWN, obj.toString()))
                        return true;
                }
            }
        }
        return false;
    }

    private boolean isIWNWord(List<Relation> inRels) {
        if (null != inRels && inRels.size() > 0) {
            for (Relation inRel : inRels) {
                if (StringUtils.equalsIgnoreCase(inRel.getStartNodeObjectType(), OBJECTTYPE_SYNSET)) {
                    Map<String, Object> metadata = inRel.getStartNodeMetadata();
                    Object iwnId = metadata.get(ATTRIB_IWN_ID);
                    if (null != iwnId && StringUtils.isNotBlank(iwnId.toString()))
                        return true;
                }
            }
        }
        return false;
    }

    private List<String> getSynsetIds(List<Relation> inRels) {
        List<String> synsetIds = new ArrayList<String>();
        if (null != inRels && inRels.size() > 0) {
            for (Relation inRel : inRels) {
                if (StringUtils.equalsIgnoreCase(inRel.getStartNodeObjectType(), OBJECTTYPE_SYNSET)
                        && StringUtils.equalsIgnoreCase(inRel.getRelationType(), RelationTypes.SYNONYM.relationName()))
                    synsetIds.add(inRel.getStartNodeId());
            }
        }
        return synsetIds;
    }

    @Override
    public Response setPrimaryMeaning(String languageId) {
        int startPosistion = 0;
        boolean found = true;
        while (found) {
            List<Node> nodes = getAllWords(languageId, startPosistion, BATCH);
            if (null != nodes && !nodes.isEmpty()) {
                for (Node node : nodes) {
                    Map<String, Object> metadata = node.getMetadata();
                    String lemma = (String) metadata.get(ATTRIB_LEMMA);
                    Object isPhrase = metadata.get(ATTRIB_IS_PHRASE);
                    Object value = metadata.get(ATTRIB_PRIMARY_MEANING_ID);
                    Integer count = null;
                    List<String> synsetIds = getSynsetIds(node.getInRelations());
                    if (null != synsetIds && !synsetIds.isEmpty())
                        count = synsetIds.size();
                    Node wordNode = new Node(node.getIdentifier(), node.getNodeType(), node.getObjectType());
                    wordNode.setGraphId(node.getGraphId());
                    Map<String, Object> wordMetadata = new HashMap<String, Object>();
                    wordMetadata.put(ATTRIB_SYNSET_COUNT, count);
                    if (null == value || StringUtils.isBlank(value.toString())) {
                        if (null != synsetIds && !synsetIds.isEmpty()) {
                            String id = synsetIds.get(0);
                            wordMetadata.put(ATTRIB_PRIMARY_MEANING_ID, id);
                        }
                    }
                    if (null == isPhrase && StringUtils.isNotBlank(lemma) && lemma.trim().contains(" "))
                        wordMetadata.put(ATTRIB_IS_PHRASE, true);
                    wordNode.setMetadata(wordMetadata);
                    Request updateReq = getRequest(languageId, GraphEngineManagers.NODE_MANAGER,
                            "updateDataNode");
                    updateReq.put(GraphDACParams.node.name(), wordNode);
                    updateReq.put(GraphDACParams.node_id.name(), wordNode.getIdentifier());
                    try {
                        getResponse(updateReq, LOGGER);
                    } catch (Exception e) {
                        System.out
                                .println("Update error : " + wordNode.getIdentifier() + " : " + e.getMessage());
                    }
                }
                System.out.println("setPrimaryMeaning complete from " + startPosistion + " - " + BATCH + " words");
                startPosistion += BATCH;
            } else {
                found = false;
                break;
            }
        }
        return OK("status", "OK");
    }

    @Override
    public Response updatePosList(String languageId) {
        int startPosistion = 0;
        boolean found = true;
        while (found) {
            List<Node> nodes = getAllWords(languageId, startPosistion, BATCH);
            if (null != nodes && !nodes.isEmpty()) {
                for (Node node : nodes) {
                    WordnetUtil.updatePOS(node);
                    Request updateReq = getRequest(languageId, GraphEngineManagers.NODE_MANAGER, "updateDataNode");
                    updateReq.put(GraphDACParams.node.name(), node);
                    updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());
                    try {
                        getResponse(updateReq, LOGGER);
                    } catch (Exception e) {
                        System.out.println("Update error : " + node.getIdentifier() + " : " + e.getMessage());
                    }
                }
                System.out.println("updatePosList complete from " + startPosistion + " - " + BATCH + " words");
                startPosistion += BATCH;
            } else {
                found = false;
                break;
            }
        }
        return OK("status", "OK");
    }

    @Override
    public Response updateWordComplexity(String languageId) {
        int startPosistion = 0;
        boolean found = true;
        while (found) {
            List<Node> nodes = getAllWords(languageId, startPosistion, BATCH);
            if (null != nodes && !nodes.isEmpty()) {
                for (Node node : nodes) {
                    try {
                        wordUtil.getWordComplexity(node, languageId);
                    } catch (Exception e) {
                        System.out.println("Update error : " + node.getIdentifier() + " : " + e.getMessage());
                    }
                }
                System.out.println("updateWordComplexity complete from " + startPosistion + " - " + BATCH + " words");
                startPosistion += BATCH;
            } else {
                found = false;
                break;
            }
        }
        return OK("status", "OK");
    }

    @SuppressWarnings("unchecked")
    @Override
    public Response updateWordFeatures(String languageId) {
        int startPosistion = 0;
        boolean found = true;
        while (found) {
            List<Node> nodes = getAllWords(languageId, startPosistion, BATCH);
            if (null != nodes && !nodes.isEmpty()) {
                System.out.println("Total words: " + nodes.size());
                List<String> words = new ArrayList<String>();
                Map<String, Node> nodeMap = new HashMap<String, Node>();
                controllerUtil.getNodeMap(nodes, nodeMap, words);
                Request langReq = getLanguageRequest(languageId, LanguageActorNames.LEXILE_MEASURES_ACTOR.name(),
                        LanguageOperations.getWordFeatures.name());
                langReq.put(LanguageParams.words.name(), words);
                Response langRes = getLanguageResponse(langReq, LOGGER);
                if (checkError(langRes))
                    return langRes;
                else {
                    Map<String, WordComplexity> featureMap = (Map<String, WordComplexity>) langRes
                            .get(LanguageParams.word_features.name());
                    if (null != featureMap && !featureMap.isEmpty()) {
                        System.out.println("Word features returned for " + featureMap.size() + " words");
                        for (Entry<String, WordComplexity> entry : featureMap.entrySet()) {
                            Node node = nodeMap.get(entry.getKey());
                            WordComplexity wc = entry.getValue();
                            if (null != node && null != wc) {
                                node.getMetadata().put("syllableCount", wc.getCount());
                                node.getMetadata().put("syllableNotation", wc.getNotation());
                                node.getMetadata().put("unicodeNotation", wc.getUnicode());
                                node.getMetadata().put("orthographic_complexity", wc.getOrthoComplexity());
                                node.getMetadata().put("phonologic_complexity", wc.getPhonicComplexity());
                                Request updateReq = getRequest(languageId, GraphEngineManagers.NODE_MANAGER,
                                        "updateDataNode");
                                updateReq.put(GraphDACParams.node.name(), node);
                                updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());
                                try {
                                    getResponse(updateReq, LOGGER);
                                } catch (Exception e) {
                                    System.out
                                            .println("Update error : " + node.getIdentifier() + " : " + e.getMessage());
                                }
                            }
                        }
                    }
                }
                System.out.println("updateWordFeatures complete from " + startPosistion + " - " + BATCH + " words");
                startPosistion += BATCH;
            } else {
                found = false;
                break;
            }
        }
        return OK("status", "OK");
    }

    @SuppressWarnings("unchecked")
    @Override
    public Response updateFrequencyCounts(String languageId) {
        int startPosistion = 0;
        boolean found = true;
        while (found) {
            List<Node> nodes = getAllWords(languageId, startPosistion, BATCH);
            if (null != nodes && !nodes.isEmpty()) {
                String[] groupBy = new String[] { "pos", "sourceType", "source", "grade" };
                List<String> words = new ArrayList<String>();
                Map<String, Node> nodeMap = new HashMap<String, Node>();
                controllerUtil.getNodeMap(nodes, nodeMap, words);
                if (null != words && !words.isEmpty()) {
                    System.out.println("Total words: " + nodes.size());
                    Map<String, Object> indexesMap = new HashMap<String, Object>();
                    Map<String, Object> wordInfoMap = new HashMap<String, Object>();
                    List<String> groupList = Arrays.asList(groupBy);
                    controllerUtil.getIndexInfo(languageId, indexesMap, words, groupList);
                    System.out.println("indexesMap size: " + indexesMap.size());
                    controllerUtil.getWordInfo(languageId, wordInfoMap, words);
                    System.out.println("wordInfoMap size: " + wordInfoMap.size());
                    if (null != nodeMap && !nodeMap.isEmpty()) {
                        for (Entry<String, Node> entry : nodeMap.entrySet()) {
                            Node node = entry.getValue();
                            String lemma = entry.getKey();
                            boolean update = false;
                            Map<String, Object> index = (Map<String, Object>) indexesMap.get(lemma);
                            List<Map<String, Object>> wordInfo = (List<Map<String, Object>>) wordInfoMap.get(lemma);
                            if (null != index) {
                                Map<String, Object> citations = (Map<String, Object>) index.get("citations");
                                if (null != citations && !citations.isEmpty()) {
                                    Object count = citations.get("count");
                                    if (null != count)
                                        node.getMetadata().put("occurrenceCount", count);
                                    controllerUtil.setCountsMetadata(node, citations, "sourceType", null);
                                    controllerUtil.setCountsMetadata(node, citations, "source", "source");
                                    controllerUtil.setCountsMetadata(node, citations, "grade", "grade");
                                    controllerUtil.setCountsMetadata(node, citations, "pos", "pos");
                                    controllerUtil.addTags(node, citations, "source");
                                    controllerUtil.updatePosList(node, citations);
                                    controllerUtil.updateSourceTypesList(node, citations);
                                    controllerUtil.updateSourcesList(node, citations);
                                    controllerUtil.updateGradeList(node, citations);
                                    update = true;
                                }
                            }
                            if (null != wordInfo && !wordInfo.isEmpty()) {
                                for (Map<String, Object> info : wordInfo) {
                                    controllerUtil.updateStringMetadata(node, info, "word", "variants");
                                    controllerUtil.updateStringMetadata(node, info, "category", "pos_categories");
                                    controllerUtil.updateStringMetadata(node, info, "gender", "genders");
                                    controllerUtil.updateStringMetadata(node, info, "number", "plurality");
                                    controllerUtil.updateStringMetadata(node, info, "pers", "person");
                                    controllerUtil.updateStringMetadata(node, info, "grammaticalCase", "cases");
                                    controllerUtil.updateStringMetadata(node, info, "inflection", "inflections");
                                }
                                update = true;
                            }
                            if (update) {
                                Request updateReq = getRequest(languageId, GraphEngineManagers.NODE_MANAGER,
                                        "updateDataNode");
                                updateReq.put(GraphDACParams.node.name(), node);
                                updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());
                                try {
                                    getResponse(updateReq, LOGGER);
                                } catch (Exception e) {
                                    System.out
                                            .println("Update error : " + node.getIdentifier() + " : " + e.getMessage());
                                }
                            }
                        }
                    }
                }
                System.out.println("updateFrequencyCounts complete from " + startPosistion + " - " + BATCH + " words");
                startPosistion += BATCH;
            } else {
                found = false;
                break;
            }
        }
        return OK("status", "OK");
    }

    @SuppressWarnings("unchecked")
    private List<Node> getAllWords(String languageId, int startPosition, int batchSize) {
        SearchCriteria sc = new SearchCriteria();
        sc.setNodeType(SystemNodeTypes.DATA_NODE.name());
        sc.setObjectType(OBJECTTYPE_WORD);
        sc.setResultSize(batchSize);
        sc.setStartPosition(startPosition);
        Request req = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
                GraphDACParams.search_criteria.name(), sc);
        req.put(GraphDACParams.get_tags.name(), true);
        Response listRes = getResponse(req, LOGGER);
        if (checkError(listRes))
            throw new ResourceNotFoundException("WORDS_NOT_FOUND", "Words not found for language: " + languageId);
        else {
            List<Node> nodes = (List<Node>) listRes.get(GraphDACParams.node_list.name());
            return nodes;
        }
    }
}
