package org.sunbird.sync.tool.util;

import java.util.Arrays;
import java.util.Iterator;

import org.sunbird.cassandra.connector.util.CassandraConnector;
import org.sunbird.searchindex.elasticsearch.ElasticSearchUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.vividsolutions.jts.util.Assert;

@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor("CassandraConnector.class, org.sunbird.searchindex.elasticsearch.ElasticSearchUtil.class")
@PrepareForTest({Session.class, CassandraConnector.class, ElasticSearchUtil.class})
@PowerMockIgnore({ "javax.management.*", "sun.security.ssl.*", "javax.net.ssl.*", "javax.crypto.*" })
public class DialcodeSyncTest {

	@Test
	public void testSyncWrongDialcodes() throws Exception {
        ResultSet resultSet = null;
		
		Session session = PowerMockito.mock(Session.class);
		PowerMockito.when(session.execute(Mockito.anyString())).thenReturn(resultSet);
		
		
		PowerMockito.mockStatic(CassandraConnector.class);
		PowerMockito.when(CassandraConnector.getSession()).thenReturn(session);
		
		DialcodeSync dialcodeSync = new DialcodeSync();
        Assert.isTrue(dialcodeSync.sync(Arrays.asList("A1B2C3"), null) == 0);
    }
	
	@Test
	public void testSyncCorrectDialcodes() throws Exception {
        Row row = PowerMockito.mock(Row.class);
        PowerMockito.when(row.getString("identifier")).thenReturn("A1B2C3");
        PowerMockito.when(row.getString("channel")).thenReturn("channel1");
        PowerMockito.when(row.getString("publisher")).thenReturn("publisher1");
        PowerMockito.when(row.getString("batchCode")).thenReturn("batchcode1");
        PowerMockito.when(row.getString("status")).thenReturn("Draft");
        PowerMockito.when(row.getString("metadata")).thenReturn(null);
        PowerMockito.when(row.getString("generated_on")).thenReturn(null);
        PowerMockito.when(row.getString("published_on")).thenReturn(null);
        
        Iterator itr = PowerMockito.mock(Iterator.class);
        PowerMockito.when(itr.hasNext()).thenReturn(true).thenReturn(false);
        PowerMockito.when(itr.next()).thenReturn(row);
        
        ResultSet resultSet = PowerMockito.mock(ResultSet.class);
        PowerMockito.when(resultSet.iterator()).thenReturn(itr);
        	
			
        
		Session session = PowerMockito.mock(Session.class);
		PowerMockito.when(session.execute(Mockito.anyString())).thenReturn(resultSet);
		
		
		PowerMockito.mockStatic(CassandraConnector.class);
		PowerMockito.when(CassandraConnector.getSession()).thenReturn(session);
		
		PowerMockito.mockStatic(ElasticSearchUtil.class);
		PowerMockito.doNothing().when(ElasticSearchUtil.class); 
		
		
		DialcodeSync dialcodeSync = new DialcodeSync();
        Assert.isTrue(dialcodeSync.sync(Arrays.asList("A1B2C3"),null) == 1);
    }
}
