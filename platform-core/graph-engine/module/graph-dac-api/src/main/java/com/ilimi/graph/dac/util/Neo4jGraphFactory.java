package com.ilimi.graph.dac.util;

import static com.ilimi.graph.dac.util.Neo4jGraphUtil.NODE_LABEL;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.io.fs.FileUtils;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;

import com.ilimi.common.exception.ClientException;
import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.common.mgr.Configuration;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.exception.GraphDACErrorCodes;

public class Neo4jGraphFactory {

    private static Map<String, GraphDatabaseService> graphDbMap = new HashMap<String, GraphDatabaseService>();

    private static String graphDbPath = "/data/graphDB";

    static {
        try {
            InputStream inputStream = Configuration.class.getClassLoader().getResourceAsStream("graph.properties");
            if (null != inputStream) {
                Properties props = new Properties();
                props.load(inputStream);
                String graphDir = props.getProperty("graph.dir");
                if (StringUtils.isNotBlank(graphDir)) {
                    File f = new File(graphDir);
                    if (!f.exists()) {
                        System.out.println(f.mkdirs());
                    }
                    graphDbPath = graphDir;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized GraphDatabaseService getGraphDb(String graphId) {
        if (StringUtils.isNotBlank(graphId)) {
            GraphDatabaseService graphDb = graphDbMap.get(graphId);
            if (null == graphDb || !graphDb.isAvailable(0)) {
                if (null != graphDb) {
                    graphDbMap.remove(graphId);
                }
                graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(graphDbPath + File.separator + graphId)
                        .setConfig(GraphDatabaseSettings.allow_store_upgrade, "true").newGraphDatabase();
                registerShutdownHook(graphDb);
                Neo4JTransactionEventHandler.registerTransactionEventForGraph(graphDb);
                graphDbMap.put(graphId, graphDb);
                createConstraints(graphDb);
            }
            if (null != graphDb && graphDb.isAvailable(0))
                return graphDb;
        }
        throw new ClientException(GraphDACErrorCodes.ERR_GRAPH_NOT_FOUND.name(), "Graph database: " + graphId + " not found");
    }
    
    public static void createConstraints(GraphDatabaseService graphDb) {
        Transaction tx = null;
        try {
            tx = graphDb.beginTx();
            Schema schema = graphDb.schema();
            schema.indexFor(NODE_LABEL).on(SystemProperties.IL_SYS_NODE_TYPE.name()).create();
            schema.indexFor(NODE_LABEL).on(SystemProperties.IL_FUNC_OBJECT_TYPE.name()).create();
            schema.constraintFor(NODE_LABEL).assertPropertyIsUnique(SystemProperties.IL_UNIQUE_ID.name()).create();
            tx.success();
        } catch (Exception e) {
            if (null != tx)
                tx.failure();
        } finally {
            if (null != tx)
                tx.close();
        }
    }

    public static void createGraph(String graphId) {
        File f = new File(graphDbPath + File.separator + graphId);
        if (!f.exists()) {
            f.mkdirs();
        }
    }

    public static void deleteGraph(String graphId) {
        File f = new File(graphDbPath + File.separator + graphId);
        if (f.exists()) {
            try {
                FileUtils.deleteRecursively(f);
            } catch (Exception e) {
                throw new ServerException(GraphDACErrorCodes.ERR_GRAPH_DELETE_UNKNOWN_ERROR.name(), e.getMessage(), e);
            }
        }
    }

    public static boolean graphExists(String graphId) {
        File f = new File(graphDbPath + File.separator + graphId);
        if (f.exists()) {
            return true;
        }
        return false;
    }

    public static void shutdownGraph(String graphId) {
        if (StringUtils.isNotBlank(graphId)) {
            GraphDatabaseService graphDb = graphDbMap.get(graphId);
            if (null != graphDb) {
                if (graphDb.isAvailable(0))
                    graphDb.shutdown();
                graphDbMap.remove(graphId);
            }
        }
    }

    public static BatchInserter getBatchInserter(String graphId) {
        BatchInserter inserter = BatchInserters.inserter(graphDbPath + File.separator + graphId);
        return inserter;
    }

    private static void registerShutdownHook(final GraphDatabaseService graphDb) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Shutting down graph db...");
                graphDb.shutdown();
            }
        });
    }

}
