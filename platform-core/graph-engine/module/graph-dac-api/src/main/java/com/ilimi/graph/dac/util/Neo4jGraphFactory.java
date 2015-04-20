package com.ilimi.graph.dac.util;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.io.fs.FileUtils;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;

import com.ilimi.graph.common.exception.ClientException;
import com.ilimi.graph.common.exception.ServerException;
import com.ilimi.graph.common.mgr.Configuration;
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
                graphDbMap.put(graphId, graphDb);
            }
            if (null != graphDb && graphDb.isAvailable(0))
                return graphDb;
        }
        throw new ClientException(GraphDACErrorCodes.ERR_DAC_GRAPH_NOT_FOUND_004.name(), "Graph database: " + graphId + " not found");
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
                throw new ServerException(GraphDACErrorCodes.ERR_DAC_DELETE_GRAPH_EXCEPTION.name(), e.getMessage(), e);
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
