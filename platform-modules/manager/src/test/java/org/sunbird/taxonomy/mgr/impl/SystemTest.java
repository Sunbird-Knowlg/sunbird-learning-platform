package org.sunbird.taxonomy.mgr.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.CassandraCQLUnit;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import com.datastax.driver.core.ResultSet;
@Ignore
public class SystemTest {

	@Rule
    public CassandraCQLUnit cassandraCQLUnit = new CassandraCQLUnit(new ClassPathCQLDataSet("sample.cql","TestKeyspace"));

    @Before
    public void setup() throws ConfigurationException, TTransportException, IOException{
    	 EmbeddedCassandraServerHelper.startEmbeddedCassandra("/cassandra.yaml", 100000L);
    }
    
    
    @Test
    public void should_have_started_and_execute_cql_script() throws Exception {
        ResultSet result = cassandraCQLUnit.session.execute("select * from student WHERE id='key1'");
        assertThat(result.iterator().next().getString("name"), is("Shukla"));
    }
    
}
