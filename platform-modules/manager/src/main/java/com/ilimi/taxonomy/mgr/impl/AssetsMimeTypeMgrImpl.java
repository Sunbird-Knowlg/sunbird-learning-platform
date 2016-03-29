package com.ilimi.taxonomy.mgr.impl;

import java.io.File;

import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Response;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.taxonomy.enums.ContentAPIParams;
import com.ilimi.taxonomy.mgr.IMimeTypeManager;

@Component("AssetsMimeTypeMgrImpl")
public class AssetsMimeTypeMgrImpl extends BaseMimeTypeManager implements IMimeTypeManager {

	@Override
	public Response upload(Node node, File uploadFile, String folder) {
	    String[] urlArray = uploadToAWS(uploadFile, folder);
	    node.getMetadata().put("s3Key", urlArray[0]);
        node.getMetadata().put(ContentAPIParams.artifactUrl.name(), urlArray[1]);
        node.getMetadata().put(ContentAPIParams.downloadUrl.name(), urlArray[1]);
        return updateContentNode(node, urlArray[1]);
	}

	@Override
	public Response extract(Node node) {
		return new Response();

	}

	@Override
	public Response publish(Node node) {
	    node.getMetadata().put("status", "Live");
	    return updateContentNode(node, null);
	}

	@Override
	public Node tuneInputForBundling(Node node) {
		// TODO Auto-generated method stub
		return node;
	}

}
