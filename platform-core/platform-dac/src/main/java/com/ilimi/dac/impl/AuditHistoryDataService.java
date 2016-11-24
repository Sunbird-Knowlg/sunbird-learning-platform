package com.ilimi.dac.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.googlecode.genericdao.search.Search;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.dac.BaseDataAccessService;
import com.ilimi.dac.TransformationHelper;
import com.ilimi.dac.dto.AuditHistoryRecord;
import com.ilimi.dac.enums.CommonDACParams;
import com.ilimi.dac.impl.entity.AuditHistoryEntity;
import com.ilimi.dac.impl.entity.dao.AuditHistoryDao;

/**
 * The Class AuditHistoryDataService provides implementations of the various
 * operations defined in the IAuditHistoryDataService It extends
 * BaseDataAccessService which is base class for DAC services.
 * 
 * @author Karthik, Rashmi
 * 
 * @see IAuditHistoryDataService
 */

@Component
public class AuditHistoryDataService extends BaseDataAccessService implements IAuditHistoryDataService {

	/** The model mapper. */
	private ModelMapper modelMapper = null;

	/** The Object mapper */
	private ObjectMapper objectMapper = null;

	/** This is the init method for the AuditHistoryDataService */
	public AuditHistoryDataService() {
		modelMapper = new ModelMapper();
		TransformationHelper.createTypeMap(modelMapper, AuditHistoryRecord.class, AuditHistoryEntity.class);
		objectMapper = new ObjectMapper();
		// objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
		// false);
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a z");
		objectMapper.setDateFormat(df);
	}

