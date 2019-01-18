package org.ekstep.taxonomy.mgr.impl;

import org.ekstep.common.Slug;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ServerException;
import org.ekstep.common.mgr.BaseManager;
import org.ekstep.common.util.S3PropertyReader;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.engine.router.GraphEngineManagers;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.ekstep.learning.util.cloud.CloudStore;
import org.ekstep.taxonomy.mgr.IReferenceManager;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class ReferenceManagerImpl extends BaseManager implements IReferenceManager {

    
    
    private static final String CONTENT_FOLDER = "cloud_storage.content.folder";
    private static final String ARTEFACT_FOLDER = "cloud_storage.artefact.folder";

    private static final String V2_GRAPH_ID = "domain";
    
	@Override
	public Response uploadReferenceDocument(File uploadedFile, String referenceId) {
        if (null == uploadedFile) {
            throw new ClientException(TaxonomyErrorCodes.ERR_INVALID_UPLOAD_FILE.name(), "Upload file is blank.");
        }
        String[] urlArray = new String[] {};
        try {
        	String folder = S3PropertyReader.getProperty(CONTENT_FOLDER) + "/"
					+ Slug.makeSlug(referenceId, true) + "/" + S3PropertyReader.getProperty(ARTEFACT_FOLDER);
        		urlArray = CloudStore.uploadFile(folder, uploadedFile, true);
        } catch (Exception e) {
            throw new ServerException(TaxonomyErrorCodes.ERR_MEDIA_UPLOAD_FILE.name(),
                    "Error wihile uploading the File.", e);
        }
        String url = urlArray[1];
        
        Request getReferenceRequest = getRequest(V2_GRAPH_ID, GraphEngineManagers.SEARCH_MANAGER, "getDataNode");
        getReferenceRequest.put(GraphDACParams.node_id.name(), referenceId);
		Response res = getResponse(getReferenceRequest);
		
		if(checkError(res)){
			return res;
		}
		
		Node referenceNode = (Node) res.get(GraphDACParams.node.name());
		referenceNode.getMetadata().put(ContentAPIParams.downloadUrl.name(), url);
		
		Request createReq = getRequest(V2_GRAPH_ID, GraphEngineManagers.NODE_MANAGER, "updateDataNode");
		createReq.put(GraphDACParams.node.name(), referenceNode);
		createReq.put(GraphDACParams.node_id.name(), referenceId);
		Response createRes = getResponse(createReq);
		
		if(checkError(createRes)){
			return createRes;
		}
		
		Response response = OK(ContentAPIParams.url.name(), url);
        return response;
	}
}