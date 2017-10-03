package org.ekstep.jobs.samza.test;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.ekstep.learning.util.ControllerUtil;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

import com.ilimi.common.Platform;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.graph.common.enums.GraphEngineParams;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.engine.router.GraphEngineManagers;

import info.aduna.io.FileUtil;


abstract public class BaseTest {

	protected static ControllerUtil util = new ControllerUtil();
	private static GraphDatabaseService graphDb;
	protected static String graphId = "domain";

	private static File tempDir = null;
	private static Settings settings = null;
	protected static Node server = null;
	static String clusterName = null;
	protected static Client client = null;
	
	@BeforeClass
	public static void before(){
		GraphDatabaseSettings.BoltConnector bolt = GraphDatabaseSettings.boltConnector( "0" );
        System.out.println("Starting neo4j in embedded mode");
       
        graphDb = new GraphDatabaseFactory()
		        .newEmbeddedDatabaseBuilder(new File(Platform.config.getString("graph.dir")))
		        .setConfig( bolt.type, "BOLT" )
		        .setConfig( bolt.enabled, "true" )
		        .setConfig( bolt.address, "localhost:7687" )
		        .newGraphDatabase();
		registerShutdownHook(graphDb);
		
		try(Transaction tx = graphDb.beginTx()){
			System.out.println("Loading All Definitions...!!");
			loadAllDefinitions(new File("src/test/resources/definitions"), "domain");
		}
		
		tempDir = new File(System.getProperty("user.dir") + "/tmp");
		settings = Settings.builder()
				.put("path.home", tempDir.getAbsolutePath())
				.put("transport.tcp.port","9500")
				.build();
		server = NodeBuilder.nodeBuilder().settings(settings).build();
		clusterName = server.settings().get("cluster.name");
		server.start();
		client = server.client();
	}
	
	@AfterClass
	public static void after() throws IOException {
		System.out.println("deleting Graph...!!");
		graphDb.shutdown();
		deleteGraph(graphId);
		server.close();
		FileUtil.deleteDir(tempDir);
	}
	
	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}

	private static void loadAllDefinitions(File folder, String graphId) {
		for (File fileEntry : folder.listFiles()) {
			if (!fileEntry.isDirectory()) {
				String definition;
				try {
					definition = FileUtils.readFileToString(fileEntry);
					createDefinition(definition, graphId);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static void createDefinition(String contentString, String graph_id) throws IOException{
		Request request = new Request();
		request.setManagerName(GraphEngineManagers.NODE_MANAGER);
		request.setOperation("importDefinitions");
		request.getContext().put(GraphHeaderParams.graph_id.name(),graph_id);
		request.put(GraphEngineParams.input_stream.name(), contentString);
		Response response = util.getResponse(request);
		Assert.assertEquals("successful", response.getParams().getStatus());
	}
	
	private static void deleteGraph(String graphId) {
		try {
			Request request = new Request();
			request.setManagerName(GraphEngineManagers.GRAPH_MANAGER);
			request.setOperation("deleteGraph");
			request.getContext().put(GraphHeaderParams.graph_id.name(),
					graphId);
			Response resp = util.getResponse(
					request);
			if (!resp.getParams().getStatus().equalsIgnoreCase("successful")) {
					System.out.println(resp.getParams().getErr() + resp.getParams().getErrmsg());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
