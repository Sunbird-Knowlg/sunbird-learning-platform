package org.ekstep.language.actor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.common.LanguageBaseActor;
import org.ekstep.language.common.enums.LanguageActorNames;
import org.ekstep.language.common.enums.LanguageErrorCodes;
import org.ekstep.language.common.enums.LanguageOperations;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.measures.entity.WordComplexity;
import org.ekstep.language.util.ControllerUtil;
import org.ekstep.language.util.IWordnetConstants;
import org.ekstep.language.util.WordUtil;
import org.ekstep.language.util.WordnetUtil;
import org.ekstep.language.wordchian.WordChainUtil;

import com.ilimi.common.dto.NodeDTO;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.mgr.ConvertGraphNode;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.model.node.DefinitionDTO;

import akka.actor.ActorRef;

/**
 * The Class EnrichActor is an AKKA actor that processes all requests to provide
 * operations on update posList, update lexileMeasures, update wordComplexity ,
 * update SyllablesList and enrich word
 *
 * @author rayulu, amarnath and karthik
 */
public class EnrichActor extends LanguageBaseActor implements IWordnetConstants{

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(EnrichActor.class.getName());

	/** The controller util. */
	private ControllerUtil controllerUtil = new ControllerUtil();

	/** The batch size. */
	private final int BATCH_SIZE = 10000;

	/** The word util. */
	private WordUtil wordUtil = new WordUtil();

