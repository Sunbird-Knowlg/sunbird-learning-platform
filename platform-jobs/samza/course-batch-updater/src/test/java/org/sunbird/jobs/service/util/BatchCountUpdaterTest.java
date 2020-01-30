package org.sunbird.jobs.service.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sunbird.jobs.samza.service.util.BaseCourseBatchUpdater;
import org.sunbird.jobs.samza.service.util.BatchCountUpdater;

import java.util.HashMap;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BatchCountUpdater.class, BaseCourseBatchUpdater.class})
@PowerMockIgnore({"javax.management.*", "sun.security.ssl.*", "javax.net.ssl.*" , "javax.crypto.*"})
public class BatchCountUpdaterTest {

    @Test
    public void testsync() throws Exception {
        BatchCountUpdater updater = PowerMockito.spy(new BatchCountUpdater());


        Map<String, Object> request = new HashMap<String, Object>(){{
            put("action", "course-batch-update");
            put("iteration", 1);
            put("courseId", "do_11283192356041523212");
            put("batchId", "0129408072098938880");
        }};

        updater.update(request);
    }
}
