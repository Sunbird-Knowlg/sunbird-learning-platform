package org.ekstep.job.samza.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BatchSyncUtil.class})
@PowerMockIgnore({"javax.management.*", "sun.security.ssl.*", "javax.net.ssl.*" , "javax.crypto.*"})
public class BatchSyncUtilTest {

    @Test
    public void testSyncBatch() {
        BatchSyncUtil spy = PowerMockito.spy(new BatchSyncUtil());

        spy.syncCourseBatch("do_11282833211754086411", null);
    }
}
