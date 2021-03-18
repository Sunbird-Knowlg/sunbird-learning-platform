package org.sunbird.dac.impl;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.dac.BaseDataAccessService;
import org.sunbird.dac.TransformationHelper;
import org.sunbird.dac.dto.AuditHistoryRecord;
import org.sunbird.dac.enums.CommonDACParams;
import org.sunbird.dac.impl.entity.AuditHistoryEntity;
import org.sunbird.dac.impl.entity.dao.AuditHistoryEsDao;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.searchindex.dto.SearchDTO;
import org.sunbird.searchindex.util.CompositeSearchConstants;
import org.sunbird.telemetry.logger.TelemetryManager;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The Class AuditHistoryDataService provides implementations of the various
 * operations defined in the IAuditHistoryEsService It extends
 * BaseDataAccessService which is base class for DAC services.
 * 
 * @author Karthik, Rashmi
 * 
 * @see IAuditHistoryEsService
 */

@Component("auditHistoryEsService")
public class AuditHistoryEsService extends BaseDataAccessService implements IAuditHistoryEsService {

	/** The model mapper. */
	private ModelMapper modelMapper = null;

	/** The Object mapper */
	private ObjectMapper objectMapper = null;
	
	/** This is the init method for the AuditHistoryEsService */
	public AuditHistoryEsService() {
		modelMapper = new ModelMapper();
		TransformationHelper.createTypeMap(modelMapper, AuditHistoryRecord.class, AuditHistoryEntity.class);
		objectMapper = new ObjectMapper();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		objectMapper.setDateFormat(df);
	}

