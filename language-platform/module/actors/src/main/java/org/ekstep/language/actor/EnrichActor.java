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

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.mgr.ConvertGraphNode;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.model.node.DefinitionDTO;

import akka.actor.ActorRef;

/**
 * The Class EnrichActor is an AKKA actor that processes all requests to provide
 * operations on update posList, update lexileMeasures, update wordComplexity ,
 * update SyllablesList and enrich word.
 *
 * @author rayulu, amarnath and karthik
 */
public class EnrichActor extends LanguageBaseActor implements IWordnetConstants {

	/** The logger. */
	private static PlatformLogger<EnrichActor> LOGGER = new PlatformLogger<>(EnrichActor.class.getName());

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
		LOGGER.log("Received Command: " + msg);
		Request request = (Request) msg;
		String languageId = (String) request.getContext().get(LanguageParams.language_id.name());
		String operation = request.getOperation();
		try {
			if (StringUtils.equalsIgnoreCase(LanguageOperations.updateFrequencyCount.name(), operation)) {
				List<Node> nodeList = (List<Node>) request.get(LanguageParams.node_list.name());
				updateFrequencyCount(languageId, nodeList);
				OK(getSender());
			} else if (StringUtils.equalsIgnoreCase(LanguageOperations.updatePosList.name(), operation)) {
				List<Node> nodeList = (List<Node>) request.get(LanguageParams.node_list.name());
				updatePosList(languageId, nodeList);
				OK(getSender());
			} else if (StringUtils.equalsIgnoreCase(LanguageOperations.enrichWord.name(), operation)) {
				String nodeId = (String) request.get(LanguageParams.word_id.name());
				Node word = getDataNode(languageId, nodeId, "Word");
				enrichWord( languageId, word);
				OK(getSender());
			} else if (StringUtils.equalsIgnoreCase(LanguageOperations.enrichWords.name(), operation)) {
				List<String> nodeIds = (List<String>) request.get(LanguageParams.node_ids.name());
				enrichWords(nodeIds, languageId);
				OK(getSender());
			} else if (StringUtils.equalsIgnoreCase(LanguageOperations.enrichAllWords.name(), operation)) {
				enrichAllWords(languageId);
				OK(getSender());
			}else if (StringUtils.equalsIgnoreCase(LanguageOperations.importDataAsync.name(), operation)) {
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
				LOGGER.log("Unsupported operation: " + operation);
				throw new ClientException(LanguageErrorCodes.ERR_INVALID_OPERATION.name(),
						"Unsupported operation: " + operation);
			}
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
			LOGGER.log("Error in enrich actor",e.getMessage(), e);
			handleException(e, getSender());
		}
	}

	/**
	 * Enrich all words.
	 *
	 * @param languageId
	 *            the language id
	 */
	private void enrichAllWords(String languageId) {
		int startPosition = 0;
		int resultSize = 1000;

		boolean enrich = true;
		while (enrich) {
			List<Node> nodes = wordUtil.getWords(languageId, startPosition, resultSize);
			if (nodes.size() > 0 && !nodes.isEmpty()) {
				enrichWords(languageId, nodes);
			} else {
				enrich = false;
			}
			startPosition += resultSize;
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

					List<Node> nodeList = getNodesList(batch_node_ids, languageId);
					enrichWords(languageId, nodeList);
					batch_node_ids = new ArrayList<String>();
				}
			}
		}
	}

	/**
	 * Enrich words.
	 *
	 * @param languageId
	 *            the language id
	 * @param nodeList
	 *            the node list
	 */
	private void enrichWords(String languageId, List<Node> nodeList) {
		long startTime = System.currentTimeMillis();

		updateWordMetadata(languageId, nodeList);
		long diff = System.currentTimeMillis() - startTime;
		LOGGER.log("Time taken for enriching " + nodeList.size() + " words: " + diff / 1000 + "s");
	}

	/**
	 * Enrich word.
	 *
	 * @param languageId
	 *            the language id
	 * @param node
	 *            the node 
	 */
	private void enrichWord(String languageId, Node word) {
		long startTime = System.currentTimeMillis();
		if (languageId.equalsIgnoreCase("en")) {
			updateSyllablesList(word);
		}
		updateLexileMeasures(languageId, word);
		updatePosList(languageId, word);
		updateWordComplexity(languageId, word);

		try{
			Request updateReq = controllerUtil.getRequest(languageId, GraphEngineManagers.NODE_MANAGER,
					"updateDataNode");
			updateReq.put(GraphDACParams.node.name(), word);
			updateReq.put(GraphDACParams.node_id.name(), word.getIdentifier());
			Response updateResponse = controllerUtil.getResponse(updateReq, LOGGER);
			if (checkError(updateResponse)) {
				throw new ClientException(LanguageErrorCodes.SYSTEM_ERROR.name(),
						updateResponse.getParams().getErrmsg());
			}
		} catch (Exception e) {
			LOGGER.log("Error updating syllable list for " , word.getIdentifier(), e);
		}
		
		long diff = System.currentTimeMillis() - startTime;
		LOGGER.log("Time taken for enriching a word , id - " +word.getIdentifier()+ " : " + diff / 1000 + "s");
	}
	/**
	 * Update word metadata.
	 *
	 * @param languageId
	 *            the language id
	 * @param nodes
	 *            the nodes
	 */
	private void updateWordMetadata(String languageId, List<Node> nodes) {

		for (Node word : nodes) {
			LOGGER.log("updateWordMetadata | Total words: " + nodes.size());

			DefinitionDTO definition = getDefinitionDTO(LanguageParams.Word.name(), languageId);
			Map<String, Object> wordMap = ConvertGraphNode.convertGraphNode(word, languageId, definition, null);
			List<Node> synsets = wordUtil.getSynsets(word);

			String primaryMeaningId = wordUtil.updatePrimaryMeaning(languageId, wordMap, synsets);
			word.getMetadata().put(LanguageParams.primaryMeaningId.name(), primaryMeaningId);

			if (synsets != null && synsets.size() > 0) {
				word.getMetadata().put(ATTRIB_SYNSET_COUNT, synsets.size());
			}

			String lemma = (String) wordMap.get(LanguageParams.lemma.name());
			if (StringUtils.isNotBlank(lemma) && lemma.trim().contains(" ")) {
				word.getMetadata().put(ATTRIB_IS_PHRASE, true);
			}

			if (primaryMeaningId != null) {
				Node synset = getDataNode(languageId, primaryMeaningId, "Synset");
				if (wordUtil.getSynonymRelations(synset.getOutRelations()) != null)
					word.getMetadata().put(ATTRIB_HAS_SYNONYMS, true);

				if (wordUtil.getAntonymRelations(synset.getOutRelations()) != null)
					word.getMetadata().put(ATTRIB_HAS_ANTONYMS, true);
				else
					word.getMetadata().put(ATTRIB_HAS_ANTONYMS, null);

				if (synset.getMetadata().get(ATTRIB_CATEGORY) != null)
					word.getMetadata().put(ATTRIB_CATEGORY, synset.getMetadata().get(ATTRIB_CATEGORY));
				if (synset.getMetadata().get(ATTRIB_PICTURES) != null)
					word.getMetadata().put(ATTRIB_PICTURES, synset.getMetadata().get(ATTRIB_PICTURES));
				if (synset.getMetadata().get(ATTRIB_GLOSS) != null)
					word.getMetadata().put(ATTRIB_MEANING, synset.getMetadata().get(ATTRIB_GLOSS));
				List<String> tags = synset.getTags();
				if (tags != null && tags.size() > 0)
					word.setTags(tags);

			} else {
				word.getMetadata().put(ATTRIB_HAS_SYNONYMS, null);
			}

			try {
				LOGGER.log("updating word metadata wordId: " + word.getIdentifier() + ", word metadata :"
						+ word.getMetadata().toString());
				Request updateReq = controllerUtil.getRequest(languageId, GraphEngineManagers.NODE_MANAGER,
						"updateDataNode");
				updateReq.put(GraphDACParams.node.name(), word);
				updateReq.put(GraphDACParams.node_id.name(), word.getIdentifier());
				Response updateResponse = controllerUtil.getResponse(updateReq, LOGGER);
				if (checkError(updateResponse)) {
					throw new ClientException(LanguageErrorCodes.SYSTEM_ERROR.name(),
							updateResponse.getParams().getErrmsg());
				}
			} catch (Exception e) {
				LOGGER.log("Update error : " + word.getIdentifier(), e.getMessage(), e);
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
		LOGGER.log("Time taken for getting " + BATCH_SIZE + " nodes: " + diff / 1000 + "s");
		return nodeList;
	}

	/**
	 * Gets the definition DTO.
	 *
	 * @param definitionName
	 *            the definition name
	 * @param graphId
	 *            the graph id
	 * @return the definition DTO
	 */
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

	/**
	 * Gets the data node.
	 *
	 * @param languageId
	 *            the language id
	 * @param nodeId
	 *            the node id
	 * @param objectType
	 *            the object type
	 * @return the data node
	 */
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
				LOGGER.log("updateFrequencyCount | Total words: " + nodes.size());
				Map<String, Object> indexesMap = new HashMap<String, Object>();
				Map<String, Object> wordInfoMap = new HashMap<String, Object>();
				List<String> groupList = Arrays.asList(groupBy);
				controllerUtil.getIndexInfo(languageId, indexesMap, words, groupList);
				LOGGER.log("indexesMap size: " + indexesMap.size());
				controllerUtil.getWordInfo(languageId, wordInfoMap, words);
				LOGGER.log("wordInfoMap size: " + wordInfoMap.size());
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
								LOGGER.log("Update Frequency Counts error : " + node.getIdentifier() + " : "
										, e.getMessage(), e);
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
	private void updateSyllablesList(Node node) {
		if (null != node) {
			LOGGER.log("updateSyllablesList | word identifier: " + node.getIdentifier());
			WordnetUtil.updateSyllables(node);
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
			LOGGER.log("updatePosList | Total words: " + nodes.size());
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
					LOGGER.log("Update error : " + node.getIdentifier(), e.getMessage(), e);
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
	private void updatePosList(String languageId, Node node) {
		if (null != node ) {
			LOGGER.log("updatePosList | word identifier: " + node.getIdentifier());
			WordnetUtil.updatePOS(node);
		}
	}
	
	/**
	 * Update word complexity.
	 *
	 * @param languageId
	 *            the language id
	 * @param node
	 *            the node
	 */
	private void updateWordComplexity(String languageId, Node node) {
		if (null != node) {
			try {
				wordUtil.computeWordComplexity(node, languageId);
			} catch (Exception e) {
				LOGGER.log("Error updating word complexity for " ,node.getIdentifier(), e);
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
	private void updateLexileMeasures(String languageId, Node node) {
		if (null != node) {
			LOGGER.log("updateLexileMeasures word identifier: " + node.getIdentifier());
			String lemma = (String) node.getMetadata().get(ATTRIB_LEMMA);
			if(StringUtils.isBlank(lemma))
				return;
			Request langReq = controllerUtil.getLanguageRequest(languageId,
					LanguageActorNames.LEXILE_MEASURES_ACTOR.name(), LanguageOperations.getWordFeatures.name());
			langReq.put(LanguageParams.word.name(), lemma);
			Response langRes = controllerUtil.getLanguageResponse(langReq, LOGGER);
			if (checkError(langRes)) {
				LOGGER.log("errror in updateLexileMeasures, languageId =" + languageId + ", error message"
						, langRes.getParams().getErrmsg(), "WARNlog");
				return;
			} else {
				Map<String, WordComplexity> featureMap = (Map<String, WordComplexity>) langRes
						.get(LanguageParams.word_features.name());
				if (null != featureMap && !featureMap.isEmpty()) {
					LOGGER.log("Word features returned for " + featureMap.size() + " word");
					WordComplexity wc = featureMap.get(lemma);
					if (null != node && null != wc) {
						node.getMetadata().put("syllableCount", wc.getCount());
						node.getMetadata().put("syllableNotation", wc.getNotation());
						node.getMetadata().put("unicodeNotation", wc.getUnicode());
						node.getMetadata().put("orthographic_complexity", wc.getOrthoComplexity());
						node.getMetadata().put("phonologic_complexity", wc.getPhonicComplexity());
					}
					try {
						wordChainUtil.updateWordSet(languageId, node, wc);
					} catch (Exception e) {
						LOGGER.log("Update error : " + node.getIdentifier() ,e.getMessage(), e);
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