package com.ilimi.test;

import java.util.Date;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.dac.dto.Comment;
import com.ilimi.dac.dto.Version;
import com.ilimi.dac.enums.CommonDACParams;
import com.ilimi.dac.impl.IVersionDataService;

//@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:mw-spring/commons-dac-context.xml" })
public class VersionDacServiceTest {

    @Autowired
    IVersionDataService versionDataService;
    
//    @org.junit.Test
    public void testCreateVersion() {
        Version version = new Version();
        version.setObjectId("g-NUMERACY");
        version.setPath("/data/graphVersions/g-NUMERACY.1.0.rdf");
        version.setVersion("1.0");
        version.setLastModifiedOn(new Date());
        version.setLastModifiedBy("mahesh");
        Comment comment = new Comment();
        comment.setComment("My Comment");
        comment.setObjectId(version.getObjectId());
        comment.setTitle("My Comment");
        version.setComment(comment);
        Request request = new Request();
        request.put(CommonDACParams.object_version.name(), version);
        Response response = versionDataService.createVersion(request);
        System.out.println("response.get(CommonDACParams.OBJECT_VERSION_ID.name()):"+response.get(CommonDACParams.object_version_id.name()));
    }
}
