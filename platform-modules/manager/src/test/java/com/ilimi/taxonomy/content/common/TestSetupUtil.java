package com.ilimi.taxonomy.content.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.TransactionTerminatedException;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

import com.ilimi.taxonomy.mgr.impl.TaxonomyManagerImpl;

/**
 * TestSetupUtil will do the initial configuration required for
 * ContentManagerImplUploadContentTest
 * 
 * @author gauraw
 *
 */
public class TestSetupUtil {

	static ClassLoader classLoader = TestSetupUtil.class.getClassLoader();
	static File definitionLocation = new File(classLoader.getResource("definitions/").getFile());
	static File literacyLocation = new File(classLoader.getResource("literacy/").getFile());

	private static Map<String, String> definitions = new HashMap<String, String>();
	private static TaxonomyManagerImpl taxonomyMgr = new TaxonomyManagerImpl();
	private static GraphDatabaseService graphDb = null;

	private static String NEO4J_SERVER_ADDRESS = "localhost:7687";
	private static String GRAPH_DIRECTORY_PROPERTY_KEY = "graph.dir";
	private static String BOLT_ENABLED = "true";

	@AfterClass
	public static void afterTest() {
		tearEmbeddedNeo4JSetup();
	}

	@BeforeClass
	public static void before() {
		setupEmbeddedNeo4J();
	}

	private static Response create(String graphId, InputStream in) {
		Response resp = null;
		try {
			resp = taxonomyMgr.create(graphId, in);
			if (!resp.getParams().getStatus().equalsIgnoreCase(TestParams.successful.name())) {
				System.out.println(resp.getParams().getErr() + resp.getParams().getErrmsg());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	private static Response createDefinition(String graphId, String objectType) {
		Response resp = null;
		try {
			resp = taxonomyMgr.updateDefinition(graphId, objectType);
			if (!resp.getParams().getStatus().equalsIgnoreCase(TestParams.successful.name())) {
				System.out.println(resp.getParams().getErr() + resp.getParams().getErrmsg());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resp;
	}

	private static InputStream csvReader(String file) {
		InputStream in = null;
		try {
			in = new FileInputStream(new File(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return in;
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

	private static void loadAllNodes() {
		InputStream in = csvReader(literacyLocation + "/literacy_concepts.csv");
		create(Platform.config.getString(TestParams.graphId.name()), in);
		InputStream in1 = csvReader(literacyLocation + "/literacy_dimensions.csv");
		create(Platform.config.getString(TestParams.graphId.name()), in1);
		InputStream in2 = csvReader(literacyLocation + "/literacy_domain.csv");
		create(Platform.config.getString(TestParams.graphId.name()), in2);
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
			loadAllNodes();
			tx.success();
		} catch (TransactionTerminatedException ignored) {
			System.out.println("Execption Occured while setting Embedded Neo4j : " + ignored);
		}
	}

	private static void tearEmbeddedNeo4JSetup() {
		graphDb.shutdown();
		deleteEmbeddedNeo4j(new File(Platform.config.getString(GRAPH_DIRECTORY_PROPERTY_KEY)));
	}

	private static void deleteEmbeddedNeo4j(final File emDb) {
		System.out.println(emDb.getAbsolutePath().toString());
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
