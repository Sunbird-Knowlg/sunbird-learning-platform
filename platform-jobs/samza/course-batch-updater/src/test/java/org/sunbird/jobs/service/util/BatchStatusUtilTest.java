package org.sunbird.jobs.service.util;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sunbird.jobs.samza.service.util.CourseBatchUpdater;
import org.sunbird.jobs.samza.util.BatchStatusUtil;
import org.sunbird.jobs.samza.util.SunbirdCassandraUtil;

import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@Ignore
@RunWith(PowerMockRunner.class)
@PrepareForTest({CourseBatchUpdater.class})
@PowerMockIgnore({"javax.management.*", "sun.security.ssl.*", "javax.net.ssl.*" , "javax.crypto.*"})
public class BatchStatusUtilTest {

    @Test
    public void testupdateOngoingbatch() {
        mockStatic(SunbirdCassandraUtil.class);
        doNothing().doThrow(Exception.class).when(SunbirdCassandraUtil.class);

        //BatchStatusUtil.updateOnGoingBatch();
    }
}
