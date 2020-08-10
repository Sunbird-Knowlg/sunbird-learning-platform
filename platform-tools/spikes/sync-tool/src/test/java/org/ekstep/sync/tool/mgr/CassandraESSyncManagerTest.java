package org.ekstep.sync.tool.mgr;

import java.util.Arrays;
import java.util.List;

import org.ekstep.sync.tool.util.DialcodeSync;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DialcodeSync.class})
@PowerMockIgnore({ "javax.management.*", "sun.security.ssl.*", "javax.net.ssl.*", "javax.crypto.*" })
public class CassandraESSyncManagerTest {

	@Test
    public void testsyncDialcodesByIds() throws Exception {
        DialcodeSync dialcodeSync = PowerMockito.mock(DialcodeSync.class);
        PowerMockito.when(dialcodeSync.sync(Mockito.anyList())).thenReturn(1);
        List<String> dialcodes = Arrays.asList("A1B2C3");
        
        CassandraESSyncManager cassandraESSyncManager = new CassandraESSyncManager(dialcodeSync);
        cassandraESSyncManager.syncDialcodesByIds(dialcodes);
    }


}
