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
        request.put("batchId", "0128318484998225920");
        request.put("userId", "95e4942d-cbe8-477d-aebd-ad8e6de4bfc8");
        request.put("courseId", "do_11283183576849612818");
        request.put("certificateName","100PercentCompletionCertificate");

        generator.generate(request);

    }
}
