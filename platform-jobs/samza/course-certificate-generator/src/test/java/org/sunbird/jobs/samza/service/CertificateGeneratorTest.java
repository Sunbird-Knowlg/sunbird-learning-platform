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
import org.sunbird.jobs.samza.service.util.CertificateGenerator;
import org.sunbird.jobs.samza.util.RedisConnect;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CertificateGenerator.class})
@PowerMockIgnore({"javax.management.*", "sun.security.ssl.*", "javax.net.ssl.*" , "javax.crypto.*"})
public class CertificateGeneratorTest {


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

        generator.generate(request);

    }
}