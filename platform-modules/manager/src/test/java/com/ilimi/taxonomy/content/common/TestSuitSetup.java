package com.ilimi.taxonomy.content.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.cassandraunit.CassandraCQLUnit;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.TransactionTerminatedException;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.ilimi.common.dto.Response;
import com.ilimi.graph.common.mgr.Configuration;
import com.ilimi.taxonomy.mgr.impl.TaxonomyManagerImpl;

public class TestSuitSetup {

	@Rule
	public static CassandraCQLUnit cassandra;

	private static File folder = new File("src/test/resources/definitions");
	private static Map<String, String> definitions = new HashMap<String, String>();
	private static TaxonomyManagerImpl taxonomyMgr = new TaxonomyManagerImpl();
	private static GraphDatabaseService graphDb = null;

	private static String CASSANDRA_KEYSPACE = "unit_test_content_store";
	private static String CASSANDRA_DDL_CQL_FILE = "db/cassandra_keystore_ddl.cql";

	@AfterClass
	public static void afterTest() {
		tearEmbeddedNeo4JSetup();
		tearEmbeddedCassandraSetup();
	}

	@BeforeClass
	public static void before() {
		setupEmbeddedNeo4J();
		setupEmbeddedCassandra();
	}

	private static void clearEmbeddedCassandraTables() {
		Collection<TableMetadata> tables = cassandra.cluster.getMetadata().getKeyspace(CASSANDRA_KEYSPACE).getTables();
		tables.forEach(table -> cassandra.session.execute(QueryBuilder.truncate(table)));
	}

	private static Response create(String graphId, InputStream in) {
		Response resp = null;
		try {
			resp = taxonomyMgr.create(graphId, in);
			if (!resp.getParams().getStatus().equalsIgnoreCase("successful")) {
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
			if (!resp.getParams().getStatus().equalsIgnoreCase("successful")) {
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

	private static Response deleteGraph(String graphId) {
		Response resp = null;
		try {
			resp = taxonomyMgr.delete(graphId);
			if (!resp.getParams().getStatus().equalsIgnoreCase("successful")) {
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
					Response resp = createDefinition(Configuration.getProperty("graphId"), definition);
					definitions.put(fileEntry.getName(), resp.getResponseCode().toString());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return definitions;
	}

	private static void loadAllNodes() {
		System.out.println("creating sample nodes for test");
		InputStream in = csvReader("src/test/resources/literacy/literacy_concepts.csv");
		create(Configuration.getProperty("graphId"), in);
		InputStream in1 = csvReader("src/test/resources/literacy/literacy_dimensions.csv");
		create(Configuration.getProperty("graphId"), in1);
		InputStream in2 = csvReader("src/test/resources/literacy/literacy_domain.csv");
		create(Configuration.getProperty("graphId"), in2);
	}

	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}

	private static void setupEmbeddedCassandra() {
		try {
			ClassPathCQLDataSet dataSet = new ClassPathCQLDataSet(CASSANDRA_DDL_CQL_FILE, true, true,
					CASSANDRA_KEYSPACE);
			cassandra = new CassandraCQLUnit(dataSet);
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}

	private static void setupEmbeddedNeo4J() {
		GraphDatabaseSettings.BoltConnector bolt = GraphDatabaseSettings.boltConnector("0");
		System.out.println("Starting neo4j in embedded mode");

		graphDb = new GraphDatabaseFactory()
				.newEmbeddedDatabaseBuilder(new File(Configuration.getProperty("graph.dir")))
				.setConfig(bolt.type, "BOLT").setConfig(bolt.enabled, "true").setConfig(bolt.address, "localhost:7687")
				.newGraphDatabase();
		registerShutdownHook(graphDb);

		try (Transaction tx = graphDb.beginTx()) {
			System.out.println("Loading All Definitions...!!");
			definitions = loadAllDefinitions(folder);
			loadAllNodes();
			tx.success();
		} catch (TransactionTerminatedException ignored) {
			System.out.println("Execption" + ignored);
		}
	}

	private static void tearEmbeddedCassandraSetup() {
		clearEmbeddedCassandraTables();
	}

	private static void tearEmbeddedNeo4JSetup() {
		System.out.println("deleting Graph...!!");
		graphDb.shutdown();
		deleteGraph(Configuration.getProperty("graphId"));
	}
}
