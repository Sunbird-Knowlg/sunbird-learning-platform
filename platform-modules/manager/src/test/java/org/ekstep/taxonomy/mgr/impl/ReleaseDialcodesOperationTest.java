package org.ekstep.taxonomy.mgr.impl;

import static org.junit.Assert.assertEquals;


import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.content.mgr.impl.operation.dialcodes.ReleaseDialcodesOperation;
import org.ekstep.learning.hierarchy.store.HierarchyStore;
import org.ekstep.taxonomy.util.DialCodeTestInputs;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;


public class ReleaseDialcodesOperationTest {

    private HierarchyStore hierarchyStoreMock;

    public ReleaseDialcodesOperation operation;


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

    @Ignore
    @Test
    public void testReleaseDialCode_2() throws Exception {
        Mockito.when(hierarchyStoreMock.getHierarchy(Mockito.anyString())).thenReturn(DialCodeTestInputs.getMap(DialCodeTestInputs.HIERARCHY_WITH_RESERVED_DC_2));
        Response response = operation.releaseDialCodes(DialCodeTestInputs.CONTENT_ID, DialCodeTestInputs.CHANNEL_ID);
        assertEquals(response.getResponseCode(), ResponseCode.OK);
    }

    @Ignore
    @Test
    public void testReleaseDialCode_1() throws Exception {
        Mockito.when(hierarchyStoreMock.getHierarchy(Mockito.anyString())).thenReturn(DialCodeTestInputs.getMap(DialCodeTestInputs.HIERARCHY_WITH_RESERVED_DC_1));
        Response response = operation.releaseDialCodes(DialCodeTestInputs.CONTENT_ID, DialCodeTestInputs.CHANNEL_ID);
        assertEquals(response.getResponseCode(), ResponseCode.OK);

    }
}
