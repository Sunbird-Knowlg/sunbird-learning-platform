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
	private static final String oldBucketName = "ekstep-public";

	protected static final String URL_String = "url";

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(AwsUrlUpdateManagerImpl.class.getName());
	
	public Response updateNodesWithUrl(String objectType, String graphId, String apiId)
	{
		List<String> propNames = new ArrayList<String>();
		List<String> failed_Nodes = new ArrayList<String>();
		Request requestDefinition = getRequest(graphId, GraphEngineManagers.SEARCH_MANAGER,
				"getNodeDefinition", GraphDACParams.object_type.name(), objectType);
		Response responseDefiniton = getResponse(requestDefinition, LOGGER);
		if (!checkError(responseDefiniton)) {
			DefinitionDTO definition = (DefinitionDTO) responseDefiniton
					.get(GraphDACParams.definition_node.name());
			List<MetadataDefinition> list = definition.getProperties();
			for(MetadataDefinition def: list){
				String datatype = def.getDataType().toLowerCase();
				String propertyName = def.getPropertyName().toLowerCase();
				String description = def.getDescription().toLowerCase();
				String title = def.getTitle().toLowerCase();
				if(datatype.equalsIgnoreCase(URL_String) || 
						propertyName.contains(URL_String) || 
						description.contains(URL_String) || title.contains(URL_String)){
					propNames.add(def.getPropertyName());
				}
			}
			if(propNames.size()>0)
			{
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
							for(String prop: propNames){
								if(null!=node.getMetadata().get(prop)){
									String url = (String)node.getMetadata().get(prop);
									if(url.contains(oldBucketName)){
										String updatedUrl = AWSUploader.updateURL(url, oldBucketName);
										if(!StringUtils.equalsIgnoreCase(url, updatedUrl)){
											node.getMetadata().put(prop, updatedUrl);
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
									failed_Nodes.add(node.getIdentifier());
								}
							}
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
        response.put("failed_nodes", failed_Nodes);
		return response;
	}
	
	
}