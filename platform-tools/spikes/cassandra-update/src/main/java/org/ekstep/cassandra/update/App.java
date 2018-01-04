package org.ekstep.cassandra.update;

import java.util.List;

import org.ekstep.cassandra.connector.util.CassandraConnector;
import org.ekstep.common.Platform;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

/**
 * @author Pradyumna & Mahesh
 *
 */
public class App {
	public static void main(String[] args) {
		Session session = CassandraConnector.getSession();

		String fromKeyspace = Platform.config.getString("cassandra.keyspace.from");
		String toKeyspace = Platform.config.getString("cassandra.keyspace.to");

		String insert = "insert into " + toKeyspace + ".script_data (name, type, reqmap) values (?,?,?)";

		ResultSet rs = session.execute("SELECT name, type, reqmap from " + fromKeyspace + ".script_data");
		List<Row> rows = rs.all();
		System.out.println("Before ::::::::::::::::::::::::::::::::::");
		System.out.println("Count :" + rows.size());

		boolean testrun = true;
		if (Platform.config.hasPath("test.run"))
			testrun = Platform.config.getBoolean("test.run");

		if (!testrun) {
			PreparedStatement pstmt = session.prepare(insert);
			for (Row row : rows) {
				System.out.println("Name :" + row.getString(0) + " |\\| ReqMap : " + row.getString(2));
				session.execute(pstmt.bind(row.getString(0), row.getString(1),
						row.getString(2).replaceAll("com.ilimi", "org.ekstep")));
			}

			System.out.println("\n");
			rs = null;

			rs = session.execute("SELECT name, type, reqmap from " + toKeyspace + ".script_data");
			rows = rs.all();
			System.out.println("After ::::::::::::::::::::::::::::::::::");
			System.out.println("Count :" + rows.size());
			for (Row row : rows) {
				System.out.println("Name :" + row.getString(0) + " |\\| ReqMap : " + row.getString(2));
			}
		}
		CassandraConnector.close();
	}
}
