package org.ekstep.graph.service.factory;

import org.ekstep.graph.service.IGraphDatabaseService;
import org.ekstep.graph.service.impl.Neo4JBoltImpl;

public class GraphServiceFactory {

	private static IGraphDatabaseService service = new Neo4JBoltImpl();

	public static IGraphDatabaseService getDatabaseService() {
		return service;
	}
}
