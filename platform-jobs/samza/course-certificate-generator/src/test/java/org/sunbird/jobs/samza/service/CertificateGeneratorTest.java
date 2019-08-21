package org.sunbird.jobs.samza.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sunbird.jobs.samza.service.util.CertificateGenerator;

import java.util.HashMap;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CertificateGenerator.class})
@PowerMockIgnore({"javax.management.*", "sun.security.ssl.*", "javax.net.ssl.*" , "javax.crypto.*"})
public class CertificateGeneratorTest {


    @Test
    public void testGenerateCertificate() {
        CertificateGenerator generator = PowerMockito.spy(new CertificateGenerator());
        Map<String, Object> request = new HashMap<>();
        request.put("action", "generate-course-certificate");
        request.put("iteration", 1);
        request.put("batchId", "0125450863553740809");
        request.put("userId", "874ed8a5-782e-4f6c-8f36-e0288455901e");
        request.put("courseId", "do_1125098016170639361238");
        request.put("certificateName","100PercentCompletionCertificate");

        generator.generate(request);

    }
}
