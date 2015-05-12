package com.ilimi.test;

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ilimi.dac.dto.AuditRecord;
import com.ilimi.dac.dto.Comment;
import com.ilimi.dac.impl.IAuditLogDataService;
import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.Response;
import com.ilimi.graph.common.dto.BaseValueObjectList;
import com.ilimi.graph.common.dto.Identifier;
import com.ilimi.graph.common.dto.StringValue;
import com.ilimi.graph.common.enums.CommonsDacParams;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:mw-spring/commons-dac-context.xml" })
public class AuditLogDacServiceTest {

    @Autowired
    IAuditLogDataService auditLogDataService;
    
//    @org.junit.Test
    public void testSaveAuditLog() {
        AuditRecord auditRecord = new AuditRecord();
        auditRecord.setObjectId("g-NUMERACY");
        auditRecord.setOperationType("CREATE");
        auditRecord.setLogRecord("request object");
        auditRecord.setStatus("OK");
        auditRecord.setStatusCode("SUCCESSFUL");
        auditRecord.setStatusMessage("Operation completed successfully.");
        Comment comment = new Comment();
        comment.setComment("My Comment");
        comment.setObjectId(auditRecord.getObjectId());
        comment.setTitle("My Comment");
        auditRecord.setComment(comment);
        
        Request request = new Request();
        request.put(CommonsDacParams.AUDIT_RECORD.name(), auditRecord);
        Response response = auditLogDataService.saveAuditLog(request);
        Identifier iden = (Identifier) response.get(CommonsDacParams.AUDIT_RECORD_ID.name());
        Assert.assertNotNull(iden);
        System.out.println("Identifier: "+iden);
    }
    
    @SuppressWarnings("unchecked")
//    @org.junit.Test
    public void testGetHistory() {
        Request request = new Request();
        request.put(CommonsDacParams.OBJECT_ID.name(), new StringValue("g-NUMERACY"));
        Response response = auditLogDataService.getAuditHistory(request);
        BaseValueObjectList<AuditRecord> records = (BaseValueObjectList<AuditRecord>) response.get(CommonsDacParams.AUDIT_RECORDS.name());
        Assert.assertNotNull(records);
        System.out.println("Response:"+records.getValueObjectList().size());
    }
}
