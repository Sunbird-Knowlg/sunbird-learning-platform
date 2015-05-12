package com.ilimi.dac.impl;

import java.lang.reflect.Type;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.googlecode.genericdao.search.Filter;
import com.googlecode.genericdao.search.Search;
import com.ilimi.dac.BaseDataAccessService;
import com.ilimi.dac.TransformationHelper;
import com.ilimi.dac.dto.AuditRecord;
import com.ilimi.dac.dto.Comment;
import com.ilimi.dac.impl.entity.AuditLogEntity;
import com.ilimi.dac.impl.entity.CommentEntity;
import com.ilimi.dac.impl.entity.dao.AuditLogDao;
import com.ilimi.dac.impl.entity.dao.CommentDao;
import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.Response;
import com.ilimi.graph.common.dto.BaseValueObjectList;
import com.ilimi.graph.common.dto.Identifier;
import com.ilimi.graph.common.dto.StringValue;
import com.ilimi.graph.common.enums.CommonsDacParams;

/**
 * 
 * @author Mahesh
 *
 */

@Component
public class AuditLogDataService extends BaseDataAccessService implements IAuditLogDataService {

    /** The model mapper. */
    private ModelMapper modelMapper = null;
    
    
    
    public AuditLogDataService() {
        modelMapper = new ModelMapper();
        TransformationHelper.createTypeMap(modelMapper, AuditRecord.class, AuditLogEntity.class);
        TransformationHelper.createTypeMap(modelMapper, Comment.class, CommentEntity.class);
    }

    @Autowired
    AuditLogDao dao = null;
    
    @Autowired
    CommentDao commentDao = null;
    
    
    @Transactional
    public Response saveAuditLog(Request request) {
        AuditRecord auditRecord = (AuditRecord) request.get(CommonsDacParams.AUDIT_RECORD.name()); 
        AuditLogEntity entity = new AuditLogEntity();
        modelMapper.map(auditRecord, entity);
        dao.save(entity);
        return OK(CommonsDacParams.AUDIT_RECORD_ID.name(), new Identifier(entity.getId()));
    }
    
    @Transactional
    public Response saveComment(Request request) {
        Comment comment = (Comment) request.get(CommonsDacParams.COMMENT.name());
        CommentEntity entity = new CommentEntity();
        modelMapper.map(comment, entity);
        commentDao.save(entity);
        comment.setId(entity.getId());
        return OK(CommonsDacParams.COMMENT.name(), comment);
    }
    
    @Transactional
    public Response getAuditHistory(Request request) {
        StringValue objectId = (StringValue) request.get(CommonsDacParams.OBJECT_ID.name());
        Search search = new Search();
        search.addFilterEqual("objectId", objectId.getId());
        List<AuditLogEntity> auditLogEntities = dao.search(search);
        Type listType = new TypeToken<List<AuditRecord>>() {}.getType();
        List<AuditRecord> auditRecords = modelMapper.map(auditLogEntities, listType);
        return OK(CommonsDacParams.AUDIT_RECORDS.name(), new BaseValueObjectList<AuditRecord>(auditRecords));
    }
    
    @Transactional
    public Response getCommentThread(Request request) {
        StringValue objectId = (StringValue) request.get(CommonsDacParams.OBJECT_ID.name());
        StringValue threadId = (StringValue) request.get(CommonsDacParams.COMMENT_THREAD_ID.name());
        Search search = new Search();
        search.addFilterAnd(new Filter("objectId", objectId.getId()), new Filter("threadId", threadId.getId()));
        List<CommentEntity> commentEntities = commentDao.search(search);
        Type listType = new TypeToken<List<Comment>>() {}.getType();
        List<Comment> comments = modelMapper.map(commentEntities, listType);
        return OK(CommonsDacParams.COMMENTS.name(), new BaseValueObjectList<Comment>(comments));
    }
    
    @Transactional
    public Response getComments(Request request) {
        StringValue objectId = (StringValue) request.get(CommonsDacParams.OBJECT_ID.name());
        Search search = new Search();
        search.addFilterAnd(new Filter("objectId", objectId.getId()), Filter.isNull("threadId"));
        List<CommentEntity> commentEntities = commentDao.search(search);
        Type listType = new TypeToken<List<Comment>>() {}.getType();
        List<Comment> comments = modelMapper.map(commentEntities, listType);
        return OK(CommonsDacParams.COMMENTS.name(), new BaseValueObjectList<Comment>(comments));
    }
    
}
