package org.sunbird.framework.test.common;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.datastax.driver.core.Session;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.sunbird.cassandra.connector.util.CassandraConnector;
import org.sunbird.common.Platform;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.graph.common.enums.GraphEngineParams;
import org.sunbird.graph.common.enums.GraphHeaderParams;
import org.sunbird.graph.engine.common.TestParams;
import org.sunbird.graph.engine.router.ActorBootstrap;
import org.sunbird.graph.engine.router.GraphEngineActorPoolMgr;
import org.sunbird.graph.engine.router.GraphEngineManagers;
import org.sunbird.graph.service.util.DriverUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.configuration.BoltConnector;
import org.springframework.test.web.servlet.ResultActions;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Common Base Test Setup Class for Neo4j and Cassandra
 *
 * @author Kumar Gauraw
 */
public class CommonTestSetup {

    private static ClassLoader classLoader = CommonTestSetup.class.getClassLoader();
    private static ObjectMapper mapper = new ObjectMapper();
    private static GraphDatabaseService graphDb = null;

    private static String NEO4J_SERVER_ADDRESS = "localhost:7687";
    private static String GRAPH_DIRECTORY_PROPERTY_KEY = "graph.dir";
    private static String BOLT_ENABLED = "true";

    protected static long timeout = 50000;
    protected static Timeout t = new Timeout(Duration.create(30, TimeUnit.SECONDS));
    protected static Set<String> graphIds = new HashSet<>();

    public static ActorRef reqRouter = null;
    public static Session session = null;


    @AfterClass
    public static void afterTest() throws Exception {
        DriverUtil.closeDrivers();
        tearEmbeddedNeo4JSetup();
        tearEmbeddedCassandraSetup();
    }

    @BeforeClass
    public static void before() throws Exception {
        ActorBootstrap.getActorSystem();
        reqRouter = GraphEngineActorPoolMgr.getRequestRouter();
        tearEmbeddedNeo4JSetup();
        tearEmbeddedCassandraSetup();
        setupEmbeddedNeo4J();
        setupEmbeddedCassandra();
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
        BoltConnector bolt = new BoltConnector("0");
        graphDb = new GraphDatabaseFactory()
                .newEmbeddedDatabaseBuilder(new File(Platform.config.getString(GRAPH_DIRECTORY_PROPERTY_KEY)))
                .setConfig(bolt.type, TestParams.BOLT.name()).setConfig(bolt.enabled, BOLT_ENABLED)
                .setConfig(bolt.listen_address, NEO4J_SERVER_ADDRESS).newGraphDatabase();
        registerShutdownHook(graphDb);
    }

    protected static void setupEmbeddedCassandra() {

        try {
            EmbeddedCassandraServerHelper.startEmbeddedCassandra("/cassandra-unit.yaml", 100000L);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static void executeScript(String... querys) {
        try {
            session = CassandraConnector.getSession();
            for (String query : querys) {
                session.execute(query);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void tearEmbeddedCassandraSetup() {
        try {
            if (null != session && !session.isClosed()) {
                session.close();
                EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void tearEmbeddedNeo4JSetup() throws Exception {
        if (null != graphDb)
            graphDb.shutdown();
        Thread.sleep(2000);
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

    protected static void loadDefinition(String... paths) throws Exception {
        graphIds.add("domain");
        if (null != paths && paths.length > 0) {
            for (String path : paths) {
                File file = new File(classLoader.getResource(path).getFile());
                String definition = FileUtils.readFileToString(file);
                createDefinition("domain", definition);
            }
        }
    }

    /**
     * @param graphId
     * @param definition
     * @throws Exception
     */
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
            System.out.println(resp.getParams().getErr() + " :: " + resp.getParams().getErrmsg());
        }
    }

    /**
     * @param actions
     * @return
     */
    public static Response getResponse(ResultActions actions) {
        String content = null;
        Response resp = null;
        try {
            content = actions.andReturn().getResponse().getContentAsString();
            if (StringUtils.isNotBlank(content))
                resp = mapper.readValue(content, Response.class);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resp;
    }

}
