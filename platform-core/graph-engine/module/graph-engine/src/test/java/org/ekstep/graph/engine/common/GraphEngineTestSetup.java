/**
 * 
 */
package org.ekstep.graph.engine.common;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.graph.common.enums.GraphEngineParams;
import org.ekstep.graph.common.enums.GraphHeaderParams;
import org.ekstep.graph.engine.router.ActorBootstrap;
import org.ekstep.graph.engine.router.GraphEngineActorPoolMgr;
import org.ekstep.graph.engine.router.GraphEngineManagers;
import org.ekstep.graph.service.util.DriverUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author pradyumna
 *
 */
public class GraphEngineTestSetup {

	private static ClassLoader classLoader = GraphEngineTestSetup.class.getClassLoader();

	private static GraphDatabaseService graphDb = null;

	private static String NEO4J_SERVER_ADDRESS = "localhost:7687";
	private static String GRAPH_DIRECTORY_PROPERTY_KEY = "graph.dir";
	private static String BOLT_ENABLED = "true";

	public static ActorRef reqRouter = null;

	protected static long timeout = 120000;
	protected static Timeout t = new Timeout(Duration.create(300, TimeUnit.SECONDS));
	protected static Set<String> graphIds = new HashSet<>();

	@AfterClass
	public static void afterTest() throws Exception {
		deleteGraph();
		DriverUtil.closeDrivers();
	}

	@BeforeClass
	public static void before() throws Exception {
		ActorBootstrap.getActorSystem();
		reqRouter = GraphEngineActorPoolMgr.getRequestRouter();
		setupEmbeddedNeo4J();
	}

	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					tearEmbeddedNeo4JSetup();
					System.out.println("cleanup Done!!");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private static void setupEmbeddedNeo4J() throws Exception {
		if(graphDb==null) {
			GraphDatabaseSettings.BoltConnector bolt = GraphDatabaseSettings.boltConnector("0");
			graphDb = new GraphDatabaseFactory()
					.newEmbeddedDatabaseBuilder(new File(Platform.config.getString(GRAPH_DIRECTORY_PROPERTY_KEY)))
					.setConfig(bolt.type, TestParams.BOLT.name()).setConfig(bolt.enabled, BOLT_ENABLED)
					.setConfig(bolt.address, NEO4J_SERVER_ADDRESS).newGraphDatabase();
			registerShutdownHook(graphDb);
		}
	}

	private static void tearEmbeddedNeo4JSetup() throws Exception {
		if (null != graphDb)
			graphDb.shutdown();
		Thread.sleep(2000);
		deleteEmbeddedNeo4j(new File(Platform.config.getString(GRAPH_DIRECTORY_PROPERTY_KEY)));
	}

	private static void deleteEmbeddedNeo4j(final File emDb) throws IOException {
		FileUtils.deleteDirectory(emDb);
	}

	protected static void deleteGraph() {

		try {			
			Iterator<String> graphIdsIterator = graphIds.iterator();
		     while(graphIdsIterator.hasNext()){
		    	 String graphId = graphIdsIterator.next();
		    	 Request request = new Request();
					request.setManagerName(GraphEngineManagers.GRAPH_MANAGER);
					request.setOperation("deleteGraph");
					request.getContext().put(GraphHeaderParams.graph_id.name(),
							graphId);
					Future<Object> response = Patterns.ask(reqRouter, request, timeout);
					Object obj = Await.result(response, t.duration());
					Response resp = (Response) obj;
					
					if (!resp.getParams().getStatus().equalsIgnoreCase("successful")) {
						System.out.println(resp.getParams().getErr() + resp.getParams().getErrmsg());
					}
		     }
	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected static void loadDefinition(String... paths) throws Exception {
		loadDefinitionByGraphId(Platform.config.getString(TestParams.graphId.name()), paths);
	}

	protected static void loadDefinitionByGraphId(String graphId, String... paths) throws Exception {
		graphIds.add(graphId);
		if (null != paths && paths.length > 0) {
			for (String path : paths) {
				InputStream is = classLoader.getResourceAsStream(path);
				String definition = IOUtils.toString(is);;
				createDefinition(graphId, definition);
			}
		}
	}
	
	private static void createDefinition(String graphId, String definition) throws Exception {
		Request request = new Request();
		request.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
		request.setManagerName(GraphEngineManagers.NODE_MANAGER);
		request.setOperation("importDefinitions");
		request.put(GraphEngineParams.input_stream.name(), definition);
		Future<Object> response = Patterns.ask(reqRouter, request, timeout);
		Object obj = Await.result(response, t.duration());

		Response resp = (Response) obj;
		if (!resp.getParams().getStatus().equalsIgnoreCase(TestParams.successful.name())) {
			
		}
	}

	/**
	 *
	 * @param time
	 */
	protected static void delay(long time) {
		try {
			Thread.sleep(time);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
