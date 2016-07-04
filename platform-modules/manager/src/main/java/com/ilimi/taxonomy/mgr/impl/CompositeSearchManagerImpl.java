package com.ilimi.taxonomy.mgr.impl;

import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Response;
import com.ilimi.common.mgr.CompositeIndexSyncManager;
import com.ilimi.taxonomy.mgr.ICompositeSearchManager;

@Component
public class CompositeSearchManagerImpl extends CompositeIndexSyncManager implements ICompositeSearchManager {

	@Override
	public Response sync(String graphId, String objectType, Integer start, Integer total) {
		return syncDefinition(graphId, objectType, start, total);
		}
	
	@Override
	public Response syncObject(String graphId, String identifier) {
		return syncObject(graphId, identifier);
    }
	
	}
