package com.ilimi.test;

import java.util.List;

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.dac.dto.AuditRecord;
import com.ilimi.dac.dto.Comment;
import com.ilimi.dac.enums.CommonDACParams;
import com.ilimi.dac.impl.IAuditLogDataService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:mw-spring/commons-dac-context.xml" })
public class AuditLogDacServiceTest {

    @Autowired
    IAuditLogDataService auditLogDataService;

    // @org.junit.Test
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
        request.put(CommonDACParams.audit_record.name(), auditRecord);
        Response response = auditLogDataService.saveAuditLog(request);
        Integer iden = (Integer) response.get(CommonDACParams.audit_record_id.name());
        Assert.assertNotNull(iden);
        System.out.println("Identifier: " + iden);
    }

    @SuppressWarnings("unchecked")
    // @org.junit.Test
    public void testGetHistory() {
        Request request = new Request();
        request.put(CommonDACParams.object_id.name(), "g-NUMERACY");
        Response response = auditLogDataService.getAuditHistory(request);
        List<AuditRecord> records = (List<AuditRecord>) response.get(CommonDACParams.audit_records.name());
        Assert.assertNotNull(records);
        System.out.println("Response:" + records.size());
    }
}
