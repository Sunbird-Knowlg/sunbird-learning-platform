package com.ilimi.taxonomy.mgr.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.common.util.AWSUploader;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.dto.ResponseParams.StatusType;
import com.ilimi.common.enums.UrlProperties;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.model.node.DefinitionDTO;
import com.ilimi.graph.model.node.MetadataDefinition;
import com.ilimi.taxonomy.mgr.IAwsUrlUpdateManager;


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
	private static Logger LOGGER = LogManager.getLogger(AwsUrlUpdateManagerImpl.class.getName());
	
	public Response updateNodesWithUrl(String objectType, String graphId, String apiId)
	{
		List<String> failedNodes = new ArrayList<String>();
		SearchCriteria sc = new SearchCriteria();
		sc.setObjectType(objectType);
		sc.setNodeType(SystemNodeTypes.DATA_NODE.name());
		Request req = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER, "searchNodes",
				GraphDACParams.search_criteria.name(), sc);
		Response findRes = getResponse(req, LOGGER);
		if (checkError(findRes))
			return null;
		else {
			List<Node> nodes = (List<Node>) findRes.get(GraphDACParams.node_list.name());
			if (null != nodes && nodes.size() > 0){
				for(Node node: nodes){
					boolean updateFlag = false;
					for(UrlProperties prop: UrlProperties.values()){
						if(null!=node.getMetadata().get(prop.toString())){
							Object propertyVal = node.getMetadata().get(prop.toString());
							if(propertyVal instanceof String)
							{
								String property = (String)propertyVal;
								if(property.contains(oldPublicBucketName) ||
										property.contains(oldConfigBucketName)){
									String updatedUrl = AWSUploader.updateURL(property, oldPublicBucketName, oldConfigBucketName);
									if(!StringUtils.equalsIgnoreCase(property, updatedUrl)){
										node.getMetadata().put(prop.toString(), updatedUrl);
										updateFlag = true;
									}
								}
							} else if(propertyVal instanceof List){
								boolean ListUpdateFlag = false;
								List<String> propertyList = (List<String>)propertyVal;
								List<String> propertyUpdateList = new ArrayList<String>();
								for(String property: propertyList){
									if(property.contains(oldPublicBucketName) ||
											property.contains(oldConfigBucketName)){
										String updatedUrl = AWSUploader.updateURL(property, oldPublicBucketName, oldConfigBucketName);
										propertyUpdateList.add(updatedUrl);
										if(!StringUtils.equalsIgnoreCase(property, updatedUrl)){
											ListUpdateFlag = true;
										}
									}
								}
								if(propertyList.size() == propertyUpdateList.size() && ListUpdateFlag){
									node.getMetadata().put(prop.toString(), propertyUpdateList);
									updateFlag = true;
								}		

							}
						}
					}
					if(updateFlag){
						Request updateReq = getRequest(node.getGraphId(), GraphEngineManagers.NODE_MANAGER, "updateDataNode");
						updateReq.put(GraphDACParams.node.name(), node);
						updateReq.put(GraphDACParams.node_id.name(), node.getIdentifier());

						LOGGER.info("Updating the Node id with AWS url: " + node.getIdentifier());
						Response updateRes = getResponse(updateReq, LOGGER);
						if (checkError(updateRes)){
							failedNodes.add(node.getIdentifier());
						}
					}
				}

			}
		}
		Response response = new Response();
		ResponseParams resStatus = new ResponseParams();
		resStatus.setStatus(StatusType.successful.name());
		response.setParams(resStatus);
		response.setResponseCode(ResponseCode.OK);
		response.put("failed_nodes", failedNodes);
		return response;
	}
	
	
}