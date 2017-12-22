package com.ilimi.dialcode.mgr.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import com.ilimi.common.Platform;
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
			return ERROR("ERR_INVALID_DIALCODE_GENERATE_REQUEST", "Batch Code is Manadatory", ResponseCode.CLIENT_ERROR);
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

}