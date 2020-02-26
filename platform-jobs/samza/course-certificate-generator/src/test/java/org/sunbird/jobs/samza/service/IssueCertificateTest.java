package org.sunbird.jobs.samza.service;

import org.junit.Test;
import org.junit.runner.RunWith;
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
        IssueCertificate spy = PowerMockito.spy(new IssueCertificate());

        Map<String, Object> request = new HashMap<>();
        request.put("action", "issue-certificate");
        request.put("iteration", 1);
        request.put("batchId", "0128764408268881921");
        request.put("courseId", "do_112876410268794880162");

        spy.issue(request, null);
    }



}
