package org.ekstep.language.mgr.impl;

import org.ekstep.language.mgr.ICompositeSearchManager;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Response;
import com.ilimi.common.mgr.CompositeIndexSyncManager;

@Component
public class LanguageDataSyncManagerImpl extends CompositeIndexSyncManager implements ICompositeSearchManager {

	@Override
	public Response sync(String graphId, String objectType, Integer start, Integer total) {
		return syncDefinition(graphId, objectType, start, total);
	}
	
	@Override
	public Response syncObject(String graphId, String identifier) {
		return syncNode(graphId, identifier);
	}

}
