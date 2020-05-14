package org.sunbird.jobs.service.util;

import com.datastax.driver.core.Session;
import org.apache.samza.config.Config;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sunbird.jobs.samza.service.util.BaseCourseBatchUpdater;
import org.sunbird.jobs.samza.service.util.CourseBatchUpdater;
import org.sunbird.jobs.samza.util.RedisConnect;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.anyString;

@Ignore
@RunWith(PowerMockRunner.class)
@PrepareForTest({CourseBatchUpdater.class, BaseCourseBatchUpdater.class})
@PowerMockIgnore({"javax.management.*", "sun.security.ssl.*", "javax.net.ssl.*" , "javax.crypto.*"})
public class CourseBatchUpdaterTest {

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

        updater.updateBatchStatus(request);

    }
}
