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
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.dac.BaseDataAccessService;
import com.ilimi.dac.TransformationHelper;
import com.ilimi.dac.dto.AuditRecord;
import com.ilimi.dac.dto.Comment;
import com.ilimi.dac.enums.CommonDACParams;
import com.ilimi.dac.impl.entity.AuditLogEntity;
import com.ilimi.dac.impl.entity.CommentEntity;
import com.ilimi.dac.impl.entity.dao.AuditLogDao;
import com.ilimi.dac.impl.entity.dao.CommentDao;

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
        AuditRecord auditRecord = (AuditRecord) request.get(CommonDACParams.audit_record.name());
        AuditLogEntity entity = new AuditLogEntity();
        modelMapper.map(auditRecord, entity);
        dao.save(entity);
        return OK(CommonDACParams.audit_record_id.name(), entity.getId());
    }

    @Transactional
    public Response saveComment(Request request) {
        Comment comment = (Comment) request.get(CommonDACParams.comment.name());
        CommentEntity entity = new CommentEntity();
        modelMapper.map(comment, entity);
        commentDao.save(entity);
        comment.setId(entity.getId());
        return OK(CommonDACParams.comment.name(), comment);
    }

    @Transactional
    public Response getAuditHistory(Request request) {
        String objectId = (String) request.get(CommonDACParams.object_id.name());
        Search search = new Search();
        search.addFilterEqual("objectId", objectId);
        List<AuditLogEntity> auditLogEntities = dao.search(search);
        Type listType = new TypeToken<List<AuditRecord>>() {
        }.getType();
        List<AuditRecord> auditRecords = modelMapper.map(auditLogEntities, listType);
        return OK(CommonDACParams.audit_records.name(), auditRecords);
    }

    @Transactional
    public Response getCommentThread(Request request) {
        String objectId = (String) request.get(CommonDACParams.object_id.name());
        String threadId = (String) request.get(CommonDACParams.comment_thread_id.name());
        Search search = new Search();
        search.addFilterAnd(new Filter("objectId", objectId), new Filter("threadId", threadId));
        List<CommentEntity> commentEntities = commentDao.search(search);
        Type listType = new TypeToken<List<Comment>>() {
        }.getType();
        List<Comment> comments = modelMapper.map(commentEntities, listType);
        return OK(CommonDACParams.comments.name(), comments);
    }

    @Transactional
    public Response getComments(Request request) {
        String objectId = (String) request.get(CommonDACParams.object_id.name());
        Search search = new Search();
        search.addFilterAnd(new Filter("objectId", objectId), Filter.isNull("threadId"));
        List<CommentEntity> commentEntities = commentDao.search(search);
        Type listType = new TypeToken<List<Comment>>() {
        }.getType();
        List<Comment> comments = modelMapper.map(commentEntities, listType);
        return OK(CommonDACParams.comments.name(), comments);
    }

}
