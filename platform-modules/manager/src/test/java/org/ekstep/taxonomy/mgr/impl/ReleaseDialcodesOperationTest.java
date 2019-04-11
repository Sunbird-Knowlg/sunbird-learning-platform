package org.ekstep.taxonomy.mgr.impl;

import org.codehaus.jackson.type.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.cassandra.store.CassandraStore;
import org.ekstep.common.dto.Response;
import org.ekstep.content.mgr.impl.operation.dialcodes.ReleaseDialcodesOperation;
import org.ekstep.learning.hierarchy.store.HierarchyStore;
import org.ekstep.taxonomy.util.DialCodeTestInputs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Map;

public class ReleaseDialcodesOperationTest {

    private HierarchyStore hierarchyStoreMock;

    public ReleaseDialcodesOperation operation;

    public ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() throws Exception {
        hierarchyStoreMock = Mockito.mock(HierarchyStore.class);
        operation = new ReleaseDialcodesOperation(hierarchyStoreMock);
    }

    @After
    public void tearDown() throws Exception {
        hierarchyStoreMock = null;
        operation = null;
    }


    @Test
    public void testReleaseDialCode_1() {
        try {
            Mockito.when(hierarchyStoreMock.getHierarchy(Mockito.anyString())).thenReturn(getMap(DialCodeTestInputs.HIERARCHY_WITH_RESERVED_DC_1));
            Response response = operation.releaseDialCodes(DialCodeTestInputs.CONTENT_ID, DialCodeTestInputs.CHANNEL_ID);
            System.out.println(response.getResult());
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testReleaseDialCode_2() {
        try {
            Mockito.when(hierarchyStoreMock.getHierarchy(Mockito.anyString())).thenReturn(getMap(DialCodeTestInputs.HIERARCHY_WITH_RESERVED_DC_2));
            Response response = operation.releaseDialCodes(DialCodeTestInputs.CONTENT_ID, DialCodeTestInputs.CHANNEL_ID);
            System.out.println(response.getResult());
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String,Object> getMap(String input) {
        try {
            if(StringUtils.isNotBlank(input) )
                return (mapper.readValue(input, new TypeReference<Map<String,Object>>(){})) ;
            else
                return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
