package org.sunbird.jobs.service.util;

import com.datastax.driver.core.Session;
import org.apache.samza.config.Config;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.ekstep.common.Platform;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sunbird.jobs.samza.service.util.BaseCourseBatchUpdater;
import org.sunbird.jobs.samza.service.util.CourseBatchUpdater;
import org.sunbird.jobs.samza.task.CourseProgressHandler;
import org.sunbird.jobs.samza.util.RedisConnect;
import redis.clients.jedis.Jedis;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.anyString;

@Ignore
@RunWith(PowerMockRunner.class)
@PrepareForTest({CourseBatchUpdater.class, BaseCourseBatchUpdater.class})
@PowerMockIgnore({"javax.management.*", "sun.security.ssl.*", "javax.net.ssl.*" , "javax.crypto.*"})
public class CourseBatchUpdaterTest {

    private MessageCollector collectorMock;
    @Mock(name = "certificateInstructionStream")
        SystemStream certificateInstructionStream = new SystemStream("kafka", Platform.config.getString("course.batch.certificate.topic"));

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);
        collectorMock = Mockito.mock(MessageCollector.class);
    }

    @Test
    public void testUpdatebatchStatus() throws Exception {
        Config config = Mockito.mock(Config.class);
        Mockito.when(config.get("redis.host")).thenReturn("localhost");
        Mockito.when(config.getInt("redis.port")).thenReturn(6379);
        Jedis redisConnect = new RedisConnect(config).getConnection();
        Session session = Mockito.mock(Session.class);
        CourseBatchUpdater updater = PowerMockito.spy(new CourseBatchUpdater(redisConnect, session));
        PowerMockito.doReturn(new HashMap<String, Object>(){{
            put("leafNodes",new ArrayList<String>() {{ add("do_112719210075947008187");}});
        }}).when(updater, "getContent", anyString(), anyString());

        Map<String, Object> request = new HashMap<>();

        request.put("action", "batch-enrolment-update");
        request.put("iteration", 1);
        request.put("batchId", "0128057392291102720");
        request.put("userId", "874ed8a5-782e-4f6c-8f36-e0288455901e");
        request.put("courseId", "do_1127212344324751361295");
        request.put("contents", new ArrayList<Map<String, Object>>() {{ add(new HashMap<String, Object>(){{
            put("contentId", "do_112719210075947008187");
            put("status", 2);
        }});}});

        updater.updateBatchStatus(request, new CourseProgressHandler());

    }

    @Test
    public void testPushCertificateEvents() {
        CourseProgressHandler courseProgressHandler = new CourseProgressHandler();
        Config config = Mockito.mock(Config.class);
        Mockito.when(config.get("redis.host")).thenReturn("localhost");
        Mockito.when(config.getInt("redis.port")).thenReturn(6379);
        Jedis redisConnect = new RedisConnect(config).getConnection();
        Session session = Mockito.mock(Session.class);
        SystemStream certificateInstructionStream = new SystemStream("kafka", "coursebatch.certificate.request");
        CourseBatchUpdater updater = PowerMockito.spy(new CourseBatchUpdater(redisConnect, session, certificateInstructionStream));

        List<Map<String, Object>> userCertificateEvents = new ArrayList<Map<String, Object>>(){{
            add(new HashMap<String, Object>(){{
              put("batchId", "0128057392291102720");
              put("userId", "874ed8a5-782e-4f6c-8f36-e0288455901e");
              put("courseId", "do_1127212344324751361295");
            }});
        }};
        updater.pushCertificateEvents(userCertificateEvents, collectorMock);
    }

    @Test
    public void testgenerateInstructionEvent() throws Exception{
        Config config = Mockito.mock(Config.class);
        Mockito.when(config.get("redis.host")).thenReturn("localhost");
        Mockito.when(config.getInt("redis.port")).thenReturn(6379);
        Jedis redisConnect = new RedisConnect(config).getConnection();
        Session session = Mockito.mock(Session.class);
        SystemStream certificateInstructionStream = new SystemStream("kafka", "coursebatch.certificate.request");
        CourseBatchUpdater updater = PowerMockito.spy(new CourseBatchUpdater(redisConnect, session, certificateInstructionStream));
        Method generateInstructionEventMethod = CourseBatchUpdater.class.getDeclaredMethod("generateInstructionEvent", Map.class);
        generateInstructionEventMethod.setAccessible(true);
        Map<String, Object> certificateEvent = new HashMap<>();
        certificateEvent.put("batchId", "0128057392291102720");
        certificateEvent.put("userId", "874ed8a5-782e-4f6c-8f36-e0288455901e");
        certificateEvent.put("courseId", "do_1127212344324751361295");
        Map<String, Object> updatedCertificateEvent = (Map<String, Object>) generateInstructionEventMethod.invoke(updater, certificateEvent);
        Assert.assertTrue(updatedCertificateEvent.containsKey("actor"));
        Assert.assertTrue(updatedCertificateEvent.containsKey("edata"));
        Map<String, Object> event = (Map<String, Object>) updatedCertificateEvent.get("edata");
        Assert.assertTrue(event.get("batchId").equals("0128057392291102720"));
        Assert.assertTrue(event.get("courseId").equals("do_1127212344324751361295"));
        Assert.assertTrue(((ArrayList<Object>) event.get("userIds")).get(0).equals("874ed8a5-782e-4f6c-8f36-e0288455901e"));
        Assert.assertTrue(updatedCertificateEvent.containsKey("eid") && updatedCertificateEvent.containsValue("BE_JOB_REQUEST"));
        Assert.assertTrue(updatedCertificateEvent.containsKey("mid"));
    }
}
