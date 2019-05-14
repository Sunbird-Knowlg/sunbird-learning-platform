package org.sunbird.lp.content;

/**
 * Connects to Neo4j db, finds contents with size value not present and updates the size for each such content
 * if the artifactUrl or downloadUrl is present 
 * 
 * @author pritha
 *
 */
public class App {
	public static void main(String[] args) throws Exception {
		int totolNoRecdUdated = 0;
		// Connect to a neo4j instance
		Neo4jDBClient dbclient = new Neo4jDBClient("bolt://localhost:7687", "", "");

		int number = 10000;
		try {
			number = new Integer(args[0]);
		} catch (Exception e) {
			System.out.println("WARN: invalid commandline argument, default limit is 100!!");
		}
		int batchCount = 0 ;
		while(dbclient.run(number) > 0 ) {
			long starts = System.currentTimeMillis();
			dbclient.updateAllContentSize();
			long ends = System.currentTimeMillis();
			
			System.out.println(batchCount+1 +" Nth batch update in time: "+ (ends-starts)+"ms");
			totolNoRecdUdated = totolNoRecdUdated + dbclient.runNumber();
		}

		System.out.println("total number of records updated: "+totolNoRecdUdated );
		//process completed, closing connection 
		dbclient.close();
		

	}

}
