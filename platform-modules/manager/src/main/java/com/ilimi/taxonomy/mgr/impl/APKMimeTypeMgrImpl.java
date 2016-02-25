package com.ilimi.taxonomy.mgr.impl;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Response;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.taxonomy.mgr.IMimeTypeManager;

@Component
@Qualifier("APKMimeTypeMgrImpl")
public class APKMimeTypeMgrImpl extends BaseMimeTypeManager implements IMimeTypeManager {

	@Override
	public void upload() {
		// TODO Auto-generated method stub

	}

	@Override
	public Response extract(Node node) {
		// TODO Auto-generated method stub
		return new Response();

	}

	@Override
	public void publish() {
		// TODO Auto-generated method stub

	}

	@Override
	public void bundle() {
		// TODO Auto-generated method stub

	}

}
