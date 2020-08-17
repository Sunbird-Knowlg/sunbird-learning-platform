package org.sunbird.jobs.samza.service;

import com.datastax.driver.core.Session;
import org.apache.samza.config.Config;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.ekstep.common.Platform;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sunbird.jobs.samza.service.util.CertificateGenerator;
import org.sunbird.jobs.samza.util.RedisConnect;
import redis.clients.jedis.Jedis;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CertificateGenerator.class})
@PowerMockIgnore({"javax.management.*", "sun.security.ssl.*", "javax.net.ssl.*" , "javax.crypto.*"})
public class CertificateGeneratorTest {

    private CertificateGenerator certificateGenerator;

    private MessageCollector collectorMock;
    @Mock(name = "certificateAuditEventStream")
    SystemStream certificateAuditEventStream = new SystemStream("kafka", Platform.config.getString("telemetry_raw_topic"));

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);
        collectorMock = Mockito.mock(MessageCollector.class);
    }

    @Test
    public void testGenerateCertificate() {
        Config config = Mockito.mock(Config.class);
        Mockito.when(config.get("redis.host")).thenReturn("localhost");
        Mockito.when(config.getInt("redis.port")).thenReturn(6379);
        Jedis redisConnect = new RedisConnect(config).getConnection();
        Session session = Mockito.mock(Session.class);
        CertificateGenerator generator = PowerMockito.spy(new CertificateGenerator(redisConnect, session));
        Map<String, Object> request = new HashMap<>();
        request.put("template", new HashMap<String, Object>(){{
            put("name", "Course completion certificate");
            put("identifier", "template_01");
            put("issuer", new HashMap<String, Object>(){{
                put("name", "Gujarat Council of Educational Research and Training");
                put("url", "https://gcert.gujarat.gov.in/gcert/");
            }});
            put("signatoryList", new ArrayList<Map<String, Object>>(){{
                add(new HashMap<String, Object>(){{
                    put("name", "CEO Gujarat");
                    put("id", "CEO");
                    put("designation", "CEO");
                    put("image", "https://cdn.pixabay.com/photo/2014/11/09/08/06/signature-523237__340.jpg");
                }});
            }});
        }});
        request.put("action", "generate-course-certificate");
        request.put("iteration", 1);
        request.put("batchId", "0128764408268881921");
        request.put("userId", "8454cb21-3ce9-4e30-85b5-fade097880d8");
        request.put("courseId", "do_112876410268794880162");

        generator.generate(request, collectorMock);

    }

    @Test
    public void testPushAuditEvent() throws Exception{
        mockAllSession();
        Method pushAuditEventMethod = CertificateGenerator.class.getDeclaredMethod("pushAuditEvent", String.class, String.class, String.class, Map.class, MessageCollector.class);
        pushAuditEventMethod.setAccessible(true);
        Map<String, Object> certificate = new HashMap<String, Object>() {{
            put("id", "4a3538d5-f671-41dd-b200-46f0853de0cc");
        }};
        String batchId = "0128057392291102720";
        String courseId = "do_1127212344324751361295";
        String userId = "874ed8a5-782e-4f6c-8f36-e0288455901e";
        pushAuditEventMethod.invoke(certificateGenerator, userId, courseId, batchId, certificate, collectorMock);
    }

    @Test
    public void testGenerateAuditEvent() throws Exception{
        mockAllSession();
        Method generateAuditEventMethod = CertificateGenerator.class.getDeclaredMethod("generateAuditEvent", String.class, String.class, String.class, Map.class);
        generateAuditEventMethod.setAccessible(true);
        Map<String, Object> certificate = new HashMap<String, Object>() {{
            put("id", "4a3538d5-f671-41dd-b200-46f0853de0cc");
        }};
        String batchId = "0128057392291102720";
        String courseId = "do_1127212344324751361295";
        String userId = "874ed8a5-782e-4f6c-8f36-e0288455901e";
        Map<String, Object> certAuditEvent = (Map<String, Object>) generateAuditEventMethod.invoke(certificateGenerator, userId, courseId, batchId, certificate);
        Assert.assertTrue(certAuditEvent.containsKey("actor"));
        Assert.assertTrue(certAuditEvent.containsKey("edata"));
        Assert.assertTrue(certAuditEvent.containsKey("object"));
        Assert.assertTrue(certAuditEvent.containsKey("context"));
        Assert.assertTrue(certAuditEvent.containsKey("eid") && certAuditEvent.containsValue("AUDIT"));
        Assert.assertTrue(certAuditEvent.containsKey("mid"));
    }

    public void mockAllSession(){
        Config config = Mockito.mock(Config.class);
        Mockito.when(config.get("redis.host")).thenReturn("localhost");
        Mockito.when(config.getInt("redis.port")).thenReturn(6379);
        Jedis redisConnect = new RedisConnect(config).getConnection();
        Session session = Mockito.mock(Session.class);
        certificateGenerator = PowerMockito.spy(new CertificateGenerator(redisConnect, session, certificateAuditEventStream));
    }
}
