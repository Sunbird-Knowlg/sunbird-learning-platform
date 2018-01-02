package org.ekstep.cassandra.update.cassandra_update;

import java.util.List;

import org.ekstep.cassandra.connector.util.CassandraConnector;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
		Session session = CassandraConnector.getSession();

		String insert = "insert into test_script_store.script_data (name, type, reqmap) values (?,?,?)";

		ResultSet rs = session.execute("SELECT name, type, reqmap from script_store.script_data");
		List<Row> rows = rs.all();
		System.out.println("Before ::::::::::::::::::::::::::::::::::");
		System.out.println("Count :" + rows.size());
		PreparedStatement pstmt = session.prepare(insert);

		for (Row row : rows) {

			System.out.println("Name :" + row.getString(0) + " |\\| REqMap : " + row.getString(2));

			session.execute(pstmt.bind(row.getString(0), row.getString(1),
					row.getString(2).replaceAll("com.ilimi", "org.ekstep")));

		}

		System.out.println("\n");
		rs = null;

		rs = session.execute("SELECT name, type, reqmap from test_script_store.script_data");
		rows = rs.all();
		System.out.println("After ::::::::::::::::::::::::::::::::::");
		System.out.println("Count :" + rows.size());
		for (Row row : rows) {

			System.out.println("Name :" + row.getString(0) + " |\\| REqMap : " + row.getString(2));
		}
		CassandraConnector.close();
    }
}
