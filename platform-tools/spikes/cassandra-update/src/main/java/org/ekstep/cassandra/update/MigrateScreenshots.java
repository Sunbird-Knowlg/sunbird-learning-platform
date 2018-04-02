/**
 * 
 */
package org.ekstep.cassandra.update;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.cassandra.connector.util.CassandraConnector;
import org.ekstep.cassandra.store.CassandraStore;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.mgr.impl.Neo4JBoltSearchMgrImpl;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author pradyumna
 *
 */
public class MigrateScreenshots extends CassandraStore {

	private Neo4JBoltSearchMgrImpl neo4jService = new Neo4JBoltSearchMgrImpl();
	private ObjectMapper mapper = new ObjectMapper();

	/**
	 * 
	 */
	public MigrateScreenshots(String type) {
		super();
		String keyspace = Platform.config.getString("content.keyspace");
		initialise(keyspace, keyspace + ".content_data", type, true);
		nodeType = "DATA_NODE";
	}

	public static void main(String[] args) throws Exception {
		MigrateScreenshots obj = new MigrateScreenshots(args[0]);
		Map<String, Object> screenshotData = obj.getDatafromNeo4j(args[0]);
		obj.updateToCassandra(screenshotData);
		// obj.removeFromNeo4j(screenshotData.keySet(), args[0]);
	}

	/**
	 * @param keySet
	 */
	private void removeFromNeo4j(Set<String> keySet, String type) {
		String deleteQuery = "Match (n:domain{IL_FUNC_OBJECT_TYPE:'" + type
				+ "'}) where exists(n.screenshots) REMOVE n.screenshots RETURN n.IL_FUNC_OBJECT_TYPE";
		Response res = executeQuery(deleteQuery);
		
		if (StringUtils.equalsIgnoreCase("successful", res.getParams().getStatus())) {
			System.out.println("Succesfully Removed screenshots");
		}
	}

	/**
	 * @param screenshotData
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private void updateToCassandra(Map<String, Object> screenshotData) throws Exception {
		Session session = CassandraConnector.getSession();
		
		String fromKeyspace = Platform.config.getString("content.keyspace");

		String selectQuery = "select count(screenshots) as count from " + fromKeyspace + ".content_data;";

		ResultSet results = session.execute(selectQuery);
		System.out.println("Before Update Cassandra Count : " + results.one().getObject("count"));

		String query = "update " + fromKeyspace + ".content_data SET screenshots = textAsBlob(?) where content_id=?";
		PreparedStatement statement = session.prepare(query);
		BoundStatement boundStatement = new BoundStatement(statement);
		int count = 0;
		for (String id : screenshotData.keySet()) {
			List<String> screenshots = validateScreenshotdata((List<String>) screenshotData.get(id));
			if (!screenshots.isEmpty()) {
				session.execute(boundStatement.bind(mapper.writeValueAsString(screenshots), id));
				System.out.println("Updated Id : " + id);
				count++;
				Map<String, Object> requestMap = new HashMap<String, Object>();

				requestMap.put("screenshots", screenshots);
			} else {
				System.out.println("Ignored ID :: " + id + " because screenshots is : : " + screenshotData.get(id));
			}

		}
		System.out.println("Updated to cassandra : : " + count);

		results = session.execute(selectQuery);
		System.out.println("After Update Cassandra Count : " + results.one().getObject("count"));
	}

	/**
	 * @param asList
	 * @return
	 * @throws Exception
	 */
	private List<String> validateScreenshotdata(List<String> screenshotsList) throws Exception {
		List<String> result = new ArrayList<String>();
		for (String screenshot : screenshotsList) {
			if (StringUtils.isNotBlank(screenshot)) {
				result.add(screenshot);
			}
		}
		return result;
	}

	/**
	 * @return
	 */

	private Response executeQuery(String query) {
		Request request = new Request();
		request.getContext().put(GraphDACParams.graph_id.name(), "domain");
		request.put(GraphDACParams.query.name(), query);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("lastUpdatedOn", "DESC");
		request.put(GraphDACParams.params.name(), params);
		Response res = neo4jService.executeQuery(request);

		return res;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getDatafromNeo4j(String type) {

		String query = "Match (n:domain{IL_FUNC_OBJECT_TYPE:'" + type
				+ "'}) where exists(n.screenshots) return n.IL_UNIQUE_ID as identifier, n.screenshots as screenshots;";
		Response res = executeQuery(query);

		List<Map<String, Object>> resultMap = (List<Map<String, Object>>) res.get(GraphDACParams.results.name());
		Map<String, Object> result = new HashMap<String, Object>();
		if (null != resultMap && !resultMap.isEmpty()) {
			for (Map<String, Object> map : resultMap) {
				if (null != map && !map.isEmpty())
					result.put((String) map.get("identifier"), map.get("screenshots"));
			}
		}
		System.out.println("Size : : " + resultMap.size());
		return result;
	}

}
