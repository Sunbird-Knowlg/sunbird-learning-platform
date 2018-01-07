package org.ekstep.dialcode.mgr.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.common.dto.ResponseParams;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.common.mgr.BaseManager;
import org.ekstep.dialcode.common.DialCodeErrorCodes;
import org.ekstep.dialcode.common.DialCodeErrorMessage;
import org.ekstep.dialcode.enums.DialCodeEnum;
import org.ekstep.dialcode.mgr.IDialCodeManager;
import org.ekstep.dialcode.model.DialCode;
import org.ekstep.dialcode.util.DialCodeStoreUtil;
import org.ekstep.dialcode.util.SeqRandomGenerator;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

/**
 * The Class <code>DialCodeManagerImpl</code> is the implementation of
 * <code>IDialCodeManager</code> for all the operation including CRUD operation
 * and High Level Operations.
 * 
 * @author gauraw
 * 
 */
@Component
public class DialCodeManagerImpl extends BaseManager implements IDialCodeManager {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.dialcode.mgr.IDialCodeManager#generateDialCode(java.util.Map,
	 * java.lang.String)
	 */
	@Override
	public Response generateDialCode(Map<String, Object> map, String channelId) throws Exception {
		Map<Double, String> dialCodeMap;
		if (null == map)
			return ERROR(DialCodeErrorCodes.ERR_INVALID_DIALCODE_REQUEST,
					DialCodeErrorMessage.ERR_INVALID_DIALCODE_REQUEST, ResponseCode.CLIENT_ERROR);
		String publisher = (String) map.get(DialCodeEnum.publisher.name());
		if (StringUtils.isBlank(publisher))
			return ERROR(DialCodeErrorCodes.ERR_INVALID_PUBLISHER, DialCodeErrorMessage.ERR_INVALID_PUBLISHER,
					ResponseCode.CLIENT_ERROR);
		int count = getCount(map);
		String batchCode = (String) map.get(DialCodeEnum.batchCode.name());
		if (StringUtils.isBlank(batchCode)) {
			batchCode = generateBatchCode(publisher);
			map.put(DialCodeEnum.batchCode.name(), batchCode);
		}
		dialCodeMap = SeqRandomGenerator.generate(count);
		Set<Map.Entry<Double, String>> entrySet = dialCodeMap.entrySet();
		for (Entry entry : entrySet) {
			DialCodeStoreUtil.save(channelId, publisher, batchCode, (String) entry.getValue(), (Double) entry.getKey());
		}
		Response resp = getSuccessResponse();
		resp.put(DialCodeEnum.count.name(), dialCodeMap.size());
		resp.put(DialCodeEnum.batchcode.name(), batchCode);
		resp.put(DialCodeEnum.dialcodes.name(), dialCodeMap.values());
		return resp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.dialcode.mgr.IDialCodeManager#readDialCode(java.lang.String)
	 */
	@Override
	public Response readDialCode(String dialCodeId) throws Exception {
		if (StringUtils.isBlank(dialCodeId))
			return ERROR(DialCodeErrorCodes.ERR_INVALID_DIALCODE_REQUEST,
					DialCodeErrorMessage.ERR_INVALID_DIALCODE_REQUEST, ResponseCode.CLIENT_ERROR);
		DialCode dialCode = DialCodeStoreUtil.read(dialCodeId);
		Response resp = getSuccessResponse();
		resp.put(DialCodeEnum.dialcode.name(), dialCode);
		return resp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.dialcode.mgr.IDialCodeManager#updateDialCode(java.lang.String,
	 * java.lang.String, java.util.Map)
	 */
	@Override
	public Response updateDialCode(String dialCodeId, String channelId, Map<String, Object> map) throws Exception {
		if (null == map)
			return ERROR(DialCodeErrorCodes.ERR_INVALID_DIALCODE_REQUEST,
					DialCodeErrorMessage.ERR_INVALID_DIALCODE_REQUEST, ResponseCode.CLIENT_ERROR);
		DialCode dialCode = DialCodeStoreUtil.read(dialCodeId);
		if (!channelId.equalsIgnoreCase(dialCode.getChannel()))
			return ERROR(DialCodeErrorCodes.ERR_INVALID_CHANNEL_ID, DialCodeErrorMessage.ERR_INVALID_CHANNEL_ID,
					ResponseCode.CLIENT_ERROR);
		if (dialCode.getStatus().equalsIgnoreCase(DialCodeEnum.Live.name()))
			return ERROR(DialCodeErrorCodes.ERR_DIALCODE_UPDATE, DialCodeErrorMessage.ERR_DIALCODE_UPDATE,
					ResponseCode.CLIENT_ERROR);
		String metaData = new Gson().toJson(map.get(DialCodeEnum.metadata.name()));
		Map<String, Object> data = new HashMap<String, Object>();
		data.put(DialCodeEnum.metadata.name(), metaData);
		DialCodeStoreUtil.update(dialCodeId, data);
		Response resp = getSuccessResponse();
		resp.put(DialCodeEnum.identifier.name(), dialCode.getIdentifier());
		return resp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.dialcode.mgr.IDialCodeManager#listDialCode(java.lang.String,
	 * java.util.Map)
	 */
	@Override
	public Response listDialCode(String channelId, Map<String, Object> map) throws Exception {
		if (null == map)
			return ERROR(DialCodeErrorCodes.ERR_INVALID_SEARCH_REQUEST, DialCodeErrorMessage.ERR_INVALID_SEARCH_REQUEST,
					ResponseCode.CLIENT_ERROR);
		// Need to be removed and request should go to ES.
		List<DialCode> dialCodeList = DialCodeStoreUtil.list(channelId, map);

		Response resp = getSuccessResponse();
		resp.put(DialCodeEnum.count.name(), dialCodeList.size());
		resp.put(DialCodeEnum.dialcodes.name(), dialCodeList);
		return resp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ekstep.dialcode.mgr.IDialCodeManager#publishDialCode(java.lang.
	 * String, java.lang.String)
	 */
	@Override
	public Response publishDialCode(String dialCodeId, String channelId) throws Exception {
		Response resp = null;
		DialCode dialCode = DialCodeStoreUtil.read(dialCodeId);
		if (!channelId.equalsIgnoreCase(dialCode.getChannel()))
			return ERROR(DialCodeErrorCodes.ERR_INVALID_CHANNEL_ID, DialCodeErrorMessage.ERR_INVALID_CHANNEL_ID,
					ResponseCode.CLIENT_ERROR);
		Map<String, Object> data = new HashMap<String, Object>();
		data.put(DialCodeEnum.status.name(), DialCodeEnum.Live.name());
		data.put(DialCodeEnum.published_on.name(), LocalDateTime.now().toString());
		DialCodeStoreUtil.update(dialCodeId, data);
		resp = getSuccessResponse();
		resp.put(DialCodeEnum.identifier.name(), dialCode.getIdentifier());
		return resp;
	}

	/**
	 * @param publisher
	 * @return String
	 */
	private String generateBatchCode(String publisher) {
		DateFormat df = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
		String date = df.format(new Date());
		return publisher.concat(".").concat(date);
	}

	/**
	 * @param map
	 * @return Integer
	 * @throws ClientException
	 */
	private int getCount(Map<String, Object> map) throws ClientException {
		Integer count = 0;
		try {
			count = (Integer) map.get(DialCodeEnum.count.name());
		} catch (Exception e) {
			throw new ClientException(DialCodeErrorCodes.ERR_INVALID_COUNT, DialCodeErrorMessage.ERR_INVALID_COUNT);
		}
		Integer maxCount = Platform.config.getInt("dialcode.max_count");
		if (count != 0 && count <= maxCount) {
			return count;
		} else {
			throw new ClientException(DialCodeErrorCodes.ERR_COUNT_VALIDATION_FAILED,
					DialCodeErrorMessage.ERR_COUNT_VALIDATION_FAILED);
		}
	}

	/**
	 * @return Response
	 */
	private Response getSuccessResponse() {
		Response resp = new Response();
		ResponseParams respParam = new ResponseParams();
		respParam.setStatus("successful");
		resp.setParams(respParam);
		return resp;
	}

}