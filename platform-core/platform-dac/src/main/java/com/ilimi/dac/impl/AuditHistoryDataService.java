package com.ilimi.dac.impl;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.genericdao.search.Search;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.dac.BaseDataAccessService;
import com.ilimi.dac.TransformationHelper;
import com.ilimi.dac.dto.AuditHistoryRecord;
import com.ilimi.dac.enums.CommonDACParams;
import com.ilimi.dac.impl.entity.AuditHistoryEntity;
import com.ilimi.dac.impl.entity.dao.AuditHistoryDao;

@Component
public class AuditHistoryDataService extends BaseDataAccessService implements IAuditHistoryDataService{

    /** The model mapper. */
    private ModelMapper modelMapper = null;
    private ObjectMapper objecMapper = null;

	public AuditHistoryDataService() {
        modelMapper = new ModelMapper();
        TransformationHelper.createTypeMap(modelMapper, AuditHistoryRecord.class, AuditHistoryEntity.class);
        objecMapper = new ObjectMapper();
//        objecMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm a z");
        objecMapper.setDateFormat(df);

	}

    @Autowired
    AuditHistoryDao dao = null;

    @Transactional
	public Response saveAuditHistoryLog(Request request) {
        AuditHistoryRecord auditRecord = (AuditHistoryRecord) request.get(CommonDACParams.audit_history_record.name());
        AuditHistoryEntity entity = new AuditHistoryEntity();
        modelMapper.map(auditRecord, entity);
        dao.save(entity);
        return OK(CommonDACParams.audit_history_record_id.name(), entity.getId());

	}

    @Transactional
    public Response getAuditHistoryLog(Request request){
    
        Date start_date = (Date) request.get(CommonDACParams.start_date.name());
        Date end_date = (Date) request.get(CommonDACParams.end_date.name());

        Search search = new Search();
        search.addFilterGreaterOrEqual("createdOn", start_date);
        if(end_date!=null)
        	search.addFilterLessOrEqual("createdOn", end_date);
        List<AuditHistoryEntity> auditHistoryLogEntities = dao.search(search);
        Type listType = new TypeToken<List<AuditHistoryRecord>>() {
        }.getType();
        List<AuditHistoryRecord> auditHistoryRecords = modelMapper.map(auditHistoryLogEntities, listType);
        return OK(CommonDACParams.audit_history_record.name(), getResponseObject(auditHistoryRecords));
    }
    
    @Transactional
    public Response getAuditHistoryLogByObjectType(Request request){
    
    	String graphId = (String) request.get(CommonDACParams.graph_id.name());
    	String objectType = (String) request.get(CommonDACParams.object_type.name());
    	Date start_date = (Date) request.get(CommonDACParams.start_date.name());
        Date end_date = (Date) request.get(CommonDACParams.end_date.name());

        Search search = new Search();
        search.addFilterEqual("graphId", graphId);
        search.addFilterEqual("objectType", objectType);
        search.addFilterGreaterOrEqual("createdOn", start_date);
        if(end_date!=null)
        	search.addFilterLessOrEqual("createdOn", end_date);
        List<AuditHistoryEntity> auditHistoryLogEntities = dao.search(search);
        Type listType = new TypeToken<List<AuditHistoryRecord>>() {
        }.getType();
        List<AuditHistoryRecord> auditHistoryRecords = modelMapper.map(auditHistoryLogEntities, listType);
        return OK(CommonDACParams.audit_history_record.name(), getResponseObject(auditHistoryRecords));

    }
    
    @Transactional
    public Response getAuditHistoryLogByObjectId(Request request){
    	String graphId = (String) request.get(CommonDACParams.graph_id.name());
    	String objectId = (String) request.get(CommonDACParams.object_id.name());
    	Date start_date = (Date) request.get(CommonDACParams.start_date.name());
        Date end_date = (Date) request.get(CommonDACParams.end_date.name());

        Search search = new Search();
        search.addFilterEqual("graphId", graphId);
        search.addFilterEqual("objectId", objectId);
        search.addFilterGreaterOrEqual("createdOn", start_date);
        if(end_date!=null)
        	search.addFilterLessOrEqual("createdOn", end_date);
        
        List<AuditHistoryEntity> auditHistoryLogEntities = dao.search(search);
        Type listType = new TypeToken<List<AuditHistoryRecord>>() {
        }.getType();
        List<AuditHistoryRecord> auditHistoryRecords = modelMapper.map(auditHistoryLogEntities, listType);
        return OK(CommonDACParams.audit_history_record.name(), getResponseObject(auditHistoryRecords));

    }
    
    
    private List<Map<String, Object>> getResponseObject(List<AuditHistoryRecord> records){
    	List<Map<String, Object>> respObj=new ArrayList<Map<String, Object>>();
    	
    	for(AuditHistoryRecord record:records){
    		Map<String, Object> props = objecMapper.convertValue(record, Map.class);
    		respObj.add(props);
    	}
    	return respObj;
    }


}
