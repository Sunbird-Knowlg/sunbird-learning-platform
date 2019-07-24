package org.sunbird.jobs.service.util;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sunbird.jobs.samza.service.util.BaseCourseBatchUpdater;
import org.sunbird.jobs.samza.service.util.BatchEnrolmentSync;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.anyString;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BatchEnrolmentSync.class, BaseCourseBatchUpdater.class})
@PowerMockIgnore({"javax.management.*", "sun.security.ssl.*", "javax.net.ssl.*" , "javax.crypto.*"})
public class BatchEnrolmentSyncTest {

    @Test
    public void testSyncEnrolment() throws Exception {
        BatchEnrolmentSync enrolSync = PowerMockito.spy(new BatchEnrolmentSync());
        PowerMockito.doReturn(new HashMap<String, Object>(){{
            put("leafNodes",new ArrayList<String>() {{ add("do_112719210075947008187");}});
        }}).when(enrolSync, "getContent", anyString(), anyString());

        Map<String, Object> request = new HashMap<>();

        request.put("action", "batch-enrolment-sync");
        request.put("iteration", 1);
        request.put("batchId", "0128057392291102720");
        request.put("userId", "874ed8a5-782e-4f6c-8f36-e0288455901e");
        request.put("courseId", "do_1127212344324751361295");
        request.put("reset", Arrays.asList("completionPercentage", "status"));

        enrolSync.syncEnrolment(request);
    }
}
