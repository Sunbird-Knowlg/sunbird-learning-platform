package com.ilimi.taxonomy.mgr.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.ekstep.common.util.AWSUploader;
import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.util.PropertiesUtil;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.dto.ResponseParams.StatusType;
import com.ilimi.common.exception.ResourceNotFoundException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.common.mgr.CompositeIndexSyncManager;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.taxonomy.mgr.IAssetCleanUpManager;

import io.searchbox.core.SearchResult;

@Component
public class AssetCleanUpMgrImpl extends BaseManager implements IAssetCleanUpManager {

	private static String graphId;
	private static String objectType;
	private static final int SYNC_BATCH_SIZE = 5000;

	private static List<String> urlsOnS3;
	private static InputStream input;
	private static Properties prop = new Properties();
	private static ObjectMapper mapper = new ObjectMapper();
	private static ElasticSearchUtil elasticSearchUtil = new ElasticSearchUtil();
	private static Logger LOGGER = LogManager.getLogger(CompositeIndexSyncManager.class.getName());

	static {
		try {
			input = PropertiesUtil.class.getClassLoader().getResourceAsStream("file.properties");
			prop.load(input);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Fetches the assets from S3 and returns its URL
	 */
	public Response getObjectsOnS3(List<String> folders) {
		Response response = new Response();
		for (String folder : folders) {
			try {
				urlsOnS3 = AWSUploader.getObjectList(folder);
				FileUtils.writeLines(new File(prop.getProperty("s3outputFile")), urlsOnS3);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		ResponseParams params = new ResponseParams();
		params.setErr("0");
		params.setStatus(StatusType.successful.name());
		params.setErrmsg("Operation successful");
		response.setParams(params);
		return response;
	}

	/**
	 * Gets the nodes by graph Id and objectType and stores in elastic search
	 */
	@SuppressWarnings("unchecked")
	public Response synchGraphNodestoES(Map<String, Object> requestMap) {
		Response response;
		if (null != requestMap && !requestMap.isEmpty()) {
			graphId = (String) requestMap.get("graphId");
			objectType = (String) requestMap.get("objectType");
			response = OK();
			int start = 0;
			int batch = SYNC_BATCH_SIZE;
			Integer total = getNodesCount();
			if (null != total && total < batch)
				batch = total;
			int fetchCount = 0;
			boolean found = true;
			while (found) {
				List<Node> nodes;
				SearchCriteria sc = new SearchCriteria();
				sc.setObjectType(objectType);
				sc.setResultSize(batch);
				sc.setStartPosition(start);
				Request req = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
						GraphDACParams.search_criteria.name(), sc);
				Response res = getResponse(req, LOGGER);
				if (checkError(res)) {
					throw new ResourceNotFoundException("NODES_NOT_FOUND", "Nodes not found: " + graphId);
				}
				nodes = (List<Node>) res.get(GraphDACParams.node_list.name());
				fetchCount += batch;
				if (null != nodes && !nodes.isEmpty()) {
					updateES(nodes);
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
		} else {
			response = ERROR("Failed", "GraphId and objectType are invalid", ResponseCode.SERVER_ERROR);
		}
		return response;
	}

	private int getNodesCount() {
		SearchCriteria sc = new SearchCriteria();
		sc.setObjectType(objectType);
		Request req = new Request();
		req.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
		req.setManagerName(GraphEngineManagers.SEARCH_MANAGER);
		req.setOperation("getNodesCount");
		req.put(GraphDACParams.search_criteria.name(), sc);
		Response res = getResponse(req, LOGGER);
		if (checkError(res)) {
			throw new ResourceNotFoundException("NODES_NOT_FOUND", "Nodes not found: " + graphId);
		}
		int count = Integer.valueOf(res.get(GraphDACParams.count.name()).toString());
		return count;

	}

	private void updateES(List<Node> nodes) {
		for (Node node : nodes) {
			try {
				String s = mapper.writeValueAsString(node);
				Map<String, Object> m = mapper.readValue(s, new TypeReference<Map<String, Object>>() {
				});
				Map<String, Object> data = new HashMap<String, Object>();
				data.put("_source", m.get("metadata"));
				elasticSearchUtil.addDocumentWithId(graphId, objectType, m.get("identifier").toString(),
						mapper.writeValueAsString(data));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Searches ElasticSearch for the URLs in the file
	 * 
	 */
	@SuppressWarnings("rawtypes")
	public Response searchElasticSearch(Map<String, Object> requestMap) {
		String s;
		Response response;
		if (null != requestMap && !requestMap.isEmpty()) {
			graphId = (String) requestMap.get("graphId");
			objectType = (String) requestMap.get("objectType");
			response = OK();
			try {
				String fileName = prop.getProperty("s3outputFile");
				BufferedReader br = new BufferedReader(new FileReader(fileName));
				Set<String> unusedUrl = new HashSet<String>();
				Set<String> usedUrl = new HashSet<String>();
				while ((s = br.readLine()) != null) {
					String query = "{ \"query\" : { \"filtered\" : { \"query\" : { \"bool\" : { \"should\" : [ { \"match_phrase\" : { \"_all\" : \""
							+ s + "\"}}]}}}}}";
					SearchResult sr = elasticSearchUtil.search(graphId, objectType, query);
					List<Map> res = elasticSearchUtil.getDocumentsFromSearchResultWithId(sr);
					if (!res.isEmpty() && res.size() > 0) {
						usedUrl.add(s);
					} else {
						if (s.endsWith("/")) {
							continue;
						}
						unusedUrl.add(s);
					}
				}
				br.close();
				FileUtils.writeLines(new File(prop.getProperty("unusedURL")), unusedUrl);
				FileUtils.writeLines(new File(prop.getProperty("usedURL")), usedUrl);
				ResponseParams params = new ResponseParams();
				params.setErr("0");
				params.setStatus(StatusType.successful.name());
				params.setErrmsg("Operation successful");
				response.setParams(params);
			} catch (IOException e) {
				e.printStackTrace();
				response = ERROR("Failed", e.getMessage(), ResponseCode.SERVER_ERROR);
			} catch (Exception e) {
				e.printStackTrace();
				response = ERROR("Failed", e.getMessage(), ResponseCode.SERVER_ERROR);
			}
		}else {
			response = ERROR("Failed", "GraphId and objectType are invalid", ResponseCode.SERVER_ERROR);
		}
		return response;
	}

	/**
	 * Deletes unused files on S3
	 */
	public Response deleteUnusedFilesOnS3() {
		BufferedReader br = null;
		String content;
		Response response = OK();
		try {
			String fileName = prop.getProperty("unusedURL");
			br = new BufferedReader(new FileReader(fileName));
			while ((content = br.readLine()) != null) {
				AWSUploader.deleteFile(content);
			}
		} catch (Exception e) {
			e.printStackTrace();
			response = ERROR("Failed", e.getMessage(), ResponseCode.SERVER_ERROR);
		}
		return response;
	}

}
