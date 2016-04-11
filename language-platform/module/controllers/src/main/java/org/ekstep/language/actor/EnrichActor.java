package org.ekstep.language.actor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.common.LanguageBaseActor;
import org.ekstep.language.common.enums.LanguageActorNames;
import org.ekstep.language.common.enums.LanguageErrorCodes;
import org.ekstep.language.common.enums.LanguageOperations;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.measures.entity.WordComplexity;
import org.ekstep.language.mgr.impl.ControllerUtil;
import org.ekstep.language.util.WordnetUtil;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ClientException;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.engine.router.GraphEngineManagers;

import akka.actor.ActorRef;

public class EnrichActor extends LanguageBaseActor {

	private static Logger LOGGER = LogManager.getLogger(EnrichActor.class.getName());
	private ControllerUtil controllerUtil = new ControllerUtil();
	private final int BATCH_SIZE = 10000;

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
				InputStream stream = (InputStream) request.get(LanguageParams.input_stream.name());
				String prevTaskId = (request.get(LanguageParams.prev_task_id.name()) == null) ? null
						: (String) request.get(LanguageParams.prev_task_id.name());
				if (prevTaskId != null) {
					if (controllerUtil.taskCompleted(prevTaskId, languageId)) {
						controllerUtil.importNodesFromStreamAsync(stream, languageId);
					}
				} else {
					controllerUtil.importNodesFromStreamAsync(stream, languageId);
				}
				OK(getSender());
			} else {
				LOGGER.info("Unsupported operation: " + operation);
				throw new ClientException(LanguageErrorCodes.ERR_INVALID_OPERATION.name(),
						"Unsupported operation: " + operation);
			}
		} catch (Exception e) {
		    System.out.println("Error: " + e.getMessage());
			handleException(e, getSender());
		}
	}

	private void enrichWords(List<String> node_ids, String languageId) {
		ArrayList<String> batch_node_ids = new ArrayList<String>();
		int count = 0;
		for (String nodeId : node_ids) {
			count++;
			batch_node_ids.add(nodeId);
			if (batch_node_ids.size() % BATCH_SIZE == 0 || (node_ids.size() % BATCH_SIZE == batch_node_ids.size() && (node_ids.size() - count) < BATCH_SIZE)) {
				long startTime = System.currentTimeMillis();
				List<Node> nodeList = getNodesList(batch_node_ids, languageId);
				updateLexileMeasures(languageId, nodeList);
				updateFrequencyCount(languageId, nodeList);
				updatePosList(languageId, nodeList);
				batch_node_ids = new ArrayList<String>();
				long diff = System.currentTimeMillis() - startTime;
				System.out.println("Time taken for enriching " + BATCH_SIZE + " words: " + diff/1000 + "s");
			}
		}
	}

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
		if(checkError(response)){
			throw new ClientException(LanguageErrorCodes.SYSTEM_ERROR.name(), response.getParams().getErrmsg());
		}
		List<Node> nodeList = (List<Node>) response.get("node_list");
		long diff = System.currentTimeMillis() - startTime;
		System.out.println("Time taken for getting " + BATCH_SIZE + " nodes: " + diff/1000 + "s");
		return nodeList;
	}

	@SuppressWarnings("unchecked")
	private void updateFrequencyCount(String languageId, List<Node> nodes) {
		if (null != nodes && !nodes.isEmpty()) {
			String[] groupBy = new String[] { "pos", "sourceType", "source", "grade" };
			List<String> words = new ArrayList<String>();
			Map<String, Node> nodeMap = new HashMap<String, Node>();
			controllerUtil.getNodeMap(nodes, nodeMap, words);
			if (null != words && !words.isEmpty()) {
				System.out.println("updateFrequencyCount | Total words: " + nodes.size());
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
							Request updateReq = controllerUtil.getRequest(languageId, GraphEngineManagers.NODE_MANAGER,
									"updateDataNode");
							updateReq.put(GraphDACParams.node.name(), node);
							updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());
							try {
								Response updateResponse = controllerUtil.getResponse(updateReq, LOGGER);
								if (checkError(updateResponse)) {
								    System.out.println(updateResponse.getParams().getErr());
								    System.out.println(updateResponse.getResult());
									throw new ClientException(LanguageErrorCodes.SYSTEM_ERROR.name(),
											updateResponse.getParams().getErrmsg());
								}
								//System.out.println("update complete for: " + node.getIdentifier());
							} catch (Exception e) {
								System.out.println("Update Frequency Counts error : " + node.getIdentifier() + " : " + e.getMessage());
							}
						}
					}
				}
			}
		}
	}
	
	private void updatePosList(String languageId, List<Node> nodes) {
	    if (null != nodes && !nodes.isEmpty()) {
            System.out.println("updatePosList | Total words: " + nodes.size());
            for (Node node : nodes) {
                WordnetUtil.updatePOS(node);
                Request updateReq = controllerUtil.getRequest(languageId, GraphEngineManagers.NODE_MANAGER,
                        "updateDataNode");
                updateReq.put(GraphDACParams.node.name(), node);
                updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());
                try {
                    Response updateResponse = controllerUtil.getResponse(updateReq, LOGGER);
                    if (checkError(updateResponse)) {
                        System.out.println(updateResponse.getParams().getErr());
                        System.out.println(updateResponse.getResult());
                        throw new ClientException(LanguageErrorCodes.SYSTEM_ERROR.name(),
                                updateResponse.getParams().getErrmsg());
                    }
                } catch (Exception e) {
                    System.out.println("Update error : " + node.getIdentifier() + " : " + e.getMessage());
                }
            }
	    }
	}

	@SuppressWarnings("unchecked")
	private void updateLexileMeasures(String languageId, List<Node> nodes) {
		if (null != nodes && !nodes.isEmpty()) {
			System.out.println("updateLexileMeasures | Total words: " + nodes.size());
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
							Request updateReq = controllerUtil.getRequest(languageId, GraphEngineManagers.NODE_MANAGER,
									"updateDataNode");
							updateReq.put(GraphDACParams.node.name(), node);
							updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());
							try {
								//System.out.println("Sending update req for : " + node.getIdentifier());
								Response updateResponse = controllerUtil.getResponse(updateReq, LOGGER);
								if (checkError(updateResponse)) {
								    System.out.println(updateResponse.getParams().getErr());
                                    System.out.println(updateResponse.getResult());
									throw new ClientException(LanguageErrorCodes.SYSTEM_ERROR.name(),
											updateResponse.getParams().getErrmsg());
								}
								//System.out.println("Update complete for : " + node.getIdentifier());
							} catch (Exception e) {
								System.out.println("Update error : " + node.getIdentifier() + " : " + e.getMessage());
							}
						}
					}
				}
			}
		}
	}

	@Override
	protected void invokeMethod(Request request, ActorRef parent) {
	}
}