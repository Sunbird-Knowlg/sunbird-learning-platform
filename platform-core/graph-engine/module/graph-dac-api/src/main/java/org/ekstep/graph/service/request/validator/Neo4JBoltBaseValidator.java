package org.ekstep.graph.service.request.validator;

import java.util.Map;

import org.ekstep.common.exception.ServerException;
import org.ekstep.common.logger.PlatformLogger;
import org.ekstep.graph.service.common.DACErrorCodeConstants;
import org.ekstep.graph.service.common.DACErrorMessageConstants;
import org.ekstep.graph.service.common.GraphOperation;
import org.ekstep.graph.service.util.DriverUtil;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

public class Neo4JBoltBaseValidator {
	
	
	protected Map<String, Object> getNeo4jNodeProperty(String graphId, String identifier) {
		Map<String, Object> prop = null;
		Driver driver = DriverUtil.getDriver(graphId,GraphOperation.READ);
		PlatformLogger.log("Driver Initialised. | [Graph Id: " + graphId + "]");
		try (Session session = driver.session()) {
			try (Transaction tx = session.beginTransaction()) {
				String query = "match (n:" + graphId + "{IL_UNIQUE_ID:'" + identifier + "'}) return (n) as result";
				StatementResult result = tx.run(query);
				if (result.hasNext()) {
					Record record = result.next();
					InternalNode node = (InternalNode) record.values().get(0).asObject();
					prop = node.asMap();
				}
				tx.success();
				tx.close();
			} catch (Exception e) {
				throw new ServerException(DACErrorCodeConstants.CONNECTION_PROBLEM.name(),
						DACErrorMessageConstants.CONNECTION_PROBLEM + " | " + e.getMessage());
			}
		}
		return prop;

	}

}
