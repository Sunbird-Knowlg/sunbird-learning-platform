package org.ekstep.language.mgr.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.language.mgr.IDictionaryManager;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.mgr.BaseManager;

@Component
public class WordListManagerImpl extends BaseManager implements IDictionaryManager {
	
	private static Logger LOGGER = LogManager.getLogger(IDictionaryManager.class.getName());
    private static final String LEMMA_PROPERTY = "lemma";
    private static final List<String> DEFAULT_STATUS = new ArrayList<String>();
    static {
        DEFAULT_STATUS.add("Live");
    }
    
    private ObjectMapper mapper = new ObjectMapper();

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
	public Response deleteRelation(String languageId, String objectType, String objectId1, String relation,
			String objectId2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response list(String languageId, String objectType, Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response addRelation(String languageId, String objectType, String objectId1, String relation,
			String objectId2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response relatedObjects(String languageId, String objectType, String objectId, String relation,
			String[] fields, String[] relations) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response translation(String languageId, String[] words, String[] languages) {
		// TODO Auto-generated method stub
		return null;
	}
	
}