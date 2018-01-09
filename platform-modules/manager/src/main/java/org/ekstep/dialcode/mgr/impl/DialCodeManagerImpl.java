package org.ekstep.dialcode.mgr.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.ekstep.dialcode.model.Publisher;
import org.ekstep.dialcode.store.DialCodeStore;
import org.ekstep.dialcode.store.PublisherStore;
import org.ekstep.dialcode.util.DialCodeGenerator;
import org.ekstep.searchindex.dto.SearchDTO;
import org.ekstep.searchindex.processor.SearchProcessor;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.Row;
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

	@Autowired
	private PublisherStore publisherStore;

	@Autowired
	private DialCodeStore dialCodeStore;

	@Autowired
	private DialCodeGenerator dialCodeGenerator;

	private SearchProcessor processor = new SearchProcessor();

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
		validatePublisher(publisher);
		int count = getCount(map);
		String batchCode = (String) map.get(DialCodeEnum.batchCode.name());
		if (StringUtils.isBlank(batchCode)) {
			batchCode = generateBatchCode(publisher);
			map.put(DialCodeEnum.batchCode.name(), batchCode);
		}
		dialCodeMap = dialCodeGenerator.generate(count, channelId, publisher, batchCode);
		Response resp = getSuccessResponse();
		resp.put(DialCodeEnum.count.name(), dialCodeMap.size());
		resp.put(DialCodeEnum.batchcode.name(), batchCode);
		resp.put(DialCodeEnum.publisher.name(), publisher);
		resp.put(DialCodeEnum.dialcodes.name(), dialCodeMap.values());
		TelemetryManager.info("DIAL codes generated", resp.getResult());
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
		DialCode dialCode = dialCodeStore.read(dialCodeId);
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
		DialCode dialCode = dialCodeStore.read(dialCodeId);
		if (!channelId.equalsIgnoreCase(dialCode.getChannel()))
			return ERROR(DialCodeErrorCodes.ERR_INVALID_CHANNEL_ID, DialCodeErrorMessage.ERR_INVALID_CHANNEL_ID,
					ResponseCode.CLIENT_ERROR);
		if (dialCode.getStatus().equalsIgnoreCase(DialCodeEnum.Live.name()))
			return ERROR(DialCodeErrorCodes.ERR_DIALCODE_UPDATE, DialCodeErrorMessage.ERR_DIALCODE_UPDATE,
					ResponseCode.CLIENT_ERROR);
		String metaData = new Gson().toJson(map.get(DialCodeEnum.metadata.name()));
		Map<String, Object> data = new HashMap<String, Object>();
		data.put(DialCodeEnum.metadata.name(), metaData);
		dialCodeStore.update(dialCodeId, data);
		Response resp = getSuccessResponse();
		resp.put(DialCodeEnum.identifier.name(), dialCode.getIdentifier());
		TelemetryManager.info("DIAL code updated", resp.getResult());
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
		if (null == map.get(DialCodeEnum.publisher.name())) {
			return ERROR(DialCodeErrorCodes.ERR_INVALID_SEARCH_REQUEST, "Publisher is mandatory to list DailCodes",
					ResponseCode.CLIENT_ERROR);
		}
		// TODO: Need to be removed and request should go to ES.
		List<DialCode> dialCodeList = dialCodeStore.list(channelId, map);

		Response resp = getSuccessResponse();
		resp.put(DialCodeEnum.count.name(), dialCodeList.size());
		resp.put(DialCodeEnum.dialcodes.name(), dialCodeList);
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
	public Response searchDialCode(String channelId, Map<String, Object> map) throws Exception {
		if (null == map || map.isEmpty())
			return ERROR(DialCodeErrorCodes.ERR_INVALID_SEARCH_REQUEST, DialCodeErrorMessage.ERR_INVALID_SEARCH_REQUEST,
					ResponseCode.CLIENT_ERROR);
		List<Object> dialCodeList = searchDialCodes(channelId, map);

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
		DialCode dialCode = dialCodeStore.read(dialCodeId);
		if (!channelId.equalsIgnoreCase(dialCode.getChannel()))
			return ERROR(DialCodeErrorCodes.ERR_INVALID_CHANNEL_ID, DialCodeErrorMessage.ERR_INVALID_CHANNEL_ID,
					ResponseCode.CLIENT_ERROR);
		Map<String, Object> data = new HashMap<String, Object>();
		data.put(DialCodeEnum.status.name(), DialCodeEnum.Live.name());
		data.put(DialCodeEnum.published_on.name(), LocalDateTime.now().toString());
		dialCodeStore.update(dialCodeId, data);
		resp = getSuccessResponse();
		resp.put(DialCodeEnum.identifier.name(), dialCode.getIdentifier());
		TelemetryManager.info("DIAL code published", resp.getResult());
		return resp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.dialcode.mgr.IDialCodeManager#createPublisher(java.util.Map,
	 * java.lang.String)
	 */
	@Override
	public Response createPublisher(Map<String, Object> map, String channelId) throws Exception {
		String ERROR_CODE = "ERR_INVALID_PUBLISHER_CREATION_REQUEST";
		if (null == map)
			return ERROR(ERROR_CODE, "Invalid Request", ResponseCode.CLIENT_ERROR);

		if (!map.containsKey(DialCodeEnum.identifier.name())
				|| StringUtils.isBlank((String) map.get(DialCodeEnum.identifier.name()))) {
			return ERROR(ERROR_CODE, "Invalid Publisher Identifier", ResponseCode.CLIENT_ERROR);
		}

		if (!map.containsKey(DialCodeEnum.name.name())
				|| StringUtils.isBlank((String) map.get(DialCodeEnum.name.name()))) {
			return ERROR(ERROR_CODE, "Invalid Publisher Name", ResponseCode.CLIENT_ERROR);
		}
		String identifier = (String) map.get(DialCodeEnum.identifier.name());
		List<Row> listOfPublisher = publisherStore.get(DialCodeEnum.identifier.name(), identifier);

		if (!listOfPublisher.isEmpty())
			return ERROR(ERROR_CODE, "Publisher with identifier: " + identifier + " already exists.",
					ResponseCode.CLIENT_ERROR);

		Map<String, Object> publisherMap = getPublisherMap(map, channelId, true);
		publisherStore.create(identifier, publisherMap);

		Response response = new Response();
		response.put(DialCodeEnum.identifier.name(), identifier);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.dialcode.mgr.IDialCodeManager#readPublisher(java.lang.String)
	 */
	@Override
	public Response readPublisher(String publisherId) throws Exception {
		String ERROR_CODE = "ERR_INVALID_PUBLISHER_READ_REQUEST";
		if (StringUtils.isBlank(publisherId))
			return ERROR(ERROR_CODE, "Invalid Publisher Identifier", ResponseCode.CLIENT_ERROR);

		List<Row> listOfPublisher = publisherStore.get(DialCodeEnum.identifier.name(), publisherId);
		if (listOfPublisher.isEmpty())
			return ERROR(ERROR_CODE, "Publisher with Identifier: " + publisherId + " does not exists.",
					ResponseCode.CLIENT_ERROR);

		Row publisherRow = listOfPublisher.get(0);
		Publisher publisher = new Publisher(publisherRow.getString(DialCodeEnum.identifier.name()),
				publisherRow.getString(DialCodeEnum.name.name()), publisherRow.getString(DialCodeEnum.channel.name()),
				publisherRow.getString(DialCodeEnum.created_on.name()),
				publisherRow.getString(DialCodeEnum.updated_on.name()));

		Response response = new Response();
		response.put(DialCodeEnum.publisher.name(), publisher);
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ekstep.dialcode.mgr.IDialCodeManager#updatePublisher(java.lang.
	 * String, java.lang.String, java.util.Map)
	 */
	@Override
	public Response updatePublisher(String publisherId, String channelId, Map<String, Object> map) throws Exception {
		String ERROR_CODE = "ERR_INVALID_PUBLISHER_UPDATE_REQUEST";
		if (null == map)
			return ERROR(ERROR_CODE, "Invalid Request", ResponseCode.CLIENT_ERROR);

		List<Row> listOfPublisher = publisherStore.get(DialCodeEnum.identifier.name(), publisherId);

		if (listOfPublisher.isEmpty())
			return ERROR(ERROR_CODE, "Publisher with Identifier: " + publisherId + " does not exists.",
					ResponseCode.CLIENT_ERROR);

		Map<String, Object> publisherMap = getPublisherMap(map, channelId, false);
		publisherStore.modify(DialCodeEnum.identifier.name(), publisherId, publisherMap);

		Response response = new Response();
		response.put(DialCodeEnum.identifier.name(), publisherId);
		return response;
	}

	private Map<String, Object> getPublisherMap(Map<String, Object> map, String channel, boolean isCreateOperation) {
		Map<String, Object> publisherMap = new HashMap<>();
		if (isCreateOperation) {
			publisherMap.put(DialCodeEnum.identifier.name(), map.get(DialCodeEnum.identifier.name()));
			publisherMap.put(DialCodeEnum.channel.name(), channel);
			publisherMap.put(DialCodeEnum.created_on.name(), LocalDateTime.now().toString());
		}
		if (map.containsKey(DialCodeEnum.name.name())
				&& !StringUtils.isBlank((String) map.get(DialCodeEnum.name.name()))) {
			publisherMap.put(DialCodeEnum.name.name(), map.get(DialCodeEnum.name.name()));
		}
		publisherMap.put(DialCodeEnum.updated_on.name(), LocalDateTime.now().toString());

		return publisherMap;
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

	// TODO: Enhance it for Specific Server Error Message.
	// TODO: Enhance DialCodeStoreUtil and Use it instead of calling
	// CassandraStoreUtil directly.
	private void validatePublisher(String publisherId) throws Exception {
		if (StringUtils.isBlank(publisherId))
			throw new ClientException(DialCodeErrorCodes.ERR_INVALID_PUBLISHER,
					DialCodeErrorMessage.ERR_INVALID_PUBLISHER, ResponseCode.CLIENT_ERROR);
		String pubId = "";
		try {
			List<Row> list = publisherStore.get(DialCodeEnum.identifier.name(), publisherId);
			Row row = list.get(0);
			pubId = row.getString(DialCodeEnum.identifier.name());
		} catch (Exception e) {
			// TODO: Enhance it to Specific Error Code
		}
		if (!StringUtils.equals(publisherId, pubId))
			throw new ClientException(DialCodeErrorCodes.ERR_INVALID_PUBLISHER,
					DialCodeErrorMessage.ERR_INVALID_PUBLISHER, ResponseCode.CLIENT_ERROR);

	}

	/**
	 * @param channelId
	 * @param map
	 * @return
	 * @throws Exception
	 */
	private List<Object> searchDialCodes(String channelId, Map<String, Object> map) throws Exception {
		List<Object> searchResult = new ArrayList<Object>();
		SearchDTO searchDto = new SearchDTO();
		/*Map<String, String> sortBy = new HashMap<String, String>();
		sortBy.put("generated_on", "desc");
		sortBy.put("operation", "desc");*/
		searchDto.setProperties(setSearchProperties(channelId, map));
		// searchDto.setSortBy(sortBy);
		searchResult = (List<Object>) processor.processSearchQuery(searchDto, false,
				CompositeSearchConstants.DIAL_CODE_INDEX, false);

		return searchResult;
	}

	/**
	 * @param channelId
	 * @param map
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private List<Map> setSearchProperties(String channelId, Map<String, Object> map) {
		List<Map> properties = new ArrayList<Map>();

		for (String key : map.keySet()) {
			Map<String, Object> property = new HashMap<String, Object>();
			property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
			property.put("propertyName", key);
			property.put("values", map.get(key));
			properties.add(property);
		}

		Map<String, Object> property = new HashMap<String, Object>();
		property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		property.put("propertyName", DialCodeEnum.channel.name());
		property.put("values", channelId);
		properties.add(property);

		property = new HashMap<String, Object>();
		property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
		property.put("propertyName", DialCodeEnum.objectType.name());
		property.put("values", DialCodeEnum.DialCode.name());
		properties.add(property);

		return properties;
	}

}