	@Autowired
	AuditHistoryDao dao = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ilimi.dac.impl.IAuditHistoryDataService
	 * #saveAuditHistoryLog(java.lang.String, java.lang.String, java.io.File,
	 * java.lang.String)
	 */
	@Transactional
	public Response saveAuditHistoryLog(Request request) {
		AuditHistoryRecord auditRecord = (AuditHistoryRecord) request.get(CommonDACParams.audit_history_record.name());
		AuditHistoryEntity entity = new AuditHistoryEntity();
		modelMapper.map(auditRecord, entity);
		dao.save(entity);
		return OK(CommonDACParams.audit_history_record_id.name(), entity.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ilimi.dac.impl.IAuditHistoryDataService
	 * #getAuditHistoryLog(java.lang.String, java.lang.String, java.io.File,
	 * java.lang.String)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Transactional
	public Response getAuditHistoryLog(Request request, String versionId) {

		Date start_date = (Date) request.get(CommonDACParams.start_date.name());
		Date end_date = (Date) request.get(CommonDACParams.end_date.name());
		Search search = new Search();
		search = setSearchCriteria(versionId);
		if (start_date != null)
			search.addFilterGreaterOrEqual("createdOn", start_date);
		if (end_date != null)
			search.addFilterLessOrEqual("createdOn", end_date);	
		List<AuditHistoryEntity> auditHistoryLogEntities = dao.search(search);
		List<Object> auditHistoryLogRecords = (List) auditHistoryLogEntities;
		return OK(CommonDACParams.audit_history_record.name(), getResponseObject(auditHistoryLogRecords));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ilimi.dac.impl.IAuditHistoryDataService
	 * #getAuditHistoryLogByObjectType(java.lang.String, java.lang.String,
	 * java.io.File, java.lang.String)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Transactional
	public Response getAuditHistoryLogByObjectType(Request request, String versionId) {

		String graphId = (String) request.get(CommonDACParams.graph_id.name());
		String objectType = (String) request.get(CommonDACParams.object_type.name());
		Date start_date = (Date) request.get(CommonDACParams.start_date.name());
		Date end_date = (Date) request.get(CommonDACParams.end_date.name());

		Search search = new Search();
		search = setSearchCriteria(versionId);
		search.addFilterEqual("graphId", graphId);
		search.addFilterEqual("objectType", objectType);
		if (start_date != null)
			search.addFilterGreaterOrEqual("createdOn", start_date);
		if (end_date != null)
			search.addFilterLessOrEqual("createdOn", end_date);
		List<AuditHistoryEntity> auditHistoryLogEntities = dao.search(search);
		List<Object> auditHistoryLogRecords = (List) auditHistoryLogEntities;
		return OK(CommonDACParams.audit_history_record.name(), getResponseObject(auditHistoryLogRecords));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ilimi.dac.impl.IAuditHistoryDataService
	 * #getAuditHistoryLogByObjectId(java.lang.String, java.lang.String,
	 * java.io.File, java.lang.String)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Transactional
	public Response getAuditHistoryLogByObjectId(Request request, String versionId) {
		String graphId = (String) request.get(CommonDACParams.graph_id.name());
		String objectId = (String) request.get(CommonDACParams.object_id.name());
		Date start_date = (Date) request.get(CommonDACParams.start_date.name());
		Date end_date = (Date) request.get(CommonDACParams.end_date.name());

		Search search = new Search();
		search = setSearchCriteria(versionId);
		search.addFilterEqual("graphId", graphId);
		search.addFilterEqual("objectId", objectId);
		if (start_date != null)
			search.addFilterGreaterOrEqual("createdOn", start_date);
		if (end_date != null)
			search.addFilterLessOrEqual("createdOn", end_date);
		List<AuditHistoryEntity> auditHistoryLogEntities = dao.search(search);
		List<Object> auditHistoryLogRecords = (List) auditHistoryLogEntities;
		return OK(CommonDACParams.audit_history_record.name(), getResponseObject(auditHistoryLogRecords));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ilimi.dac.impl.IAuditHistoryDataService
	 * #getAuditLogRecordByAuditId(java.lang.String, java.lang.String,
	 * java.io.File, java.lang.String)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Transactional
	public Response getAuditLogRecordById(Request request) {
		String objectId = (String) request.get(CommonDACParams.object_id.name());
		Date time_stamp = (Date) request.get(CommonDACParams.time_stamp.name());

		Search search = new Search();
		search = setSearchCriteria(null, true);
		if (time_stamp != null)
			search.addFilterEqual("createdOn", time_stamp);
		search.addFilterEqual("objectId", objectId);
		
		List<AuditHistoryEntity> auditHistoryLogEntities = dao.search(search);
		List<Object> auditHistoryLogRecords = (List) auditHistoryLogEntities;
		return OK(CommonDACParams.audit_history_record.name(), getResponseObject(auditHistoryLogRecords));
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
	private List<Map<String, Object>> getResponseObject(List<Object> records) {
		List<Map<String, Object>> respObj = new ArrayList<Map<String, Object>>();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		for (Object record : records) {
			try {
				if (record != null) {
					resultMap = (Map) record;
					try {
						if (resultMap.containsKey("summary")) {
							String summaryData = (String) resultMap.get("summary");
								if(summaryData != null && summaryData instanceof String){
									Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss a z").create();
									Map summary = gson.fromJson(summaryData.toString(), Map.class);
									resultMap.put("summary", summary);
								}
						}
						if (resultMap.containsKey("logRecord")) {
							String logData = (String) resultMap.get("logRecord");
								if(logData != null && logData instanceof String){
									Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss a z").create();
									Map logRecord = gson.fromJson(logData.toString(), Map.class);
									resultMap.put("logRecord", logRecord);
								}
						}
						String createdOn = (String) resultMap.get("createdOn");
						if(createdOn != null && createdOn instanceof String){
							Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss a z").create();
							Map created = gson.fromJson(createdOn.toString(), Map.class);
									resultMap.put("createdOn", created);
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
		return respObj;
	}
	
	/**
	 * This method is used set the search criteria based on 
	 * versionId to fetch AuditHistory from DB
	 * @param versionId
	 * 			The API versionId
	 */
	
	public Search setSearchCriteria(String versionId) {
		return setSearchCriteria(versionId, false);
	}

	/**
	 * This method is used set the search criteria based on 
	 * versionId to fetch required AuditHistory fields from DB
	 * @param versionId
	 * 		The API versionId
	 * @param returnAllFields
	 * 	 	The boolean value to retun fields
	 */
	
	public Search setSearchCriteria(String versionId, boolean returnAllFields) {
		Search search = new Search();
		search.addField("audit_id", "id");
		search.addField("label", "label");
		search.addField("objectId", "objectId");
		search.addField("objectType", "objectType");
		search.addField("operation", "operation");
		search.addField("requestId", "requestId");
		search.addField("userId", "userId");
		search.addField("graphId", "graphId");
		search.addField("createdOn", "createdOn");
		if (returnAllFields) {
			search.addField("logRecord", "logRecord");
			search.addField("summary", "summary");
		} else {
			if (StringUtils.equalsIgnoreCase("1.0", versionId))
				search.addField("logRecord", "logRecord");
			else
				search.addField("summary", "summary");
		}
		return search;
	}
}