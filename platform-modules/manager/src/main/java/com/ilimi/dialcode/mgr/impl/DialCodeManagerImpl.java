package com.ilimi.dialcode.mgr.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.ilimi.common.Platform;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.dialcode.enums.DialCodeEnum;
import com.ilimi.dialcode.mgr.IDialCodeManager;
import com.ilimi.dialcode.model.DialCode;
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

	/**
	 * Generate Dial Code
	 * 
	 * @param map
	 * @param channelId
	 *
	 */
	@Override
	public Response generateDialCode(Map<String, Object> map, String channelId) throws Exception {
		if (null == map)
			return ERROR("ERR_INVALID_DIALCODE_GENERATE_REQUEST", "Invalid Request", ResponseCode.CLIENT_ERROR);
		if(null==map.get(DialCodeEnum.publisher.name()) || StringUtils.isBlank((String)map.get(DialCodeEnum.publisher.name())))
			return ERROR("ERR_INVALID_DIALCODE_GENERATE_REQUEST", "Publisher is Manadatory", ResponseCode.CLIENT_ERROR);
		if(null==map.get(DialCodeEnum.batchCode.name()) || StringUtils.isBlank((String)map.get(DialCodeEnum.batchCode.name())))
			map.put(DialCodeEnum.batchCode.name(), String.valueOf(System.currentTimeMillis()));
			//return ERROR("ERR_INVALID_DIALCODE_GENERATE_REQUEST", "Batch Code is Manadatory", ResponseCode.CLIENT_ERROR);
		if(null==map.get(DialCodeEnum.count.name()))
			return ERROR("ERR_INVALID_DIALCODE_GENERATE_REQUEST", "Count is Manadatory.", ResponseCode.CLIENT_ERROR);
		Integer count=0;
		
		// Validation for Empty Count or Count having Character
		try{
			count=Integer.parseInt(String.valueOf(map.get(DialCodeEnum.count.name())));
		}catch(NumberFormatException e){
			return ERROR("ERR_INVALID_DIALCODE_GENERATE_REQUEST", "Count must be a positive Number.", ResponseCode.CLIENT_ERROR);
		}
		
		int maxCount=Integer.parseInt(Platform.config.getString("MAX_NO_OF_ALLOWED_DIALCODE_PER_REQUEST"));
		
		if(count>maxCount){
			return ERROR("ERR_INVALID_DIALCODE_GENERATE_REQUEST", "Maximum allowed value for Count is "+maxCount, ResponseCode.CLIENT_ERROR);
		}
		
		Map<Integer,String> dialCodeMap=new HashMap<Integer,String>();
		List<String> list;
		
		synchronized(this) {
			Integer startIndex=DialCodeStoreUtil.getDialCodeIndex();	// TODO: Need to fetch it from redis
			Set<String> dialCodes=SeqRandomGenerator.generate(++startIndex, count);
			
			for(String code:dialCodes){
				dialCodeMap.put(startIndex++, code);
			}
			
			list = new ArrayList<String>(dialCodeMap.values());
			
			// Store DIAL Codes in Cassandra
			DialCodeStoreUtil.saveDialCode(channelId, (String)map.get(DialCodeEnum.publisher.name()), 
					(String)map.get(DialCodeEnum.batchCode.name()), dialCodeMap);
		}
		
		Response resp = new Response();
		resp.put("count", dialCodeMap.size());
		resp.put("dialcodes", list);
		return resp;
		
	}

	@SuppressWarnings("unused")
	@Override
	public Response readDialCode(String dialCodeId) throws Exception {
		if (StringUtils.isBlank(dialCodeId))
			return ERROR("ERR_INVALID_DIALCODE_INFO_REQUEST", "Invalid Request", ResponseCode.CLIENT_ERROR);
		
		Response resp=new Response();
		DialCode dialCode=DialCodeStoreUtil.readDialCode(dialCodeId);
		
		resp.put(DialCodeEnum.dialcode.name(), dialCode);
		return resp;
	}

	@Override
	public Response updateDialCode(String dialCodeId, String channelId, Map<String, Object> map) throws Exception {
		if (null == map)
			return ERROR("ERR_INVALID_DIALCODE_UPDATE_REQUEST", "Invalid Request", ResponseCode.CLIENT_ERROR);
		
		DialCode dialCode=DialCodeStoreUtil.readDialCode(dialCodeId);
		
		if(!channelId.equalsIgnoreCase(dialCode.getChannel()))
			return ERROR("ERR_DIALCODE_UPDATE_REQUEST", "Invalid Channel Id.", ResponseCode.CLIENT_ERROR);
		
		if(dialCode.getStatus().equalsIgnoreCase(DialCodeEnum.Live.name()))
			return ERROR("ERR_DIALCODE_UPDATE_REQUEST", "Dial Code with Live status can't be updated.", ResponseCode.CLIENT_ERROR);
		
		String publisher=(String)map.get(DialCodeEnum.publisher.name());
		if(StringUtils.isBlank(publisher)){
			publisher=dialCode.getPublisher();
		}
		String metaData=new Gson().toJson(map.get(DialCodeEnum.metadata.name()));
		String dialCodeIdentifier=DialCodeStoreUtil.updateData(dialCodeId,publisher,metaData);
		
		if(StringUtils.isBlank(dialCodeIdentifier)){
			return ERROR("ERR_DIALCODE_UPDATE_REQUEST", "Internal Server Error", ResponseCode.CLIENT_ERROR);
		}
		
		Response resp = new Response();
		resp.put("identifier",dialCodeIdentifier);
		return resp;
	}

	@Override
	public Response listDialCode(Map<String, Object> map) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response publishDialCode(String dialCodeId, String channelId) throws Exception {
		Response resp=null;
		DialCode dialCode=DialCodeStoreUtil.readDialCode(dialCodeId);
		
		if(!channelId.equalsIgnoreCase(dialCode.getChannel()))
			return ERROR("ERR_DIALCODE_PUBLISH_REQUEST", "Invalid Channel Id.", ResponseCode.CLIENT_ERROR);
		
		String dialCodeIdentifier=DialCodeStoreUtil.updateData(dialCodeId);
		
		if(StringUtils.isBlank(dialCodeIdentifier)){
			return ERROR("ERR_DIALCODE_PUBLISH_REQUEST", "Internal Server Error", ResponseCode.CLIENT_ERROR);
		}
		
		resp = new Response();
		resp.put("identifier",dialCodeIdentifier);
		return resp;
	}

}