	/** The word chain util. */
	private WordChainUtil wordChainUtil = new WordChainUtil();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ilimi.graph.common.mgr.BaseGraphManager#onReceive(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void onReceive(Object msg) throws Exception {
		LOGGER.info("Received Command: " + msg);
		Request request = (Request) msg;
		String languageId = (String) request.getContext().get(LanguageParams.language_id.name());
		String operation = request.getOperation();
		try {
			if (StringUtils.equalsIgnoreCase(LanguageOperations.updateLexileMeasures.name(), operation)) {
				List<Node> nodeList = (List<Node>) request.get(LanguageParams.node_list.name());
				updateLexileMeasures(languageId, nodeList);
				OK(getSender());
			} else if (StringUtils.equalsIgnoreCase(LanguageOperations.updateFrequencyCount.name(), operation)) {
				List<Node> nodeList = (List<Node>) request.get(LanguageParams.node_list.name());
				updateFrequencyCount(languageId, nodeList);
				OK(getSender());
			} else if (StringUtils.equalsIgnoreCase(LanguageOperations.updatePosList.name(), operation)) {
				List<Node> nodeList = (List<Node>) request.get(LanguageParams.node_list.name());
				updatePosList(languageId, nodeList);
				OK(getSender());
			} else if (StringUtils.equalsIgnoreCase(LanguageOperations.enrichWords.name(), operation)) {
				List<String> nodeIds = (List<String>) request.get(LanguageParams.node_ids.name());
				enrichWords(nodeIds, languageId);
				OK(getSender());
			} else if (StringUtils.equalsIgnoreCase(LanguageOperations.importDataAsync.name(), operation)) {
				try (InputStream stream = (InputStream) request.get(LanguageParams.input_stream.name())) {
					String prevTaskId = (request.get(LanguageParams.prev_task_id.name()) == null) ? null
							: (String) request.get(LanguageParams.prev_task_id.name());
					if (prevTaskId != null) {
						if (controllerUtil.taskCompleted(prevTaskId, languageId)) {
							controllerUtil.importNodesFromStreamAsync(stream, languageId);
						}
					} else {
						controllerUtil.importNodesFromStreamAsync(stream, languageId);
					}
				}
				OK(getSender());
			} else {
				LOGGER.info("Unsupported operation: " + operation);
				throw new ClientException(LanguageErrorCodes.ERR_INVALID_OPERATION.name(),
						"Unsupported operation: " + operation);
			}
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
			LOGGER.error("Error in enrich actor", e);
			handleException(e, getSender());
		}
	}

	/**
	 * Enrich words.
	 *
	 * @param node_ids
	 *            the node ids
	 * @param languageId
	 *            the language id
	 */
	private void enrichWords(List<String> node_ids, String languageId) {
		if (null != node_ids && !node_ids.isEmpty()) {
			Set<String> nodeIds = new HashSet<String>();
			nodeIds.addAll(node_ids);
			ArrayList<String> batch_node_ids = new ArrayList<String>();
			int count = 0;
			for (String nodeId : nodeIds) {
				count++;
				batch_node_ids.add(nodeId);
				if (batch_node_ids.size() % BATCH_SIZE == 0 || (nodeIds.size() % BATCH_SIZE == batch_node_ids.size()
						&& (nodeIds.size() - count) < BATCH_SIZE)) {
					long startTime = System.currentTimeMillis();
					List<Node> nodeList = getNodesList(batch_node_ids, languageId);
					for(Node word:nodeList) {
						
						DefinitionDTO definition = getDefinitionDTO(LanguageParams.Word.name(), languageId);
						Map<String, Object> wordMap = ConvertGraphNode.convertGraphNode(word, languageId, definition, null);
						
						if(wordMap.get(LanguageParams.synonyms.name())!=null){
							List<NodeDTO> synonyms =(List<NodeDTO>) wordMap.get(LanguageParams.synonyms.name());
							word.getMetadata().put(ATTRIB_SYNSET_COUNT, synonyms.size());
							
							Set<String> posSet = new HashSet<>();
							List<Relation> synsets = wordUtil.getSynonymRelations(word.getInRelations());
							for(Relation synsetRelation:synsets) {
								String pos = (String)synsetRelation.getStartNodeMetadata().get(ATTRIB_POS);
								if(pos!=null)
									posSet.add(pos);
							}
							
							if(posSet.size()>0){
								List<String> posList= new ArrayList<>(posSet);
								word.getMetadata().put(ATTRIB_POS, posList);
							}
						}
						String lemma = (String) wordMap.get(LanguageParams.lemma.name());
						if (StringUtils.isNotBlank(lemma) && lemma.trim().contains(" ")) {
							 	word.getMetadata().put(ATTRIB_IS_PHRASE, true);
						}
						
						String primaryMeaningId = (String) word.getMetadata().get(LanguageParams.primaryMeaningId.name());
						if(primaryMeaningId!=null){
							Node synset = getDataNode(languageId, primaryMeaningId, "Synset");
							if(wordUtil.getSynonymRelations(synset.getOutRelations())!=null)
								word.getMetadata().put(ATTRIB_HAS_SYNONYMS, true);
							else
								word.getMetadata().put(ATTRIB_HAS_SYNONYMS, null);

							if(wordUtil.getAntonymRelations(synset.getOutRelations())!=null)
								word.getMetadata().put(ATTRIB_HAS_ANTONYMS, true);
							else
								word.getMetadata().put(ATTRIB_HAS_ANTONYMS, null);

							if(synset.getMetadata().get(ATTRIB_CATEGORY)!=null)
								word.getMetadata().put(ATTRIB_CATEGORY, synset.getMetadata().get(ATTRIB_CATEGORY));
							if(synset.getMetadata().get(ATTRIB_EXAMPLE_SENTENCES)!=null)
								word.getMetadata().put(ATTRIB_EXAMPLE_SENTENCES, synset.getMetadata().get(ATTRIB_EXAMPLE_SENTENCES));
							if(synset.getMetadata().get(ATTRIB_PICTURES)!=null)
								word.getMetadata().put(ATTRIB_PICTURES, synset.getMetadata().get(ATTRIB_PICTURES));
							if(synset.getMetadata().get(ATTRIB_PICTURES)!=null)
								word.getMetadata().put(ATTRIB_PICTURES, synset.getMetadata().get(ATTRIB_PICTURES));
							if(synset.getMetadata().get(ATTRIB_GLOSS)!=null)
								word.getMetadata().put(ATTRIB_MEANING, synset.getMetadata().get(ATTRIB_GLOSS));
							List<String> tags = synset.getTags();
							if(tags!=null&&tags.size()>0)
								 word.setTags(tags);
							 
						}
					}
					
					if (languageId.equalsIgnoreCase("en")) {
						updateSyllablesList(nodeList);
					}
					updateLexileMeasures(languageId, nodeList);
					updateFrequencyCount(languageId, nodeList);
					updatePosList(languageId, nodeList);
					updateWordComplexity(languageId, nodeList);
					batch_node_ids = new ArrayList<String>();
					long diff = System.currentTimeMillis() - startTime;
					LOGGER.info("Time taken for enriching " + BATCH_SIZE + " words: " + diff / 1000 + "s");
				}
			}
		}
	}

	/**
	 * Gets the nodes list.
	 *
	 * @param node_ids
	 *            the node ids
	 * @param languageId
	 *            the language id
	 * @return the nodes list
	 */
	@SuppressWarnings("unchecked")
	private List<Node> getNodesList(ArrayList<String> node_ids, String languageId) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(LanguageParams.node_ids.name(), node_ids);
		Request getDataNodesRequest = new Request();
		getDataNodesRequest.setRequest(map);
		getDataNodesRequest.setManagerName(GraphEngineManagers.SEARCH_MANAGER);
		getDataNodesRequest.setOperation("getDataNodes");
		getDataNodesRequest.getContext().put(GraphHeaderParams.graph_id.name(), languageId);
		long startTime = System.currentTimeMillis();
		Response response = controllerUtil.getResponse(getDataNodesRequest, LOGGER);
		if (checkError(response)) {
			throw new ClientException(LanguageErrorCodes.SYSTEM_ERROR.name(), response.getParams().getErrmsg());
		}
		List<Node> nodeList = (List<Node>) response.get("node_list");
		long diff = System.currentTimeMillis() - startTime;
		LOGGER.info("Time taken for getting " + BATCH_SIZE + " nodes: " + diff / 1000 + "s");
		return nodeList;
	}

	public DefinitionDTO getDefinitionDTO(String definitionName, String graphId) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(GraphDACParams.object_type.name(), definitionName);
		Request requestDefinition = new Request();
		requestDefinition.setRequest(map);
		requestDefinition.setManagerName(GraphEngineManagers.SEARCH_MANAGER);
		requestDefinition.setOperation("getNodeDefinition");
		requestDefinition.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
		requestDefinition.put(GraphDACParams.object_type.name(), definitionName);
		requestDefinition.put(GraphDACParams.graph_id.name(), graphId);

		Response responseDefiniton = controllerUtil.getResponse(requestDefinition, LOGGER);
		if (checkError(responseDefiniton)) {
			throw new ServerException(LanguageErrorCodes.SYSTEM_ERROR.name(), getErrorMessage(responseDefiniton));
		} else {
			DefinitionDTO definition = (DefinitionDTO) responseDefiniton.get(GraphDACParams.definition_node.name());
			return definition;
		}
	}
	
	public Node getDataNode(String languageId, String nodeId, String objectType) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(LanguageParams.node_id.name(), nodeId);
		Request getNodeReq = new Request();
		getNodeReq.setRequest(map);
		getNodeReq.setManagerName(GraphEngineManagers.SEARCH_MANAGER);
		getNodeReq.setOperation("getDataNode");
		getNodeReq.getContext().put(GraphHeaderParams.graph_id.name(), languageId);
		getNodeReq.put(GraphDACParams.node_id.name(), nodeId);
		getNodeReq.put(GraphDACParams.graph_id.name(), languageId);
		getNodeReq.put(GraphDACParams.objectType.name(), objectType);
		Response getNodeRes = controllerUtil.getResponse(getNodeReq, LOGGER);
		if (checkError(getNodeRes)) {
			throw new ServerException(LanguageErrorCodes.SYSTEM_ERROR.name(), getNodeRes.getParams().getErrmsg());
		}
		return (Node) getNodeRes.get(GraphDACParams.node.name());
	}
	/**
	 * Update frequency count.
	 *
	 * @param languageId
	 *            the language id
	 * @param nodes
	 *            the nodes
	 */
	@SuppressWarnings("unchecked")
	private void updateFrequencyCount(String languageId, List<Node> nodes) {
		if (null != nodes && !nodes.isEmpty()) {
			String[] groupBy = new String[] { "pos", "sourceType", "source", "grade" };
			List<String> words = new ArrayList<String>();
			Map<String, Node> nodeMap = new HashMap<String, Node>();
			controllerUtil.getNodeMap(nodes, nodeMap, words);
			if (null != words && !words.isEmpty()) {
				LOGGER.info("updateFrequencyCount | Total words: " + nodes.size());
				Map<String, Object> indexesMap = new HashMap<String, Object>();
				Map<String, Object> wordInfoMap = new HashMap<String, Object>();
				List<String> groupList = Arrays.asList(groupBy);
				controllerUtil.getIndexInfo(languageId, indexesMap, words, groupList);
				LOGGER.info("indexesMap size: " + indexesMap.size());
				controllerUtil.getWordInfo(languageId, wordInfoMap, words);
				LOGGER.info("wordInfoMap size: " + wordInfoMap.size());
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
							Request updateReq = controllerUtil.getRequest(languageId, GraphEngineManagers.NODE_MANAGER,
									"updateDataNode");
							updateReq.put(GraphDACParams.node.name(), node);
							updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());
							try {
								Response updateResponse = controllerUtil.getResponse(updateReq, LOGGER);
								if (checkError(updateResponse)) {
									throw new ClientException(LanguageErrorCodes.SYSTEM_ERROR.name(),
											updateResponse.getParams().getErrmsg());
								}
							} catch (Exception e) {
								LOGGER.error("Update Frequency Counts error : " + node.getIdentifier() + " : "
										+ e.getMessage(), e);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Update syllables list.
	 *
	 * @param nodes
	 *            the nodes
	 */
	private void updateSyllablesList(List<Node> nodes) {
		if (null != nodes && !nodes.isEmpty()) {
			LOGGER.info("updateSyllablesList | Total words: " + nodes.size());
			for (Node node : nodes) {
				try {
					WordnetUtil.updateSyllables(node);
					Request updateReq = controllerUtil.getRequest("en", GraphEngineManagers.NODE_MANAGER,
							"updateDataNode");
					updateReq.put(GraphDACParams.node.name(), node);
					updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());
					Response updateResponse = controllerUtil.getResponse(updateReq, LOGGER);
					if (checkError(updateResponse)) {
						throw new ClientException(LanguageErrorCodes.SYSTEM_ERROR.name(),
								updateResponse.getParams().getErrmsg());
					}
				} catch (Exception e) {
					LOGGER.error("Error updating syllable list for " + node.getIdentifier(), e);
				}
			}
		}
	}

	/**
	 * Update pos list.
	 *
	 * @param languageId
	 *            the language id
	 * @param nodes
	 *            the nodes
	 */
	private void updatePosList(String languageId, List<Node> nodes) {
		if (null != nodes && !nodes.isEmpty()) {
			LOGGER.info("updatePosList | Total words: " + nodes.size());
			for (Node node : nodes) {
				try {
					WordnetUtil.updatePOS(node);
					Request updateReq = controllerUtil.getRequest(languageId, GraphEngineManagers.NODE_MANAGER,
							"updateDataNode");
					updateReq.put(GraphDACParams.node.name(), node);
					updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());
					Response updateResponse = controllerUtil.getResponse(updateReq, LOGGER);
					if (checkError(updateResponse)) {
						throw new ClientException(LanguageErrorCodes.SYSTEM_ERROR.name(),
								updateResponse.getParams().getErrmsg());
					}
				} catch (Exception e) {
					LOGGER.error("Update error : " + node.getIdentifier() + " : " + e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * Update word complexity.
	 *
	 * @param languageId
	 *            the language id
	 * @param nodes
	 *            the nodes
	 */
	private void updateWordComplexity(String languageId, List<Node> nodes) {
		if (null != nodes && !nodes.isEmpty()) {
			for (Node node : nodes) {
				try {
					wordUtil.getWordComplexity(node, languageId);
				} catch (Exception e) {
					LOGGER.error("Error updating word complexity for " + node.getIdentifier(), e);
				}
			}
		}
	}

	/**
	 * Update lexile measures.
	 *
	 * @param languageId
	 *            the language id
	 * @param nodes
	 *            the nodes
	 */
	@SuppressWarnings("unchecked")
	private void updateLexileMeasures(String languageId, List<Node> nodes) {
		if (null != nodes && !nodes.isEmpty()) {
			LOGGER.info("updateLexileMeasures | Total words: " + nodes.size());
			List<String> words = new ArrayList<String>();
			Map<String, Node> nodeMap = new HashMap<String, Node>();
			controllerUtil.getNodeMap(nodes, nodeMap, words);
			Request langReq = controllerUtil.getLanguageRequest(languageId,
					LanguageActorNames.LEXILE_MEASURES_ACTOR.name(), LanguageOperations.getWordFeatures.name());
			langReq.put(LanguageParams.words.name(), words);
			Response langRes = controllerUtil.getLanguageResponse(langReq, LOGGER);
			if (checkError(langRes))
				throw new ClientException(LanguageErrorCodes.SYSTEM_ERROR.name(), langRes.getParams().getErrmsg());
			else {
				Map<String, WordComplexity> featureMap = (Map<String, WordComplexity>) langRes
						.get(LanguageParams.word_features.name());
				if (null != featureMap && !featureMap.isEmpty()) {
					LOGGER.info("Word features returned for " + featureMap.size() + " words");
					for (Entry<String, WordComplexity> entry : featureMap.entrySet()) {
						Node node = nodeMap.get(entry.getKey());
						WordComplexity wc = entry.getValue();
						if (null != node && null != wc) {
							node.getMetadata().put("syllableCount", wc.getCount());
							node.getMetadata().put("syllableNotation", wc.getNotation());
							node.getMetadata().put("unicodeNotation", wc.getUnicode());
							node.getMetadata().put("orthographic_complexity", wc.getOrthoComplexity());
							node.getMetadata().put("phonologic_complexity", wc.getPhonicComplexity());
							try {
								Request updateReq = controllerUtil.getRequest(languageId,
										GraphEngineManagers.NODE_MANAGER, "updateDataNode");
								updateReq.put(GraphDACParams.node.name(), node);
								updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());
								Response updateResponse = controllerUtil.getResponse(updateReq, LOGGER);
								if (checkError(updateResponse)) {
									throw new ClientException(LanguageErrorCodes.SYSTEM_ERROR.name(),
											updateResponse.getParams().getErrmsg());
								}
							} catch (Exception e) {
								LOGGER.error("Update error : " + node.getIdentifier() + " : " + e.getMessage(), e);
							}
						}
						try {
							wordChainUtil.updateWordSet(languageId, node, wc);
						} catch (Exception e) {
							LOGGER.error("Update error : " + node.getIdentifier() + " : " + e.getMessage(), e);
						}
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ilimi.graph.common.mgr.BaseGraphManager#invokeMethod(com.ilimi.common
	 * .dto.Request, akka.actor.ActorRef)
	 */
	@Override
	protected void invokeMethod(Request request, ActorRef parent) {
	}
}