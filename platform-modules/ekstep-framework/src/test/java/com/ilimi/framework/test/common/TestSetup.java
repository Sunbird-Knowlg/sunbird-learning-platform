/**
 * 
 */
package com.ilimi.framework.test.common;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.TransactionTerminatedException;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

import com.ilimi.common.Platform;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.graph.common.enums.GraphEngineParams;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.engine.router.ActorBootstrap;
import com.ilimi.graph.engine.router.GraphEngineActorPoolMgr;
import com.ilimi.graph.engine.router.GraphEngineManagers;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

/**
 * @author pradyumna
 *
 */
@Ignore
public class TestSetup {

	static ClassLoader classLoader = TestSetup.class.getClassLoader();
	static File definitionLocation = new File(classLoader.getResource("definitions/").getFile());

	private static Map<String, String> definitions = new HashMap<String, String>();
	private static GraphDatabaseService graphDb = null;

	private static String NEO4J_SERVER_ADDRESS = "localhost:7687";
	private static String GRAPH_DIRECTORY_PROPERTY_KEY = "graph.dir";
	private static String BOLT_ENABLED = "true";
	private static ActorRef reqRouter = null;

	static long timeout = 50000;
	static Timeout t = new Timeout(Duration.create(30, TimeUnit.SECONDS));

	private static ActorRef initReqRouter() throws Exception {
		ActorBootstrap.getActorSystem();
		ActorRef reqRouter = GraphEngineActorPoolMgr.getRequestRouter();
		Thread.sleep(2000);
		System.out.println("Request Router: " + reqRouter);
		return reqRouter;
		/*ActorSystem system = ActorSystem.create("MySystem");
		ActorRef reqRouter = system.actorOf(Props.create(RequestRouter.class));
		
		Future<Object> future = Patterns.ask(reqRouter, "init", timeout);
		Object response = Await.result(future, t.duration());
		Thread.sleep(2000);
		System.out.println("Response from request router: " + response);
		return reqRouter;*/
	}

	@AfterClass
	public static void afterTest() throws Exception {
		tearEmbeddedNeo4JSetup();
		Thread.sleep(5000);
	}

	@BeforeClass
	public static void before() throws Exception {
		tearEmbeddedNeo4JSetup();
		reqRouter = initReqRouter();
		setupEmbeddedNeo4J();
	}

	private static Response createDefinition(String graphId, String objectType) {
		Response resp = null;
		try {
			Request request = new Request();
			request.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
			request.setManagerName(GraphEngineManagers.NODE_MANAGER);
			request.setOperation("importDefinitions");
			request.put(GraphEngineParams.input_stream.name(), objectType);
			Future<Object> response = Patterns.ask(reqRouter, request, timeout);

			Object obj = Await.result(response, t.duration());

			resp = (Response) obj;
			System.out.println(resp.getResponseCode() + "    ::    " + resp.getParams().getStatus());
			if (!resp.getParams().getStatus().equalsIgnoreCase(TestParams.successful.name())) {
				System.out.println(resp.getParams().getErr() + resp.getParams().getErrmsg());
			}
		} catch (Exception e) {
			e.printStackTrace();
	}
		return resp;
	}


	private static Map<String, String> loadAllDefinitions(File folder) {
		for (File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				loadAllDefinitions(fileEntry);
			} else {
				String definition;
				try {
					definition = FileUtils.readFileToString(fileEntry);
					Response resp = createDefinition(Platform.config.getString(TestParams.graphId.name()), definition);
					definitions.put(fileEntry.getName(), resp.getResponseCode().toString());
				} catch (IOException e) {
					e.printStackTrace();
			}
			}
	}
		return definitions;
	}

	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}

	private static void setupEmbeddedNeo4J() {
		GraphDatabaseSettings.BoltConnector bolt = GraphDatabaseSettings.boltConnector("0");

		graphDb = new GraphDatabaseFactory()
				.newEmbeddedDatabaseBuilder(new File(Platform.config.getString(GRAPH_DIRECTORY_PROPERTY_KEY)))
				.setConfig(bolt.type, TestParams.BOLT.name()).setConfig(bolt.enabled, BOLT_ENABLED)
				.setConfig(bolt.address, NEO4J_SERVER_ADDRESS).newGraphDatabase();
		registerShutdownHook(graphDb);

		try (Transaction tx = graphDb.beginTx()) {
			definitions = loadAllDefinitions(definitionLocation);
			tx.success();
		} catch (TransactionTerminatedException ignored) {
			System.out.println("Execption Occured while setting Embedded Neo4j : " + ignored);
	}
	}

	private static void tearEmbeddedNeo4JSetup() throws Exception {
		if (null != graphDb)
			graphDb.shutdown();
		Thread.sleep(5000);
		deleteEmbeddedNeo4j(new File(Platform.config.getString(GRAPH_DIRECTORY_PROPERTY_KEY)));
	}

	private static void deleteEmbeddedNeo4j(final File emDb) {
		if (emDb.exists()) {
			if (emDb.isDirectory()) {
				for (File child : emDb.listFiles()) {
					deleteEmbeddedNeo4j(child);
			}
		}
			try {
				emDb.delete();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	}
}
