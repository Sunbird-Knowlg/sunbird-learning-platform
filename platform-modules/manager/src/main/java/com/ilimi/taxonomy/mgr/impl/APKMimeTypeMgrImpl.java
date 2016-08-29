package com.ilimi.taxonomy.mgr.impl;

import java.io.File;

import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Response;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.taxonomy.mgr.IMimeTypeManager;

/**
 * The Class APKMimeTypeMgrImpl.
 */
@Component
public class APKMimeTypeMgrImpl extends BaseMimeTypeManager implements IMimeTypeManager {

	/* (non-Javadoc)
	 * @see com.ilimi.taxonomy.mgr.IMimeTypeManager#upload(com.ilimi.graph.dac.model.Node, java.io.File, java.lang.String)
	 */
	@Override
	public Response upload(Node node, File uploadFile, String folder) {
		return uploadContent(node, uploadFile, folder);
	}

	/* (non-Javadoc)
	 * @see com.ilimi.taxonomy.mgr.IMimeTypeManager#extract(com.ilimi.graph.dac.model.Node)
	 */
	@Override
	public Response extract(Node node) {
		return new Response();

	}

	/* (non-Javadoc)
	 * @see com.ilimi.taxonomy.mgr.IMimeTypeManager#publish(com.ilimi.graph.dac.model.Node)
	 */
	@Override
	public Response publish(Node node) {
		return rePublish(node);
	}

	/* (non-Javadoc)
	 * @see com.ilimi.taxonomy.mgr.IMimeTypeManager#tuneInputForBundling(com.ilimi.graph.dac.model.Node)
	 */
	@Override
	public Node tuneInputForBundling(Node node) {
		// TODO Auto-generated method stub
		return node;
	}

}