	@Autowired
	AuditHistoryEsDao dao = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sunbird.dac.impl.IAuditHistoryEsService
	 * #saveAuditHistoryLog(java.lang.String, java.lang.String, java.io.File,
	 * java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public Response saveAuditHistoryLog(Request request) {
	
		AuditHistoryRecord auditRecord = (AuditHistoryRecord) request.get(CommonDACParams.audit_history_record.name());
		
		TelemetryManager.log("getting audit record from request object:" , request.getRequest());
		AuditHistoryEntity entity = new AuditHistoryEntity();
		modelMapper.map(auditRecord, entity);
		Map<String, Object> entity_map = objectMapper.convertValue(entity, Map.class);
		try {
			TelemetryManager.log("sending entity object to audit history dao" , entity_map);
			dao.save(entity_map);
		} catch (IOException e) {
			TelemetryManager.error("exception while proceesing audit history entity map: "+ e.getMessage(), e);
		}
		return OK(CommonDACParams.audit_history_record_id.name(), entity.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sunbird.dac.impl.IAuditHistoryEsService
	 * #getAuditHistoryLog(java.lang.String, java.lang.String, java.io.File,
	 * java.lang.String)
	 */
	public Response getAuditHistoryLog(Request request, String versionId) {
		String start_date = (String) request.get(CommonDACParams.start_date.name());
		String end_date = (String) request.get(CommonDACParams.end_date.name());
		String graphId = (String) request.get(CommonDACParams.graph_id.name());
		Map<String, String> sortBy = new HashMap<String, String>();
		sortBy.put(GraphDACParams.createdOn.name(), "desc");
		sortBy.put("operation", "desc");
		SearchDTO search = new SearchDTO();
		search.setProperties(setSearchFilters(graphId, null, null, start_date, end_date));
		search.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		search.setFields(setSearchCriteria(versionId));
		search.setSortBy(sortBy);
		TelemetryManager.log("creating search criteria to fetch audit history from ES: " + search);
		List<Object> auditHistoryLogEntities = (List<Object>) dao.search(search);
		TelemetryManager.log("list of fields returned from search result: " + auditHistoryLogEntities);
		return OK(CommonDACParams.audit_history_record.name(), getResponseObject(auditHistoryLogEntities));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sunbird.dac.impl.IAuditHistoryEsService
	 * #getAuditHistoryLogByObjectType(java.lang.String, java.lang.String,
	 * java.io.File, java.lang.String)
	 */
	public Response getAuditHistoryLogByObjectType(Request request, String versionId) {
		String graphId = (String) request.get(CommonDACParams.graph_id.name());
		String objectType = (String) request.get(CommonDACParams.object_type.name());
		String start_date = (String) request.get(CommonDACParams.start_date.name());
		String end_date = (String) request.get(CommonDACParams.end_date.name());
		SearchDTO search = new SearchDTO();
		search.setFields(setSearchCriteria(versionId));
		search.setProperties(setSearchFilters(graphId, null, objectType, start_date, end_date));
		search.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		Map<String, String> sortBy = new HashMap<String, String>();
		sortBy.put(GraphDACParams.createdOn.name(), "desc");
		sortBy.put("operation", "desc");
		search.setSortBy(sortBy);
		TelemetryManager.log("setting search criteria to fetch audit records from ES: " + search);
		List<Object> auditHistoryLogEntities = (List<Object>) dao.search(search);
		TelemetryManager.log("list of fields returned from ES based on search query: " + auditHistoryLogEntities);
		return OK(CommonDACParams.audit_history_record.name(), getResponseObject(auditHistoryLogEntities));

	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sunbird.dac.impl.IAuditHistoryEsService
	 * #getAuditHistoryLogByObjectId(java.lang.String, java.lang.String,
	 * java.io.File, java.lang.String)
	 */
	public Response getAuditHistoryLogByObjectId(Request request, String versionId) {
		String graphId = (String) request.get(CommonDACParams.graph_id.name());
		String objectId = (String) request.get(CommonDACParams.object_id.name());
		String start_date = (String) request.get(CommonDACParams.start_date.name());
		String end_date = (String) request.get(CommonDACParams.end_date.name());
		SearchDTO search = new SearchDTO();
		search.setFields(setSearchCriteria(versionId));
		search.setProperties(setSearchFilters(graphId, objectId, null, start_date, end_date));
		search.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		Map<String, String> sortBy = new HashMap<String, String>();
		sortBy.put(GraphDACParams.createdOn.name(), "desc");
		sortBy.put("operation", "desc");
		search.setSortBy(sortBy);
		TelemetryManager.log("setting search criteria to fetch audit records from ES: " + search);
		List<Object> auditHistoryLogEntities = dao.search(search);
		TelemetryManager.log("list of fields returned from ES based on search query: " + auditHistoryLogEntities);
		return OK(CommonDACParams.audit_history_record.name(), getResponseObject(auditHistoryLogEntities));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sunbird.dac.impl.IAuditHistoryEsService
	 * #getAuditLogRecordByAuditId(java.lang.String, java.lang.String,
	 * java.io.File, java.lang.String)
	 */
	public Response getAuditLogRecordById(Request request) {
		String objectId = (String) request.get(CommonDACParams.object_id.name());
		String start_date = (String) request.get(CommonDACParams.time_stamp.name());
		SearchDTO search = new SearchDTO();
		search.setFields(setSearchCriteria(null, true));
		search.setProperties(setSearchFilters(null, objectId, null, start_date, null));
		search.setOperation(CompositeSearchConstants.SEARCH_OPERATION_AND);
		Map<String, String> sortBy = new HashMap<String, String>();
		sortBy.put(GraphDACParams.createdOn.name(), "desc");
		sortBy.put("operation", "desc");
		search.setSortBy(sortBy);
		TelemetryManager.log("setting search criteria to fetch audit records from ES: " + search);
		List<Object> auditHistoryLogEntities = dao.search(search);
		TelemetryManager.log("list of fields returned from ES based on search query: " + auditHistoryLogEntities);
		return OK(CommonDACParams.audit_history_record.name(), getResponseObject(auditHistoryLogEntities));
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sunbird.dac.impl.IAuditHistoryEsService
	 * #deletEeDate(java.lang.String, java.lang.String,
	 * java.io.File, java.lang.String)
	 */
	public Response deleteEsData(Request request) {
		TelemetryManager.log("getting request to delete audit history records from ES", request.getRequest());
		String timeStamp = (String) request.get(CommonDACParams.time_stamp.name());
		RangeQueryBuilder query = new RangeQueryBuilder("createdOn").lte(timeStamp);
		// String query = "{\"query\": {\"range\" : {\"createdOn\" : {\"lte\" :\""
		// +timeStamp+ "\"}}}}";
		try {
			TelemetryManager.log("Sending query to delete the data from ES: " + query);
			dao.delete(query);
		} catch (IOException e) {
			TelemetryManager.error("Exception occured: "+ e.getMessage(),e);
		}
		return OK();
	}

	/**
	 * This method is used to get the ResponseObject in required format
	 * 
	 * @param versionId
	 * 
	 * @param List
	 *            of AuditHistoryRecords The records
	 * @return ResponseObject which holds the actual result of the operation
	 */

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<Map<String, Object>> getResponseObject(List<Object> records) {
		List<Map<String, Object>> respObj = new ArrayList<Map<String, Object>>();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		for (Object record : records) {
			try {
				if (record != null) {
					resultMap = (Map) record;
					try {
						if (resultMap != null) {
							if (resultMap.containsKey("summary")) {
								String summaryData = (String) resultMap.get("summary");
								if (summaryData != null && summaryData instanceof String) {
									Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss a z").create();
									Map summary = gson.fromJson(summaryData.toString(), Map.class);
									resultMap.put("summary", summary);
								}
							}
							if (resultMap.containsKey("logRecord")) {
								String logData = (String) resultMap.get("logRecord");
								if (logData != null && logData instanceof String) {
									Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss a z").create();
									Map logRecord = gson.fromJson(logData.toString(), Map.class);
									resultMap.put("logRecord", logRecord);
								}
							}
							resultMap.put("createdOn", resultMap.get("createdOn"));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					respObj.add(resultMap);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		TelemetryManager.log("returning the setted response object: " + respObj);
		return respObj;
	}

	/**
	 * This method is used set the search criteria based on versionId to fetch
	 * AuditHistory from DB
	 * 
	 * @param versionId
	 *            The API versionId
	 */

	public List<String> setSearchCriteria(String versionId) {
		return setSearchCriteria(versionId, false);
	}

	/**
	 * This method is used set the search criteria based on versionId to fetch
	 * required AuditHistory fields from DB
	 * 
	 * @param versionId
	 *            The API versionId
	 * @param returnAllFields
	 *            The boolean value to retun fields
	 */

	public List<String> setSearchCriteria(String versionId, boolean returnAllFields) {
		List<String> fields = new ArrayList<String>();
		fields.add("audit_id");
		fields.add("label");
		fields.add("objectId");
		fields.add("objectType");
		fields.add("operation");
		fields.add("requestId");
		fields.add("userId");
		fields.add("graphId");
		fields.add("createdOn");
		if (returnAllFields) {
			fields.add("logRecord");
			fields.add("summary");
		} else {
			if (StringUtils.equalsIgnoreCase("1.0", versionId))
				fields.add("logRecord");
			else
				fields.add("summary");
		}
		TelemetryManager.log("returning the search criteria fields: " + fields.size());
		return fields;
	}

	/**
	 * This method is used set the search filters based on given parameters to
	 * fetch required AuditHistory fields from DB
	 * 
	 * @param graphId
	 *            The graphId
	 * @param objectId
	 *            The objectId
	 * @param objectType
	 *            The objectType
	 * @param objectId
	 *            The objectId
	 * @param start_date
	 *            The start_date
	 * @param end_date
	 *            The end_date
	 * @return SearchDTO The searchDTO
	 */
	@SuppressWarnings("rawtypes")
	public List<Map> setSearchFilters(String graphId, String objectId, String objectType, String start_date,
			String end_date) {
		List<Map> properties = new ArrayList<Map>();

		if (StringUtils.isNotBlank(graphId)) {
			Map<String, Object> property = new HashMap<String, Object>();
			property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
			property.put("propertyName", "graphId");
			property.put("values", Arrays.asList(graphId));
			properties.add(property);
		}
		if (StringUtils.isNotBlank(start_date)) {
			Map<String, Object> property = new HashMap<String, Object>();
			property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_RANGE);
			property.put("propertyName", GraphDACParams.createdOn.name());
			Map<String, Object> range_map = new HashMap<String, Object>();
			range_map.put(CompositeSearchConstants.SEARCH_OPERATION_RANGE_GTE, start_date);
			property.put("values", range_map);
			properties.add(property);
		}
		if (StringUtils.isNotBlank(end_date)) {
			Map<String, Object> property = new HashMap<String, Object>();
			property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_RANGE);
			property.put("propertyName", GraphDACParams.createdOn.name());
			Map<String, Object> range_map = new HashMap<String, Object>();
			range_map.put(CompositeSearchConstants.SEARCH_OPERATION_RANGE_LTE, end_date);
			property.put("values", range_map);
			properties.add(property);
		}
		if (StringUtils.isNotBlank(objectType)) {
			Map<String, Object> property = new HashMap<String, Object>();
			property = new HashMap<String, Object>();
			property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
			property.put("propertyName", "objectType");
			property.put("values", Arrays.asList(objectType));
			properties.add(property);
		}
		if (StringUtils.isNotBlank(objectId)) {
			Map<String, Object> property = new HashMap<String, Object>();
			property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_EQUAL);
			property.put("propertyName", "objectId");
			property.put("values", Arrays.asList(objectId));
			properties.add(property);
		}
		TelemetryManager.log("returning the search filters: " + properties.size());
		return properties;
	}
}