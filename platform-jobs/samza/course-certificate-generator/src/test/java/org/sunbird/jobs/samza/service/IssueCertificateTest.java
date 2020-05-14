package org.sunbird.jobs.samza.service;

import com.datastax.driver.core.Session;
import org.apache.samza.config.Config;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sunbird.jobs.samza.service.util.IssueCertificate;

import java.util.HashMap;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({IssueCertificate.class})
@PowerMockIgnore({"javax.management.*", "sun.security.ssl.*", "javax.net.ssl.*" , "javax.crypto.*"})
public class IssueCertificateTest {


    @Test
    public void testIssueCert() {
        Config config = Mockito.mock(Config.class);
        Session session = Mockito.mock(Session.class);
        IssueCertificate spy = PowerMockito.spy(new IssueCertificate(session));

        Map<String, Object> request = new HashMap<>();
        request.put("action", "issue-certificate");
        request.put("iteration", 1);
        request.put("batchId", "0125450863553740809");
        request.put("courseId", "do_1125098016170639361238");

        spy.issue(request, null);
    }



}
