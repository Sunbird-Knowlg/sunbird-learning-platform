package org.ekstep.dialcode.mgr.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.dialcode.enums.DialCodeEnum;
import org.ekstep.dialcode.mgr.IDialCodeManager;
import org.ekstep.dialcode.model.DialCode;
import org.ekstep.dialcode.model.DialCodesBatch;
import org.ekstep.dialcode.util.DialCodeStoreUtil;
import org.ekstep.dialcode.util.SeqRandomGenerator;
import org.ekstep.graph.cache.util.RedisStoreUtil;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

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
	 * 
	 */
	static {
		double maxIndex;
		try {
			maxIndex = DialCodeStoreUtil.getDialCodeIndex();
			setMaxIndex(maxIndex);
		} catch (Exception e) {

		}

	}

	/**
	 * Generate Dial Code
	 * 
	 * @param map
	 * @param channelId
	 *
	 */
	@Override
	public Response generateDialCode(Map<String, Object> map, String channelId) throws Exception {
		DialCodesBatch dialCodesBatch;
		Map<Double, String> dialCodeMap;

		String ERROR_CODE = "ERR_INVALID_DIALCODE_GENERATE_REQUEST";
		if (null == map)
			return ERROR(ERROR_CODE, "Invalid Request", ResponseCode.CLIENT_ERROR);
		if (StringUtils.isBlank((String) map.get(DialCodeEnum.publisher.name())))
			return ERROR(ERROR_CODE, "Publisher is manadatory", ResponseCode.CLIENT_ERROR);

		int count = getCount(map);

		if (StringUtils.isBlank((String) map.get(DialCodeEnum.batchCode.name())))
			map.put(DialCodeEnum.batchCode.name(), String.valueOf(System.currentTimeMillis()));

		Double startIndex = getMaxIndex();
		dialCodesBatch = SeqRandomGenerator.generate(++startIndex, count);
		dialCodeMap = dialCodesBatch.getDialCodes();
		setMaxIndex(dialCodesBatch.getMaxIndex());

		// Store DIAL Codes in Cassandra
		DialCodeStoreUtil.saveDialCode(channelId, (String) map.get(DialCodeEnum.publisher.name()),
				(String) map.get(DialCodeEnum.batchCode.name()), dialCodeMap);

		Response resp = new Response();
		resp.put("count", dialCodeMap.size());
		resp.put("dialcodes", dialCodeMap.values());
		return resp;

	}

	/**
	 * @param map
	 * @return
	 */
	private int getCount(Map<String, Object> map) {
		try {
			String countStr = String.valueOf(map.get(DialCodeEnum.count.name()));
			if (StringUtils.isNotBlank(countStr)) {
				int count = Integer.parseInt(countStr);
				int maxCount = Platform.config.getInt("dialcode.max_count");
				if (count > maxCount) {
					throw new ClientException("ERR_INVALID_DIALCODE_COUNT", "Count is exceeding max count");
				}
				return count;
			} else {
				throw new ClientException("ERR_INVALID_DIALCODE_COUNT", "count is empty.");
			}
		} catch (Exception e) {
			throw new ClientException("ERR_INVALID_DIALCODE_COUNT", "Invalid count.");
		}

	}

	private static void setMaxIndex(Double maxIndex) {
		RedisStoreUtil.saveNodeProperty("domain", "dialcode", "max_index", maxIndex.toString());
	}

	private Double getMaxIndex() throws Exception {
		String indexStr = RedisStoreUtil.getNodeProperty("domain", "dialcode", "max_index");
		if (StringUtils.isNotBlank(indexStr)) {
			return Double.parseDouble(indexStr);
		} else {
			double maxIndex = DialCodeStoreUtil.getDialCodeIndex();
			setMaxIndex(maxIndex);
			return maxIndex;
		}

	}

	@Override
	public Response readDialCode(String dialCodeId) throws Exception {
		if (StringUtils.isBlank(dialCodeId))
			return ERROR("ERR_INVALID_DIALCODE_INFO_REQUEST", "Invalid Request", ResponseCode.CLIENT_ERROR);

		Response resp = new Response();
		DialCode dialCode = DialCodeStoreUtil.readDialCode(dialCodeId);

		resp.put(DialCodeEnum.dialcode.name(), dialCode);
		return resp;
	}

	@Override
	public Response updateDialCode(String dialCodeId, String channelId, Map<String, Object> map) throws Exception {
		if (null == map)
			return ERROR("ERR_INVALID_DIALCODE_UPDATE_REQUEST", "Invalid Request", ResponseCode.CLIENT_ERROR);

		DialCode dialCode = DialCodeStoreUtil.readDialCode(dialCodeId);

		if (!channelId.equalsIgnoreCase(dialCode.getChannel()))
			return ERROR("ERR_DIALCODE_UPDATE_REQUEST", "Invalid Channel Id.", ResponseCode.CLIENT_ERROR);

		if (dialCode.getStatus().equalsIgnoreCase(DialCodeEnum.Live.name()))
			return ERROR("ERR_DIALCODE_UPDATE_REQUEST", "Dial Code with Live status can't be updated.",
					ResponseCode.CLIENT_ERROR);

		String publisher = (String) map.get(DialCodeEnum.publisher.name());
		if (StringUtils.isBlank(publisher)) {
			publisher = dialCode.getPublisher();
		}
		String metaData = new Gson().toJson(map.get(DialCodeEnum.metadata.name()));
		String dialCodeIdentifier = DialCodeStoreUtil.updateData(dialCodeId, publisher, metaData);

		if (StringUtils.isBlank(dialCodeIdentifier)) {
			return ERROR("ERR_DIALCODE_UPDATE_REQUEST", "Internal Server Error", ResponseCode.CLIENT_ERROR);
		}

		Response resp = new Response();
		resp.put("identifier", dialCodeIdentifier);
		return resp;
	}

	@Override
	public Response listDialCode(String channelId, Map<String, Object> map) throws Exception {
		if (null == map)
			return ERROR("ERR_INVALID_DIALCODE_SEARCH_REQUEST", "Invalid Search Request", ResponseCode.CLIENT_ERROR);

		List<DialCode> dialCodeList = DialCodeStoreUtil.getDialCodeList(channelId, map);

		Response resp = new Response();
		resp.put("count", dialCodeList.size());
		resp.put("dialcodes", dialCodeList);
		return resp;
	}

	@Override
	public Response publishDialCode(String dialCodeId, String channelId) throws Exception {
		Response resp = null;
		DialCode dialCode = DialCodeStoreUtil.readDialCode(dialCodeId);

		if (!channelId.equalsIgnoreCase(dialCode.getChannel()))
			return ERROR("ERR_DIALCODE_PUBLISH_REQUEST", "Invalid Channel Id.", ResponseCode.CLIENT_ERROR);

		String dialCodeIdentifier = DialCodeStoreUtil.updateData(dialCodeId);

		if (StringUtils.isBlank(dialCodeIdentifier)) {
			return ERROR("ERR_DIALCODE_PUBLISH_REQUEST", "Internal Server Error", ResponseCode.CLIENT_ERROR);
		}

		resp = new Response();
		resp.put("identifier", dialCodeIdentifier);
		return resp;
	}

}