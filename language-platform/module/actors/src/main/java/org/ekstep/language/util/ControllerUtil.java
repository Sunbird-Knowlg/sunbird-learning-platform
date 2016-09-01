package org.ekstep.language.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
import org.ekstep.language.router.LanguageRequestRouterPool;
import org.ekstep.language.util.IWordnetConstants;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.enums.TaxonomyErrorCodes;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.router.RequestRouterPool;
import com.ilimi.graph.common.enums.GraphEngineParams;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.util.Neo4jGraphFactory;
import com.ilimi.graph.dac.util.Neo4jGraphUtil;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.enums.ImportType;
import com.ilimi.graph.importer.InputStreamValue;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import scala.concurrent.Await;
import scala.concurrent.Future;

// TODO: Auto-generated Javadoc
/**
 * The Class ControllerUtil.
 *
 * @author rayulu, amarnath and karthik
 */
@Component
public class ControllerUtil extends BaseLanguageManager implements IWordnetConstants {

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(ControllerUtil.class.getName());
	
	/** The task refresh time in millis. */
	private Long TASK_REFRESH_TIME_IN_MILLIS = 10000L;

	/* (non-Javadoc)
	 * @see org.ekstep.language.util.BaseLanguageManager#getLanguageRequest(java.lang.String, java.lang.String, java.lang.String)
	 */
	public Request getLanguageRequest(String graphId, String manager, String operation) {
		return super.getLanguageRequest(graphId, manager, operation);
    }
	
	/* (non-Javadoc)
	 * @see com.ilimi.common.mgr.BaseManager#getRequest(java.lang.String, java.lang.String, java.lang.String)
	 */
	public Request getRequest(String graphId, String manager, String operation) {
		Request request = new Request();
        return setContext(request, graphId, manager, operation);
    }
	
	/* (non-Javadoc)
	 * @see com.ilimi.common.mgr.BaseManager#setContext(com.ilimi.common.dto.Request, java.lang.String, java.lang.String, java.lang.String)
	 */
	protected Request setContext(Request request, String graphId, String manager, String operation) {
        request.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
        request.setManagerName(manager);
        request.setOperation(operation);
        return request;
    }

	/* (non-Javadoc)
	 * @see org.ekstep.language.util.BaseLanguageManager#getLanguageResponse(com.ilimi.common.dto.Request, org.apache.logging.log4j.Logger)
	 */
	public Response getLanguageResponse(Request request, Logger logger) {
        return super.getLanguageResponse(request, logger);
    }
	
