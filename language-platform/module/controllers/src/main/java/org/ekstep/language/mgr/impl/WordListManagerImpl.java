package org.ekstep.language.mgr.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.mgr.IWordListManager;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.mgr.BaseManager;

@Component
public class WordListManagerImpl extends BaseManager implements IWordListManager {
	
	private static Logger LOGGER = LogManager.getLogger(IWordListManager.class.getName());

    @Override
    public Response create(String languageId, String objectType, Request request) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Response update(String languageId, String id, String objectType, Request request) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Response find(String languageId, String id, String[] fields) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Response findAll(String languageId, String objectType, String[] fields, Integer limit) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Response list(String languageId, String objectType, Request request) {
        // TODO Auto-generated method stub
        return null;
    }
    	
}