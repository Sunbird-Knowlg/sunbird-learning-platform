package com.ilimi.graph.dac.util;

import java.util.HashMap;
import java.util.Map;

import org.ekstep.graph.service.common.GraphOperation;
import org.ekstep.graph.service.request.validaor.Neo4JBoltDataVersionKeyValidator;
import org.ekstep.graph.service.util.DriverUtil;
import org.junit.Assert;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

import com.ilimi.graph.dac.model.Node;



public class Neo4jBoltDataVersionKeyValidatorTest {

	
	//@Test
	public void test(){
		Map<String, Object> nodeMeta = getNeo4jNodeProperty("en", "44347");
		System.out.println(nodeMeta);
		Node node =new Node("en", nodeMeta);
		Neo4JBoltDataVersionKeyValidator validator = new Neo4JBoltDataVersionKeyValidator();
		Assert.assertEquals(true, validator.validateUpdateOperation("en", node));
		
		Map<String, Object> nodeMeta2 = getNeo4jNodeProperty("domain", "org.ekstep.nov23.story.2");
		System.out.println(nodeMeta2);
		node =new Node("domain", nodeMeta2);
		Assert.assertEquals(true, validator.validateUpdateOperation("domain", node));
		Map<String, Object> md = new HashMap<>(node.getMetadata());
		md.put("versionKey", "1479206180");
		node.setMetadata(md);
		try{
			validator.validateUpdateOperation("domain", node);	
		}catch(Exception e){
		
		}
		
	
	}
	
	private Map<String, Object> getNeo4jNodeProperty(String graphId, String identifier) {
		Map<String, Object> prop = null;
		Driver driver = DriverUtil.getDriver(graphId, GraphOperation.READ);
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
				e.printStackTrace();
			} finally {
			}
		}
		return prop;

	}
}
