package org.ekstep.sync.tool.shell;

import com.datastax.driver.core.Row;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ekstep.cassandra.connector.util.CassandraConnector;
import org.ekstep.sync.tool.mgr.BatchEnrolmentSyncManager;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CassandraConnector.class})
@PowerMockIgnore({ "javax.management.*", "sun.security.ssl.*", "javax.net.ssl.*", "javax.crypto.*" })
public class BatchEnrolmentSyncManagerTest extends GraphEngineTestSetup {

    private static ObjectMapper mapper = new ObjectMapper();
    @Mock Row row;
    
    @BeforeClass
    public static void beforeClass() throws Exception {
        loadDefinition("definitions/content_definition.json");
    }
    
    @Test
    public void testSyncBatch() throws Exception {
        Row row = PowerMockito.mock(Row.class);
        BatchEnrolmentSyncManager mgr = PowerMockito.spy(new BatchEnrolmentSyncManager());
        PowerMockito.doReturn(Arrays.asList(row)).when(mgr).readBatch(new String[]{"do_112693299606175744173"});
        PowerMockito.when(row.getInt("status")).thenReturn(1);
        PowerMockito.when(row.getString("name")).thenReturn("testbatch");
        PowerMockito.when(row.getString("batchid")).thenReturn("1023456");
        PowerMockito.when(row.getString("startdate")).thenReturn("2020-02-06");
        PowerMockito.when(row.getString("enddate")).thenReturn("2020-02-08");
        PowerMockito.when(row.getString("enrollmentenddate")).thenReturn(null);
        PowerMockito.when(row.getString("enrollmenttype")).thenReturn("open");
        PowerMockito.when(row.getList("createdfor", String.class)).thenReturn(new ArrayList<>());
        mgr.sync("batch-detail-update", null, null, null, null, new String[]{"do_112693299606175744173"});
    }
}
