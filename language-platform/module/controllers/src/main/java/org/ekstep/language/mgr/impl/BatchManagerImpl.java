package org.ekstep.language.mgr.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.common.enums.LanguageActorNames;
import org.ekstep.language.common.enums.LanguageOperations;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.measures.entity.WordComplexity;
import org.ekstep.language.mgr.IBatchManager;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ResourceNotFoundException;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.engine.router.GraphEngineManagers;

@Component
public class BatchManagerImpl extends BaseLanguageManager implements IBatchManager {

    private String objectType = "Word";

    private static Logger LOGGER = LogManager.getLogger(IBatchManager.class.getName());

    @SuppressWarnings("unchecked")
    @Override
    public Response updateWordFeatures(String languageId) {
        List<Node> nodes = getAllWords(languageId);
        if (null != nodes && !nodes.isEmpty()) {
            System.out.println("Total words: " + nodes.size());
            List<String> words = new ArrayList<String>();
            Map<String, Node> nodeMap = new HashMap<String, Node>();
            getNodeMap(nodes, nodeMap, words);
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
                            node.getMetadata().put("status", "Live");
                            Request updateReq = getRequest(languageId, GraphEngineManagers.NODE_MANAGER,
                                    "updateDataNode");
                            updateReq.put(GraphDACParams.node.name(), node);
                            updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());
                            try {
                                System.out.println("Sending update req for : " + node.getIdentifier());
                                getResponse(updateReq, LOGGER);
                                System.out.println("Update complete for : " + node.getIdentifier());
                            } catch (Exception e) {
                                System.out.println("Update error : " + node.getIdentifier() + " : " + e.getMessage());
                            }
                        }
                    }
                }
            }
        }
        return OK("status", "OK");
    }

    @SuppressWarnings("unchecked")
    @Override
    public Response updateFrequencyCounts(String languageId) {
        List<Node> nodes = getAllWords(languageId);
        if (null != nodes && !nodes.isEmpty()) {
            System.out.println("Total words: " + nodes.size());
            String[] groupBy = new String[] { "pos", "sourceType", "source", "grade" };
            List<String> words = new ArrayList<String>();
            Map<String, Node> nodeMap = new HashMap<String, Node>();
            getNodeMap(nodes, nodeMap, words);
            if (null != words && !words.isEmpty()) {
                Map<String, Object> indexesMap = new HashMap<String, Object>();
                Map<String, Object> wordInfoMap = new HashMap<String, Object>();
                List<String> groupList = Arrays.asList(groupBy);
                getIndexInfo(languageId, indexesMap, words, groupList);
                getWordInfo(languageId, wordInfoMap, words);
                if (null != nodeMap && !nodeMap.isEmpty()) {
                    for (Entry<String, Node> entry : nodeMap.entrySet()) {
                        Node node = entry.getValue();
                        String lemma = entry.getKey();
                        Map<String, Object> index = (Map<String, Object>) indexesMap.get(lemma);
                        List<Map<String, Object>> wordInfo = (List<Map<String, Object>>) wordInfoMap.get(lemma);
                        if (null != index) {
                            Map<String, Object> citations = (Map<String, Object>) index.get("citations");
                            if (null != citations && !citations.isEmpty()) {
                                Object count = citations.get("count");
                                if (null != count)
                                    node.getMetadata().put("occurrenceCount", count);
                                setCountsMetadata(node, citations, "sourceType", null);
                                setCountsMetadata(node, citations, "source", "source");
                                setCountsMetadata(node, citations, "grade", "grade");
                                setCountsMetadata(node, citations, "pos", "pos");
                                addTags(node, citations, "source");
                                updatePosList(node, citations);
                                updateSourceTypesList(node, citations);
                                updateSourcesList(node, citations);
                                updateGradeList(node, citations);
                            }
                        }
                        if (null != wordInfo && !wordInfo.isEmpty()) {
                            for (Map<String, Object> info : wordInfo) {
                                updateListMetadata(node, info, "word", "variants");
                                updateListMetadata(node, info, "category", "pos_categories");
                                updateListMetadata(node, info, "gender", "genders");
                                updateListMetadata(node, info, "number", "plurality");
                                updateListMetadata(node, info, "pers", "person");
                                updateListMetadata(node, info, "grammaticalCase", "cases");
                                updateListMetadata(node, info, "inflection", "inflections");
                            }
                        }
                        node.getMetadata().put("status", "Live");
                        Request updateReq = getRequest(languageId, GraphEngineManagers.NODE_MANAGER,
                                "updateDataNode");
                        updateReq.put(GraphDACParams.node.name(), node);
                        updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());
                        try {
                            System.out.println("Sending update req for : " + node.getIdentifier());
                            getResponse(updateReq, LOGGER);
                            System.out.println("Update complete for : " + node.getIdentifier());
                        } catch (Exception e) {
                            System.out
                                    .println("Update error : " + node.getIdentifier() + " : " + e.getMessage());
                        }
                    }
                }
            }
        }
        return OK("status", "OK");
    }

    @SuppressWarnings("unchecked")
    private List<Node> getAllWords(String languageId) {
        SearchCriteria sc = new SearchCriteria();
        sc.setNodeType(SystemNodeTypes.DATA_NODE.name());
        sc.setObjectType(objectType);
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

    private void getNodeMap(List<Node> nodes, Map<String, Node> nodeMap, List<String> words) {
        for (Node node : nodes) {
            Map<String, Object> metadata = node.getMetadata();
            if (null == metadata) {
                metadata = new HashMap<String, Object>();
                node.setMetadata(metadata);
            }
            String lemma = (String) metadata.get("lemma");
            if (StringUtils.isNotBlank(lemma)) {
                words.add(lemma);
                nodeMap.put(lemma, node);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void setCountsMetadata(Node node, Map<String, Object> citations, String groupName, String prefix) {
        Map<String, Object> counts = (Map<String, Object>) citations.get(groupName);
        if (null != counts && !counts.isEmpty()) {
            for (Entry<String, Object> countEntry : counts.entrySet()) {
                String key = "count_";
                if (StringUtils.isNotBlank(prefix))
                    key += (prefix.trim() + "_");
                Object value = countEntry.getValue();
                if (null != value) {
                    key += countEntry.getKey().trim().replaceAll("\\s+", "_");
                    node.getMetadata().put(key, value);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void addTags(Node node, Map<String, Object> citations, String groupName) {
        Map<String, Object> sources = (Map<String, Object>) citations.get(groupName);
        if (null != sources && !sources.isEmpty()) {
            List<String> tags = node.getTags();
            if (null == tags)
                tags = new ArrayList<String>();
            for (String source : sources.keySet()) {
                if (!tags.contains(source.trim()))
                    tags.add(source.trim());
            }
            node.setTags(tags);
        }
    }

    private void updatePosList(Node node, Map<String, Object> citations) {
        updateListMetadata(node, citations, "pos", "pos");
    }
    
    private void updateSourceTypesList(Node node, Map<String, Object> citations) {
        updateListMetadata(node, citations, "sourceType", "sourceTypes");
    }

    private void updateSourcesList(Node node, Map<String, Object> citations) {
        updateListMetadata(node, citations, "source", "sources");
    }
    
    private void updateGradeList(Node node, Map<String, Object> citations) {
        updateListMetadata(node, citations, "grade", "grade");
    }

    @SuppressWarnings("unchecked")
    private void updateListMetadata(Node node, Map<String, Object> citations, String indexKey, String metadataKey) {
        Map<String, Object> posList = (Map<String, Object>) citations.get(indexKey);
        if (null != posList && !posList.isEmpty()) {
            String[] arr = (String[]) node.getMetadata().get(metadataKey);
            List<String> sources = new ArrayList<String>();
            if (null != arr && arr.length > 0) {
                for (String str : arr)
                    sources.add(str);
            }
            for (String key : posList.keySet()) {
                if (!sources.contains(key))
                    sources.add(key);
            }
            node.getMetadata().put(metadataKey, sources);
        }
    }

    @SuppressWarnings("unchecked")
    private void getIndexInfo(String languageId, Map<String, Object> indexesMap, List<String> words,
            List<String> groupList) {
        if (null != words && !words.isEmpty()) {
            int start = 0;
            int batch = 100;
            if (batch > words.size())
                batch = words.size();
            while (start < words.size()) {
                List<String> list = new ArrayList<String>();
                for (int i = start; i < batch; i++) {
                    list.add(words.get(i));
                }
                Request langReq = getLanguageRequest(languageId, LanguageActorNames.INDEXES_ACTOR.name(),
                        LanguageOperations.getIndexInfo.name());
                langReq.put(LanguageParams.words.name(), list);
                langReq.put(LanguageParams.groupBy.name(), groupList);
                Response langRes = getLanguageResponse(langReq, LOGGER);
                if (!checkError(langRes)) {
                    Map<String, Object> map = (Map<String, Object>) langRes.get(LanguageParams.index_info.name());
                    if (null != map && !map.isEmpty()) {
                        indexesMap.putAll(map);
                    }
                }
                start += 100;
                batch += 100;
                if (batch > words.size())
                    batch = words.size();
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void getWordInfo(String languageId, Map<String, Object> wordInfoMap, List<String> words) {
        if (null != words && !words.isEmpty()) {
            int start = 0;
            int batch = 100;
            if (batch > words.size())
                batch = words.size();
            while (start < words.size()) {
                List<String> list = new ArrayList<String>();
                for (int i = start; i < batch; i++) {
                    list.add(words.get(i));
                }
                Request langReq = getLanguageRequest(languageId, LanguageActorNames.INDEXES_ACTOR.name(),
                        LanguageOperations.rootWordInfo.name());
                langReq.put(LanguageParams.words.name(), list);
                Response langRes = getLanguageResponse(langReq, LOGGER);
                if (!checkError(langRes)) {
                    Map<String, Object> map = (Map<String, Object>) langRes.get(LanguageParams.root_word_info.name());
                    if (null != map && !map.isEmpty()) {
                        wordInfoMap.putAll(map);
                    }
                }
                start += 100;
                batch += 100;
                if (batch > words.size())
                    batch = words.size();
            }
        }
    }
}
