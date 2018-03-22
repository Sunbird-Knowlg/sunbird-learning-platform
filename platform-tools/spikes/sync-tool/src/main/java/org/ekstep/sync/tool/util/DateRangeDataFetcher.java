package org.ekstep.sync.tool.util;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

public class DateRangeDataFetcher {

	public List<String> neo4jData(String objectType, String startDate, String endDate) {
		List<String> identifiers = new ArrayList<>();
		Driver driver = GraphDatabase.driver("bolt://localhost:7687");
		Session session = driver.session();
		try{
			StatementResult result = session.run("MATCH (n:domain {IL_FUNC_OBJECT_TYPE:'"+objectType+"'}) WHERE n.lastUpdatedOn>= '"+startDate+"' AND n.lastUpdatedOn<='"+endDate+"' return n.IL_UNIQUE_ID");
			while(result.hasNext()){
				Record record = result.next();
				identifiers.add(record.get("n.IL_UNIQUE_ID").asString());
			}
		}
		catch(Exception e){
		}
		finally{
			session.close();
			driver.close();
		}
		return identifiers;
	}
}
