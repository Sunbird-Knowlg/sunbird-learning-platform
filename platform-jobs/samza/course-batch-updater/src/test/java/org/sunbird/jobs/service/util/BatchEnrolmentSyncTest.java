package org.sunbird.jobs.service.util;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sunbird.jobs.samza.service.util.BaseCourseBatchUpdater;
import org.sunbird.jobs.samza.service.util.BatchEnrolmentSync;

import java.lang.reflect.Method;
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
            put("leafNodes",new ArrayList<String>() {{ add("do_11260735471149056012299");}});
        }}).when(enrolSync, "getContent", anyString(), anyString());

        Map<String, Object> request = new HashMap<>();

        request.put("action", "batch-enrolment-sync");
        request.put("iteration", 1);
        request.put("batchId", "0126083288437637121");
        request.put("userId", "8454cb21-3ce9-4e30-85b5-fade097880d8");
        request.put("courseId", "do_1127212344324751361295");
        request.put("reset", Arrays.asList("completionPercentage", "status", "contentStatus", "lastReadContentId", "lastReadContentStatus"));

        enrolSync.syncEnrolment(request);
    }
    
    @Test
    public void testGetLatestReadContent() throws Exception {
        BatchEnrolmentSync enrolSync = PowerMockito.spy(new BatchEnrolmentSync());
        Map<String, String> lastReadContents = new HashMap<String, String>(){{
            put("content2", null);
            put("content1", null);
        }};
        Method method = BatchEnrolmentSync.class.getDeclaredMethod("fetchLatestLastReadContent", Map.class);
        method.setAccessible(true);
        String contentId = (String) method.invoke(enrolSync, lastReadContents);
        Assert.assertNull(contentId);
        Map<String, String> lastReadContents1 = new HashMap<String, String>(){{
            put("content2", "2020-04-01");
            put("content1", "2020-05-01");
        }};
        Assert.assertNotNull((String) method.invoke(enrolSync, lastReadContents1));
    }
}
