package org.sunbird.job.samza.util;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@Ignore
@RunWith(PowerMockRunner.class)
@PrepareForTest({CourseBatchUtil.class})
@PowerMockIgnore({"javax.management.*", "sun.security.ssl.*", "javax.net.ssl.*" , "javax.crypto.*"})
public class CourseBatchUtilTest {

    @Test
    public void testSyncBatch() {
        CourseBatchUtil spy = PowerMockito.spy(new CourseBatchUtil());

        spy.syncCourseBatch("do_11282833211754086411", null);
    }
}
