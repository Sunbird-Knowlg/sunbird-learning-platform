package com.ilimi.dialcode.mgr.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.dialcode.enums.DialCodeEnum;
import com.ilimi.dialcode.mgr.IDialCodeManager;
import com.ilimi.dialcode.util.DialCodeStoreUtil;
import com.ilimi.dialcode.util.SeqRandomGenerator;



/**
 * The Class <code>DialCodeManagerImpl</code> is the implementation of
 * <code>IDialCodeManager</code> for all the operation including CRUD operation
 * and High Level Operations.
 * 
 * 
 * @author gauraw
 *
 */
@Component
public class DialCodeManagerImpl extends DialCodeBaseManager implements IDialCodeManager {

	@Override
	public Response generateDialCode(Map<String, Object> map, String channelId) throws Exception {
		if (null == map)
			return ERROR("ERR_INVALID_DIALCODE_GENERATE_REQUEST", "Invalid Request", ResponseCode.CLIENT_ERROR);
		/*if(null==map.get(DialCodeEnum.publisher.name()) || StringUtils.isBlank((String)map.get(DialCodeEnum.publisher.name())))
			return ERROR("ERR_INVALID_DIALCODE_GENERATE_REQUEST", "Publisher is Manadatory", ResponseCode.CLIENT_ERROR);
		if(null==map.get(DialCodeEnum.batchCode.name()) || StringUtils.isBlank((String)map.get(DialCodeEnum.batchCode.name())))
			return ERROR("ERR_INVALID_DIALCODE_GENERATE_REQUEST", "Batch Code is Manadatory", ResponseCode.CLIENT_ERROR);
		if(null==map.get(DialCodeEnum.count.name()) || 0==(Integer)map.get(DialCodeEnum.count.name()) || StringUtils.isBlank((String)map.get(DialCodeEnum.count.name())))
			return ERROR("ERR_INVALID_DIALCODE_GENERATE_REQUEST", "Count Can not be zero", ResponseCode.CLIENT_ERROR);*/
		
		Map<Integer,String> dialCodeMap=new HashMap<Integer,String>();
		Integer startIndex=DialCodeStoreUtil.getDialCodeIndex();	// TODO: Need to fetch it from redis
		Integer count=(Integer)map.get(DialCodeEnum.count.name());
		Set<String> dialCodes=SeqRandomGenerator.generate(++startIndex, count);
		
		for(String code:dialCodes){
			dialCodeMap.put(startIndex++, code);
		}
		
		List<String> list = new ArrayList<String>(dialCodeMap.values());
		
		DialCodeStoreUtil.saveDialCode(channelId, (String)map.get(DialCodeEnum.publisher.name()), 
				(String)map.get(DialCodeEnum.batchCode.name()), dialCodeMap);
		
		Response resp = new Response();
		resp.put("count", dialCodeMap.size());
		resp.put("dialcodes", list);
		
		return resp;
		//return getStubbedResponse(); // Need to return actual response.
	}

	@Override
	public Response readDialCode(String dialCodeId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response updateDialCode(String dialCodeId, String channelId, Map<String, Object> map) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response listDialCode(Map<String, Object> map) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response publishDialCode(String dialCodeId, String channelId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	private Response getStubbedResponse(){
		Response res=new Response();
		res.setId("testRes-001");
		res.setResponseCode(ResponseCode.OK);
		return res;
	}
	
}