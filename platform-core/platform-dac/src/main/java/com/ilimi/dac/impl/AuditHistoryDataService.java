package com.ilimi.dac.impl;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
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
 * The Class AuditHistoryDataService provides implementations of the various operations
 * defined in the IAuditHistoryDataService
 * It extends BaseDataAccessService  which is base class for DAC services.
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
	private ObjectMapper objecMapper = null;

    /** This is the init method for the AuditHistoryDataService */	
	public AuditHistoryDataService() {
		modelMapper = new ModelMapper();
		TransformationHelper.createTypeMap(modelMapper, AuditHistoryRecord.class, AuditHistoryEntity.class);
		objecMapper = new ObjectMapper();
		// objecMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
		// false);
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a z");
		objecMapper.setDateFormat(df);
	}

	@Autowired
	AuditHistoryDao dao = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ilimi.dac.impl.IAuditHistoryDataService #saveAuditHistoryLog(java.lang.String,
	 * java.lang.String, java.io.File, java.lang.String)
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
	 * @see com.ilimi.dac.impl.IAuditHistoryDataService #getAuditHistoryLog(java.lang.String,
	 * java.lang.String, java.io.File, java.lang.String)
	 */
	@Transactional
	public Response getAuditHistoryLog(Request request) {

		Date start_date = (Date) request.get(CommonDACParams.start_date.name());
		Date end_date = (Date) request.get(CommonDACParams.end_date.name());

		Search search = new Search();
		if (start_date != null)
			search.addFilterGreaterOrEqual("createdOn", start_date);
		if (end_date != null)
			search.addFilterLessOrEqual("createdOn", end_date);
		List<AuditHistoryEntity> auditHistoryLogEntities = dao.search(search);
		Type listType = new TypeToken<List<AuditHistoryRecord>>() {
		}.getType();
		List<AuditHistoryRecord> auditHistoryRecords = modelMapper.map(auditHistoryLogEntities, listType);
		return OK(CommonDACParams.audit_history_record.name(), getAllResponseObject(auditHistoryRecords));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ilimi.dac.impl.IAuditHistoryDataService #getAuditHistoryLogByObjectType(java.lang.String,
	 * java.lang.String, java.io.File, java.lang.String)
	 */
	@Transactional
	public Response getAuditHistoryLogByObjectType(Request request) {

		String graphId = (String) request.get(CommonDACParams.graph_id.name());
		String objectType = (String) request.get(CommonDACParams.object_type.name());
		Date start_date = (Date) request.get(CommonDACParams.start_date.name());
		Date end_date = (Date) request.get(CommonDACParams.end_date.name());

		Search search = new Search();
		search.addFilterEqual("graphId", graphId);
		search.addFilterEqual("objectType", objectType);
		if (start_date != null)
			search.addFilterGreaterOrEqual("createdOn", start_date);
		if (end_date != null)
			search.addFilterLessOrEqual("createdOn", end_date);
		List<AuditHistoryEntity> auditHistoryLogEntities = dao.search(search);
		Type listType = new TypeToken<List<AuditHistoryRecord>>() {
		}.getType();
		List<AuditHistoryRecord> auditHistoryRecords = modelMapper.map(auditHistoryLogEntities, listType);
		return OK(CommonDACParams.audit_history_record.name(), getAllResponseObject(auditHistoryRecords));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ilimi.dac.impl.IAuditHistoryDataService #getAuditHistoryLogByObjectId(java.lang.String,
	 * java.lang.String, java.io.File, java.lang.String)
	 */
	@Transactional
	public Response getAuditHistoryLogByObjectId(Request request) {
		String graphId = (String) request.get(CommonDACParams.graph_id.name());
		String objectId = (String) request.get(CommonDACParams.object_id.name());
		Date start_date = (Date) request.get(CommonDACParams.start_date.name());
		Date end_date = (Date) request.get(CommonDACParams.end_date.name());

		Search search = new Search();
		search.addFilterEqual("graphId", graphId);
		search.addFilterEqual("objectId", objectId);
		if (start_date != null)
			search.addFilterGreaterOrEqual("createdOn", start_date);
		if (end_date != null)
			search.addFilterLessOrEqual("createdOn", end_date);

		List<AuditHistoryEntity> auditHistoryLogEntities = dao.search(search);
		Type listType = new TypeToken<List<AuditHistoryRecord>>() {
		}.getType();
		List<AuditHistoryRecord> auditHistoryRecords = modelMapper.map(auditHistoryLogEntities, listType);
		return OK(CommonDACParams.audit_history_record.name(), getAllResponseObject(auditHistoryRecords));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ilimi.dac.impl.IAuditHistoryDataService #getAuditLogRecordByAuditId(java.lang.String,
	 * java.lang.String, java.io.File, java.lang.String)
	 */
	@Transactional
	public Response getAuditLogRecordByAuditId(Request request) {
		String audit_id = (String) request.get(CommonDACParams.audit_id.name());
		Date start_date = (Date) request.get(CommonDACParams.start_date.name());
		Date end_date = (Date) request.get(CommonDACParams.end_date.name());

		Search search = new Search();
		search.addFilterEqual("audit_id", audit_id);
		if (start_date != null)
			search.addFilterGreaterOrEqual("createdOn", start_date);
		if (end_date != null)
			search.addFilterLessOrEqual("createdOn", end_date);

		List<AuditHistoryEntity> auditHistoryLogEntities = dao.search(search);
		Type listType = new TypeToken<List<AuditHistoryRecord>>() {
		}.getType();
		List<AuditHistoryRecord> auditHistoryRecords = modelMapper.map(auditHistoryLogEntities, listType);
		return OK(CommonDACParams.audit_history_record.name(), getResponseObject(auditHistoryRecords));

	}

	/**
	 * This method is used to get the ResponseObject in required format
	 * 
	 * @param List of AuditHistoryRecords
	 *                The records
	 * @return ResponseObject which holds the actual result of the operation
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<Map<String, Object>> getResponseObject(List<AuditHistoryRecord> records) {
		
		List<Map<String, Object>> respObj = new ArrayList<Map<String, Object>>();
		objecMapper = new ObjectMapper();
		HashMap<String, Object> summary = new HashMap<String, Object>();
		
		for (AuditHistoryRecord record : records) {
			Map<String, Object> props = objecMapper.convertValue(record, Map.class);
			Object logRecordObj = props.get("logRecord");
			String summaryRecordObj = (String) props.get("summary");
			
			if (logRecordObj != null && logRecordObj instanceof String) {
				Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss a z").create();
				Map logRecordProps = gson.fromJson(logRecordObj.toString(), Map.class);
				props.put("logRecord", logRecordProps);
			}
			
			if (summaryRecordObj != null && summaryRecordObj instanceof String) {
				TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
				};
				try {
					summary = objecMapper.readValue(summaryRecordObj, typeRef);
				} catch (IOException e) {
					e.printStackTrace();
				}
				props.put("summary", summary);
			}
			respObj.add(props);
		}
		return respObj;
	}

	/**
	 * This method is used to get the ResponseObject in required format
	 * 
	 * @param List of AuditHistoryRecords
	 *                The records
	 * @return ResponseObject which holds the actual result of the operation
	 */
	@SuppressWarnings({ "unchecked" })
	private List<Map<String, Object>> getAllResponseObject(List<AuditHistoryRecord> records) {
		objecMapper = new ObjectMapper();
		HashMap<String, Object> summary = new HashMap<String, Object>();
		List<Map<String, Object>> respObj = new ArrayList<Map<String, Object>>();
		for (AuditHistoryRecord record : records) {
			Map<String, Object> props = objecMapper.convertValue(record, Map.class);
			String summaryRecordObj = (String) props.get("summary");

			if (props.containsKey("logRecord")) {
				props.remove("logRecord");
			}

			if (summaryRecordObj != null && summaryRecordObj instanceof String) {
				TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
				};
				try {
					summary = objecMapper.readValue(summaryRecordObj, typeRef);
				} catch (IOException e) {
					e.printStackTrace();
				}
				props.put("summary", summary);
			}
			respObj.add(props);
		}
		return respObj;
	}

}
