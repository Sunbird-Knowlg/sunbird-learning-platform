package org.sunbird.jobs.service.util;

import com.datastax.driver.core.Session;
import org.apache.samza.config.Config;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sunbird.jobs.samza.service.util.BaseCourseBatchUpdater;
import org.sunbird.jobs.samza.service.util.BatchStatusUpdater;
import org.sunbird.jobs.samza.util.RedisConnect;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.anyString;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BatchStatusUpdater.class, BaseCourseBatchUpdater.class})
@PowerMockIgnore({"javax.management.*", "sun.security.ssl.*", "javax.net.ssl.*" , "javax.crypto.*"})
public class BatchStatusUpdaterTest {

    @Test
    public void testsync() throws Exception {
        Config config = Mockito.mock(Config.class);
        Session session = Mockito.mock(Session.class);
        BatchStatusUpdater updater = PowerMockito.spy(new BatchStatusUpdater(session));
        PowerMockito.doReturn(new HashMap<String, Object>(){{
            put("leafNodes",new ArrayList<String>() {{ add("do_11260735471149056012299");}});
        }}).when(updater, "getContent", anyString(), anyString());

        Map<String, Object> request = new HashMap<String, Object>(){{
            put("action", "batch-status-update");
            put("iteration", 1);
            put("courseId", "do_1127212344324751361295");
            put("batchId", "0127278807246684164753");
            put("status", 1);
        }};

        updater.update(request);
    }
}
