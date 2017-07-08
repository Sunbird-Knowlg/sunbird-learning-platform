package com.ilimi.taxonomy.mgr.impl;
import java.io.File;
import org.ekstep.common.slugs.Slug;
import org.ekstep.common.util.AWSUploader;
import org.ekstep.common.util.S3PropertyReader;
import org.ekstep.learning.common.enums.ContentAPIParams;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.enums.TaxonomyErrorCodes;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.common.util.PlatformLogger;;
import com.ilimi.common.util.PlatformLogManager;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.taxonomy.mgr.IReferenceManager;

@Component
public class ReferenceManagerImpl extends BaseManager implements IReferenceManager {

    private static ILogger LOGGER = PlatformLogManager.getLogger();
    
    private static final String s3Content = "s3.content.folder";
    private static final String s3Artifacts = "s3.artifact.folder";

    private static final String V2_GRAPH_ID = "domain";
    
	@Override
	public Response uploadReferenceDocument(File uploadedFile, String referenceId) {
        if (null == uploadedFile) {
            throw new ClientException(TaxonomyErrorCodes.ERR_INVALID_UPLOAD_FILE.name(), "Upload file is blank.");
        }
        String[] urlArray = new String[] {};
        try {
        	String folder = S3PropertyReader.getProperty(s3Content) + "/"
					+ Slug.makeSlug(referenceId, true) + "/" + S3PropertyReader.getProperty(s3Artifacts);
            urlArray = AWSUploader.uploadFile(folder, uploadedFile);
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
		referenceNode.getMetadata().put(ContentAPIParams.downloadUrl.name(), url);
		
		Request createReq = getRequest(V2_GRAPH_ID, GraphEngineManagers.NODE_MANAGER, "updateDataNode");
		createReq.put(GraphDACParams.node.name(), referenceNode);
		createReq.put(GraphDACParams.node_id.name(), referenceId);
		Response createRes = getResponse(createReq, LOGGER);
		
		if(checkError(createRes)){
			return createRes;
		}
		
		Response response = OK(ContentAPIParams.url.name(), url);
        return response;
	}
}