package com.ilimi.taxonomy.mgr.impl;
import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.common.util.AWSUploader;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.enums.TaxonomyErrorCodes;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.taxonomy.content.enums.ContentWorkflowPipelineParams;
import com.ilimi.taxonomy.mgr.IReferenceManager;

@Component
public class ReferenceManagerImpl extends BaseManager implements IReferenceManager {

    private static Logger LOGGER = LogManager.getLogger(ReferenceManagerImpl.class.getName());

    private static final String V2_GRAPH_ID = "domain";
    
	@Override
	public Response uploadReferenceDocument(File uploadedFile, String referenceId) {
		String bucketName = "ekstep-public";
        String folder = "content";
        if (null == uploadedFile) {
            throw new ClientException(TaxonomyErrorCodes.ERR_INVALID_UPLOAD_FILE.name(), "Upload file is blank.");
        }
        String[] urlArray = new String[] {};
        try {
            urlArray = AWSUploader.uploadFile(bucketName, folder, uploadedFile);
        } catch (Exception e) {
            throw new ServerException(TaxonomyErrorCodes.ERR_MEDIA_UPLOAD_FILE.name(),
                    "Error wihile uploading the File.", e);
        }
        String url = urlArray[1];
        
        Request getReferenceRequest = getRequest(V2_GRAPH_ID, GraphEngineManagers.SEARCH_MANAGER, "getDataNode");
        getReferenceRequest.put(GraphDACParams.node_id.name(), referenceId);
		Response res = getResponse(getReferenceRequest, LOGGER);
		
		if(checkError(res)){
			return res;
		}
		
		Node referenceNode = (Node) res.get(GraphDACParams.node.name());
		referenceNode.getMetadata().put(ContentWorkflowPipelineParams.downloadUrl.name(), url);
		
		Request createReq = getRequest(V2_GRAPH_ID, GraphEngineManagers.NODE_MANAGER, "updateDataNode");
		createReq.put(GraphDACParams.node.name(), referenceNode);
		createReq.put(GraphDACParams.node_id.name(), referenceId);
		Response createRes = getResponse(createReq, LOGGER);
		
		if(checkError(createRes)){
			return createRes;
		}
		
		Response response = OK(ContentWorkflowPipelineParams.url.name(), url);
        return response;
	}
}