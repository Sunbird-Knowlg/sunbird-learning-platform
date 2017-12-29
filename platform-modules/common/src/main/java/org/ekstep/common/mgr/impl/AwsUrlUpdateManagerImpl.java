package org.ekstep.common.mgr.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.dto.ResponseParams;
import org.ekstep.common.dto.ResponseParams.StatusType;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.util.AWSUploader;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.dac.model.SearchCriteria;
import org.ekstep.graph.engine.router.GraphEngineManagers;
import org.ekstep.telemetry.logger.PlatformLogger;
import org.springframework.stereotype.Component;

import org.ekstep.common.enums.AwsUrlUpdateErrorCodes;
import org.ekstep.common.enums.CompositeSearchErrorCodes;
import org.ekstep.common.enums.UrlProperties;
import org.ekstep.common.mgr.BaseManager;
import org.ekstep.common.mgr.IAwsUrlUpdateManager;


/**
 * The Class <code>AwsUrlUpdateManagerImpl</code> is the implementation of
 * <code>IAwsUrlUpdateManager</code> for all the operation.
 * 
 * @author Jyotsna
 * 
 * @see IAwsUrlUpdateManager
 */
@Component
public class AwsUrlUpdateManagerImpl extends BaseManager implements IAwsUrlUpdateManager {

	/** Old/Existing public AWS Bucket Name. */
	private static final String oldPublicBucketName = "ekstep-public";

	/** Old/Existing config AWS Bucket Name. */
	private static final String oldConfigBucketName = "ekstep-config";

	protected static final String URL_String = "url";

	/** The logger. */
	

	@SuppressWarnings("unchecked")
	public Response updateNodesWithUrl(String objectType, String graphId, String apiId)
	{
		if (StringUtils.isBlank(graphId))
			throw new ClientException(AwsUrlUpdateErrorCodes.ERR_AWS_URL_UPDATE_BLANK_GRAPH_ID.name(),
					"Graph Id is blank.");
		if (StringUtils.isBlank(objectType))
			throw new ClientException(AwsUrlUpdateErrorCodes.ERR_AWS_URL_UPDATE_BLANK_OBJECT_TYPE.name(),
					"Object type is blank.");
		List<String> failedNodes = new ArrayList<String>();
		SearchCriteria sc = new SearchCriteria();
		sc.setObjectType(objectType);
		Request req = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
				GraphDACParams.search_criteria.name(), sc);
		Response findRes = getResponse(req);
		if (checkError(findRes))
			return null;
		else {
			List<Node> nodes = (List<Node>) findRes.get(GraphDACParams.node_list.name());
			if (null != nodes && nodes.size() > 0){
				for(Node node: nodes){
					failedNodes = updateNodes(node, failedNodes);
				}
			}
		}
		return getResponse(failedNodes);
	}
	
	public Response updateNodesWithIdentifiers(String graphId, String[] identifiers, String apiId)
	{
		if (StringUtils.isBlank(graphId))
			throw new ClientException(AwsUrlUpdateErrorCodes.ERR_AWS_URL_UPDATE_BLANK_GRAPH_ID.name(),
					"Graph Id is blank.");
		if (identifiers==null || identifiers.length==0)
			throw new ClientException(AwsUrlUpdateErrorCodes.ERR_AWS_URL_UPDATE_BLANK_IDENTIFIER.name(),
					"Object type is blank.");
		List<String> failedNodes = new ArrayList<String>();
		if (null != identifiers && identifiers.length > 0) {
			for (String identifier : identifiers) {
				Node node = getNode(graphId, identifier);
				if (null != node) {
					failedNodes = updateNodes(node,failedNodes);
				}
			}
		}
		return getResponse(failedNodes);
	}
	
	private Response getResponse(List<String> failedNodes){
		Response response = new Response();
		ResponseParams resStatus = new ResponseParams();
		resStatus.setStatus(StatusType.successful.name());
		response.setParams(resStatus);
		response.setResponseCode(ResponseCode.OK);
		response.put("failed_nodes", failedNodes);
		return response;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<String> updateNodes(Node node, List<String> failedNodes){
		
		boolean updateFlag = false;
		for(UrlProperties prop: UrlProperties.values()){
			String propName = prop.toString();
			if(null!= node.getMetadata() && null!=node.getMetadata().get(propName)){
				Object propertyVal = node.getMetadata().get(propName);
				if(propertyVal instanceof String)
				{
					String updatedUrl = updateNodeUrlForString(propertyVal, node);
					if(updatedUrl!=null){
						node.getMetadata().put(propName, updatedUrl);
						updateFlag = true;
					}
				} else if(propertyVal instanceof Map){
					Map updatedMap = verifyMapForUrl(propertyVal, node, propName);
					node.getMetadata().put(propName, updatedMap);
					updateFlag = true;

				} else if(propertyVal instanceof List){
					List<String> propertyList = (List<String>)propertyVal;
					for(Object property: propertyList){
						if(property instanceof String)
						{
							String updatedUrl = updateNodeUrlForString(property, node);
							if(updatedUrl!=null){
								node.getMetadata().put(propName, updatedUrl);
								updateFlag = true;
							}

						} else if(property instanceof Map){
							Map updatedMap = verifyMapForUrl(propertyVal, node, propName);
							node.getMetadata().put(propName, updatedMap);
							updateFlag = true;
						}
					}	

				}
			}
		}
		if(updateFlag){
			Request updateReq = getRequest(node.getGraphId(), GraphEngineManagers.NODE_MANAGER, "updateDataNode");
			updateReq.put(GraphDACParams.node.name(), node);
			updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());

			PlatformLogger.log("Updating the Node id with AWS url: " + node.getIdentifier());
			Response updateRes = getResponse(updateReq);
			if (checkError(updateRes)){
				failedNodes.add(node.getIdentifier());
			}
		}
		return failedNodes;
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map verifyMapForUrl(Object propertyVal, Node node, String propName){
		Map propMap = (Map)propertyVal;
		Map finalMap = new HashMap();
		for(Object mapObject: propMap.entrySet())
		{
			Map.Entry entry = (Map.Entry)mapObject;
			Object mapValue = entry.getValue();
			String updatedUrl = updateNodeUrlForString(mapValue, node);
			if(updatedUrl!=null){
				finalMap.put(entry.getKey(), updatedUrl);
			} else{
				finalMap.put(entry.getKey(), mapValue);
			}
		}
		return finalMap;
	}

	private String updateNodeUrlForString(Object propertyVal, Node node)
	{
		String property = (String)propertyVal;
		if(property.contains(oldPublicBucketName) ||
				property.contains(oldConfigBucketName)){
			String updatedUrl = AWSUploader.updateURL(property, oldPublicBucketName, oldConfigBucketName);
			return updatedUrl;
		} else{
			return null;
		}
	}
	
	/**
	 * Gets the node from the graph.
	 *
	 * @param graphId
	 *            the graph id
	 * @param identifier
	 *            the identifier
	 * @return the node
	 */
	private Node getNode(String graphId, String identifier) {
		Request req = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "getDataNode",
				GraphDACParams.node_id.name(), identifier);
		Response listRes = getResponse(req);
		if (checkError(listRes))
			throw new ResourceNotFoundException(
					CompositeSearchErrorCodes.ERR_COMPOSITE_SEARCH_SYNC_OBJECT_NOT_FOUND.name(),
					"Object not found: " + identifier);
		else {
			Node node = (Node) listRes.get(GraphDACParams.node.name());
			return node;
		}
	}

}