	/* (non-Javadoc)
	 * @see com.ilimi.common.mgr.BaseManager#getResponse(com.ilimi.common.dto.Request, org.apache.logging.log4j.Logger)
	 */
	public Response getResponse(Request request, Logger logger) {
        ActorRef router = RequestRouterPool.getRequestRouter();
        try {
            Future<Object> future = Patterns.ask(router, request, RequestRouterPool.REQ_TIMEOUT);
            Object obj = Await.result(future, RequestRouterPool.WAIT_TIMEOUT.duration());
            if (obj instanceof Response) {
                return (Response) obj;
            } else {
                return ERROR(TaxonomyErrorCodes.SYSTEM_ERROR.name(), "System Error", ResponseCode.SERVER_ERROR);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), e.getMessage(), e);
        }
    }
	
	/**
	 * Import nodes from stream async.
	 *
	 * @param wordContent the word content
	 * @param languageId the language id
	 * @param taskId the task id
	 */
	public void importNodesFromStreamAsync(String wordContent, String languageId, String taskId) {
		InputStream in = new ByteArrayInputStream(wordContent.getBytes(StandardCharsets.UTF_8));
		Request request = getRequest(languageId, GraphEngineManagers.GRAPH_MANAGER, "importGraph");
		request.put(GraphEngineParams.format.name(), ImportType.CSV.name());
		request.put(GraphEngineParams.input_stream.name(), new InputStreamValue(in));
		if(taskId != null){
			request.put(GraphEngineParams.task_id.name(), taskId);
		}
		makeAsyncRequest(request, LOGGER);
	}
	
	/**
	 * Make language async request.
	 *
	 * @param request the request
	 * @param logger the logger
	 */
	public void makeLanguageAsyncRequest(Request request, Logger logger) {
        ActorRef router = LanguageRequestRouterPool.getRequestRouter();
        try {
            router.tell(request, router);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(), e.getMessage(), e);
        }
    }
	
	/**
	 * Import nodes from stream async.
	 *
	 * @param in the in
	 * @param languageId the language id
	 * @param taskId the task id
	 */
	public void importNodesFromStreamAsync(InputStream in, String languageId, String taskId) {
		Request request = getRequest(languageId, GraphEngineManagers.GRAPH_MANAGER, "importGraph");
		request.put(GraphEngineParams.format.name(), ImportType.CSV.name());
		request.put(GraphEngineParams.input_stream.name(), new InputStreamValue(in));
		if(taskId != null){
			request.put(GraphEngineParams.task_id.name(), taskId);
		}
		makeAsyncRequest(request, LOGGER);
	}
	
	/**
	 * Import nodes from stream async.
	 *
	 * @param in the in
	 * @param languageId the language id
	 */
	public void importNodesFromStreamAsync(InputStream in, String languageId) {
		importNodesFromStreamAsync(in, languageId, null);
	}
	
	/**
	 * Import nodes from stream.
	 *
	 * @param wordContent the word content
	 * @param languageId the language id
	 * @return the string
	 */
	public String importNodesFromStream(String wordContent, String languageId) {
		InputStream in = new ByteArrayInputStream(wordContent.getBytes(StandardCharsets.UTF_8));
		Request request = getRequest(languageId, GraphEngineManagers.GRAPH_MANAGER, "importGraph");
		request.put(GraphEngineParams.format.name(), ImportType.CSV.name());
		request.put(GraphEngineParams.input_stream.name(), new InputStreamValue(in));
		Response response;
		try{
			response = getResponse(request, LOGGER);
			String taskId = (String) response.get(GraphEngineParams.task_id.name());
			return taskId;
		}
		catch(Exception e){
			LOGGER.error("error in importNodesFromStream , msg" + e.getMessage(), e);
		}
		return null;
	}
	
	/**
	 * Creates the task node.
	 *
	 * @param languageId the language id
	 * @return the string
	 */
	public String createTaskNode( String languageId) {
		Request request = getRequest(languageId, GraphEngineManagers.GRAPH_MANAGER, "createTaskNode");
		Response response;
		try{
			response = getResponse(request, LOGGER);
			String taskId = (String) response.get(GraphEngineParams.task_id.name());
			return taskId;
		}
		catch(Exception e){
			LOGGER.error("error in createTaskNode" + e.getMessage(), e);
		}
		return null;
	}

	/**
	 * Replace associations.
	 *
	 * @param wordContent the word content
	 * @param replacedWordIdMap the replaced word id map
	 * @return the string
	 */
	public String replaceAssociations(String wordContent, Map<String, Object> replacedWordIdMap) {
		for (Map.Entry<String, Object> entry : replacedWordIdMap.entrySet()) {
			wordContent = wordContent.replaceAll(entry.getKey(), (String) entry.getValue());
		}
		return wordContent;
	}

	/**
	 * Gets the node map.
	 *
	 * @param nodes the nodes
	 * @param nodeMap the node map
	 * @param words the words
	 * @return the node map
	 */
	public void getNodeMap(List<Node> nodes, Map<String, Node> nodeMap, List<String> words) {
		for (Node node : nodes) {
			Map<String, Object> metadata = node.getMetadata();
			if (null == metadata) {
				metadata = new HashMap<String, Object>();
				node.setMetadata(metadata);
			}
			String lemma = (String) metadata.get(ATTRIB_LEMMA);
			if (StringUtils.isNotBlank(lemma)) {
				words.add(lemma);
				nodeMap.put(lemma, node);
			}
		}
	}

	/**
	 * Sets the counts metadata.
	 *
	 * @param node the node
	 * @param citations the citations
	 * @param groupName the group name
	 * @param prefix the prefix
	 */
	@SuppressWarnings("unchecked")
	public void setCountsMetadata(Node node, Map<String, Object> citations, String groupName, String prefix) {
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

	/**
	 * Adds the tags.
	 *
	 * @param node the node
	 * @param citations the citations
	 * @param groupName the group name
	 */
	@SuppressWarnings("unchecked")
	public void addTags(Node node, Map<String, Object> citations, String groupName) {
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

	/**
	 * Update pos list.
	 *
	 * @param node the node
	 * @param citations the citations
	 */
	public void updatePosList(Node node, Map<String, Object> citations) {
		updateListMetadata(node, citations, "pos", "posTags");
	}

	/**
	 * Update source types list.
	 *
	 * @param node the node
	 * @param citations the citations
	 */
	public void updateSourceTypesList(Node node, Map<String, Object> citations) {
		updateListMetadata(node, citations, "sourceType", "sourceTypes");
	}

	/**
	 * Update sources list.
	 *
	 * @param node the node
	 * @param citations the citations
	 */
	public void updateSourcesList(Node node, Map<String, Object> citations) {
		updateListMetadata(node, citations, "source", "sources");
	}

	/**
	 * Update grade list.
	 *
	 * @param node the node
	 * @param citations the citations
	 */
	public void updateGradeList(Node node, Map<String, Object> citations) {
		updateListMetadata(node, citations, "grade", "grade");
	}

	/**
	 * Update list metadata.
	 *
	 * @param node the node
	 * @param citations the citations
	 * @param indexKey the index key
	 * @param metadataKey the metadata key
	 */
	@SuppressWarnings("unchecked")
	public void updateListMetadata(Node node, Map<String, Object> citations, String indexKey, String metadataKey) {
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

	/**
	 * Update string metadata.
	 *
	 * @param node the node
	 * @param citations the citations
	 * @param indexKey the index key
	 * @param metadataKey the metadata key
	 */
	@SuppressWarnings("unchecked")
	public void updateStringMetadata(Node node, Map<String, Object> citations, String indexKey, String metadataKey) {
		String key = (String) citations.get(indexKey);
		if (StringUtils.isNotBlank(key)) {
			Object obj = node.getMetadata().get(metadataKey);
			String[] arr = null;
			List<String> sources = new ArrayList<String>();
			if (null != obj) {
				if (obj instanceof String[]) {
					arr = (String[]) obj;
				} else {
					sources = (List<String>) obj;
				}
			}
			if (null != arr && arr.length > 0) {
				for (String str : arr)
					sources.add(str);
			}
			if (!sources.contains(key))
				sources.add(key);
			node.getMetadata().put(metadataKey, sources);
		}
	}

	/**
	 * Gets the index info.
	 *
	 * @param languageId the language id
	 * @param indexesMap the indexes map
	 * @param words the words
	 * @param groupList the group list
	 * @return the index info
	 */
	@SuppressWarnings("unchecked")
	public void getIndexInfo(String languageId, Map<String, Object> indexesMap, List<String> words,
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

	/**
	 * Gets the word info.
	 *
	 * @param languageId the language id
	 * @param wordInfoMap the word info map
	 * @param words the words
	 * @return the word info
	 */
	@SuppressWarnings("unchecked")
    public void getWordInfo(String languageId, Map<String, Object> wordInfoMap, List<String> words) {
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

	/**
	 * Import words and synsets.
	 *
	 * @param wordContent the word content
	 * @param synsetContent the synset content
	 * @param languageId the language id
	 */
	public void importWordsAndSynsets(String wordContent, String synsetContent, String languageId) {
		InputStream wordsInputStream = new ByteArrayInputStream(wordContent.getBytes(StandardCharsets.UTF_8));
		InputStream synsetsInputStream = new ByteArrayInputStream(synsetContent.getBytes(StandardCharsets.UTF_8));
		Request request = getRequest(languageId, LanguageActorNames.IMPORT_ACTOR.name(), LanguageOperations.importWordsAndSynsets.name());
		request.put(LanguageParams.words_input_stream.name(), new InputStreamValue(wordsInputStream));
		request.put(LanguageParams.synset_input_stream.name(), new InputStreamValue(synsetsInputStream));
		makeAsyncRequest(request, LOGGER);
	}

	/**
	 * Task completed.
	 *
	 * @param taskId the task id
	 * @param graphId the graph id
	 * @return true, if successful
	 */
	public boolean taskCompleted(String taskId, String graphId) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
		Transaction tx = null;
		boolean taskStatus = false;
		try {
			Long startTime = System.currentTimeMillis();
			while (true) {
				Long timeDiff = System.currentTimeMillis() - startTime;
				if (timeDiff >= TASK_REFRESH_TIME_IN_MILLIS) {
					tx = graphDb.beginTx();
					startTime = System.currentTimeMillis();
					org.neo4j.graphdb.Node taskNode = Neo4jGraphUtil.getNodeByUniqueId(graphDb, taskId);
					String status = (String) taskNode.getProperty(GraphEngineParams.status.name());
					if (status.equalsIgnoreCase(GraphEngineParams.Completed.name())) {
						taskStatus = true;
					}
					tx.success();
					tx.close();
					if(taskStatus){
						return taskStatus;
					}
				}
			}
		} catch (Exception e) {
			if (null != tx) {
				tx.failure();
				tx.close();
			}
		}
		return taskStatus;
	}

	/**
	 * Import nodes from stream async.
	 *
	 * @param synsetContent the synset content
	 * @param languageId the language id
	 */
	public void importNodesFromStreamAsync(String synsetContent, String languageId) {
		importNodesFromStreamAsync(synsetContent, languageId, null);
	}
}
