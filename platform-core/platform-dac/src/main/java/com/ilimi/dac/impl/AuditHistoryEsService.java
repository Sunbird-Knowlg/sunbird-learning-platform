package com.ilimi.dac.impl;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.searchindex.dto.SearchDTO;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.dac.BaseDataAccessService;
import com.ilimi.dac.TransformationHelper;
import com.ilimi.dac.dto.AuditHistoryRecord;
import com.ilimi.dac.enums.CommonDACParams;
import com.ilimi.dac.impl.entity.AuditHistoryEntity;
import com.ilimi.dac.impl.entity.dao.AuditHistoryEsDao;


/**
 * The Class AuditHistoryDataService provides implementations of the various
 * operations defined in the IAuditHistoryDataService It extends
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
	 * @see com.ilimi.dac.impl.IAuditHistoryDataService
	 * #saveAuditHistoryLog(java.lang.String, java.lang.String, java.io.File,
	 * java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Transactional
	public Response saveAuditHistoryLog(Request request) {
		AuditHistoryRecord auditRecord = (AuditHistoryRecord) request.get(CommonDACParams.audit_history_record.name());
		AuditHistoryEntity entity = new AuditHistoryEntity();
		modelMapper.map(auditRecord, entity);
		Map<String,Object> entity_map = objectMapper.convertValue(entity,Map.class);
		try {
			dao.save(entity_map);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return OK(CommonDACParams.audit_history_record_id.name(), entity.getId());
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ilimi.dac.impl.IAuditHistoryDataService
	 * #getAuditHistoryLog(java.lang.String, java.lang.String, java.io.File,
	 * java.lang.String)
	 */
	@SuppressWarnings({ "rawtypes" })
	@Transactional
	public Response getAuditHistoryLog(Request request, String versionId) {

		String start_date = (String) request.get(CommonDACParams.start_date.name());
		String end_date = (String) request.get(CommonDACParams.end_date.name());
		List<Map> properties = new ArrayList<Map>();
		SearchDTO search = new SearchDTO();
		search = setSearchCriteria(versionId);
		if (start_date != null){
			Map<String, Object> property = new HashMap<String, Object>();
			property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_RANGE_GTE);
			property.put("propertyName", "*");
			property.put("values", Arrays.asList(start_date, LocalDateTime.now()));
			properties.add(property);
		}
		if (end_date != null){
			Map<String, Object> property = new HashMap<String, Object>();
			property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_RANGE_LTE);
			property.put("propertyName", "*");
			property.put("values", Arrays.asList(end_date, LocalDateTime.now()));
			properties.add(property);
		}
		search.setLimit(100);
		search.setOperation("AND");
		search.setProperties(properties);
		Map<String, Object> auditHistoryLogEntities = dao.search(search);
		return OK(CommonDACParams.audit_history_record.name(), getResponseObject(auditHistoryLogEntities));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ilimi.dac.impl.IAuditHistoryDataService
	 * #getAuditHistoryLogByObjectType(java.lang.String, java.lang.String,
	 * java.io.File, java.lang.String)
	 */
	@SuppressWarnings({"rawtypes" })
	@Transactional
	public Response getAuditHistoryLogByObjectType(Request request, String versionId) {

		String graphId = (String) request.get(CommonDACParams.graph_id.name());
		String objectType = (String) request.get(CommonDACParams.object_type.name());
		String start_date = (String) request.get(CommonDACParams.start_date.name());
		String end_date = (String) request.get(CommonDACParams.end_date.name());
		List<Map> properties = new ArrayList<Map>();
		
		SearchDTO search = new SearchDTO();
		search = setSearchCriteria(versionId);
		
		Map<String, Object> property = new HashMap<String, Object>();
		property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_LIKE);
		property.put("propertyName", "graphId");
		property.put("values", Arrays.asList(graphId));
		properties.add(property);
		
		property = new HashMap<String, Object>();
		property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_LIKE);
		property.put("propertyName", "objectType");
		property.put("values", Arrays.asList(objectType));
		properties.add(property);
		
		if (start_date != null){
			property = new HashMap<String, Object>();
			property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_RANGE_GTE);
			property.put("propertyName", "createdOn");
			property.put("values", Arrays.asList(start_date, LocalDateTime.now()));
			properties.add(property);
		}
		if (end_date != null){
			property = new HashMap<String, Object>();
			property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_RANGE_LTE);
			property.put("propertyName", "createdOn");
			property.put("values",Arrays.asList(end_date,LocalDateTime.now()));
			properties.add(property);
		}
		search.setLimit(100);
		search.setOperation("AND");
		search.setProperties(properties);
		Map<String, Object> auditHistoryLogEntities = dao.search(search);
		return OK(CommonDACParams.audit_history_record.name(), getResponseObject(auditHistoryLogEntities));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ilimi.dac.impl.IAuditHistoryDataService
	 * #getAuditHistoryLogByObjectId(java.lang.String, java.lang.String,
	 * java.io.File, java.lang.String)
	 */
	@SuppressWarnings({ "rawtypes" })
	@Transactional
	public Response getAuditHistoryLogByObjectId(Request request, String versionId) {
		String graphId = (String) request.get(CommonDACParams.graph_id.name());
		String objectId = (String) request.get(CommonDACParams.object_id.name());
		String start_date = (String) request.get(CommonDACParams.start_date.name());
		String end_date = (String) request.get(CommonDACParams.end_date.name());	
		List<Map> properties = new ArrayList<Map>();
		SearchDTO search = new SearchDTO();
		search = setSearchCriteria(versionId);
		

		Map<String, Object> property = new HashMap<String, Object>();
		property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_LIKE);
		property.put("propertyName", "graphId");
		property.put("values", Arrays.asList(graphId));
		properties.add(property);
		
		property = new HashMap<String, Object>();
		property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_LIKE);
		property.put("propertyName", "objectId");
		property.put("values", Arrays.asList(objectId));
		properties.add(property);
		
		if (start_date != null){
			property = new HashMap<String, Object>();
			property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_RANGE_GTE);
			property.put("propertyName", "createdOn");
			property.put("values",Arrays.asList(start_date,LocalDateTime.now()));
			properties.add(property);
		}
		if (end_date != null){
			property = new HashMap<String, Object>();
			property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_RANGE_LTE);
			property.put("propertyName", "createdOn");
			property.put("values", Arrays.asList(end_date,LocalDateTime.now()));
			properties.add(property);
		}
		search.setLimit(100);
		search.setOperation("AND");
		search.setProperties(properties);
		Map<String,Object> auditHistoryLogEntities = dao.search(search);
		return OK(CommonDACParams.audit_history_record.name(), getResponseObject(auditHistoryLogEntities));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ilimi.dac.impl.IAuditHistoryDataService
	 * #getAuditLogRecordByAuditId(java.lang.String, java.lang.String,
	 * java.io.File, java.lang.String)
	 */
	@SuppressWarnings({"rawtypes" })
	@Transactional
	public Response getAuditLogRecordById(Request request) {
		String objectId = (String) request.get(CommonDACParams.object_id.name());
		String time_stamp = (String) request.get(CommonDACParams.time_stamp.name());
		
		List<Map> properties = new ArrayList<Map>();
		SearchDTO search = new SearchDTO();
		search = setSearchCriteria(null, true);
		
		Map<String,Object> property = new HashMap<String, Object>();
		property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_LIKE);
		property.put("propertyName", "objectId");
		property.put("values", Arrays.asList(objectId));
		properties.add(property);
		
		if (time_stamp != null){
			property = new HashMap<String, Object>();
			property.put("operation", CompositeSearchConstants.SEARCH_OPERATION_LIKE);
			property.put("propertyName", "createdOn");
			property.put("values",Arrays.asList(time_stamp,LocalDateTime.now()));
			properties.add(property);
		}
		search.setLimit(100);
		search.setOperation("AND");
		search.setProperties(properties);
		Map<String, Object> auditHistoryLogEntities = dao.search(search);
		return OK(CommonDACParams.audit_history_record.name(), getResponseObject(auditHistoryLogEntities));
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<Map<String, Object>> getResponseObject(Map<String, Object> auditHistoryLogEntities) {
		List<Map<String, Object>> respObj = new ArrayList<Map<String, Object>>();
		List<Map<String,Object>> map = (List) auditHistoryLogEntities.get("results");
		for (Map<String, Object> resultMap : map) {
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
//						Timestamp createdOn  = (Timestamp) resultMap.get("createdOn");
//						DateFormat df = objectMapper.getDateFormat();
                        resultMap.put("createdOn", resultMap.get("createdOn"));
				}
					} catch (Exception e) {
						e.printStackTrace();
					}
			respObj.add(resultMap);
		}
		return respObj;
	}

	/**
	 * This method is used set the search criteria based on versionId to fetch
	 * AuditHistory from DB
	 * 
	 * @param versionId
	 *            The API versionId
	 */

	public SearchDTO setSearchCriteria(String versionId) {
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

	public SearchDTO setSearchCriteria(String versionId, boolean returnAllFields) {
		SearchDTO search = new SearchDTO();
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
		search.setFields(fields);
		return search;
	}
}