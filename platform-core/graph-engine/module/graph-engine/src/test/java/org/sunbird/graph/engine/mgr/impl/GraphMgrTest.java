package org.sunbird.graph.engine.mgr.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.graph.common.enums.GraphEngineParams;
import org.sunbird.graph.common.enums.GraphHeaderParams;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.model.SearchCriteria;
import org.sunbird.graph.engine.router.GraphEngineManagers;
import org.sunbird.graph.engine.router.RequestRouter;
import org.sunbird.graph.enums.ImportType;
import org.sunbird.graph.importer.InputStreamValue;
import org.sunbird.graph.importer.OutputStreamValue;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class GraphMgrTest {

    long timeout = 50000;
    Timeout t = new Timeout(Duration.create(30, TimeUnit.SECONDS));
    private static final Logger logger = LogManager.getLogger("PerformanceTestLogger");

    private ActorRef initReqRouter() throws Exception {
        ActorSystem system = ActorSystem.create("MySystem");
        ActorRef reqRouter = system.actorOf(Props.create(RequestRouter.class));

        Future<Object> future = Patterns.ask(reqRouter, "init", timeout);
        Object response = Await.result(future, t.duration());
        Thread.sleep(2000);
        logger.info("Response from request router: " + response);
        return reqRouter;
    }

//    @Test
    public void testImportGraph() {
        try {
            ActorRef reqRouter = initReqRouter();

            long t1 = System.currentTimeMillis();
            Request request = new Request();
            request.getContext().put(GraphHeaderParams.graph_id.name(), "TEMP_CS_004");
            request.setManagerName(GraphEngineManagers.GRAPH_MANAGER);
            request.setOperation("importGraph");
            request.put(GraphEngineParams.format.name(), ImportType.CSV.name());

            // Change the file path.
            InputStream inputStream = GraphMgrTest.class.getClassLoader().getResourceAsStream("A vocational education course.csv");

            request.put(GraphEngineParams.input_stream.name(), new InputStreamValue(inputStream));
            Future<Object> req = Patterns.ask(reqRouter, request, t);

            handleFutureBlock(req, "importGraph", GraphDACParams.graph_id.name());
            long t2 = System.currentTimeMillis();
            logger.info("Import Time: " + (t2 - t1));
            Thread.sleep(15000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    @Test
    public void testExportGraph() {
        try {
            ActorRef reqRouter = initReqRouter();
            Request request = new Request();
            request.getContext().put(GraphHeaderParams.graph_id.name(), "TEMP_CS_007");
            request.setManagerName(GraphEngineManagers.GRAPH_MANAGER);
            request.setOperation("exportGraph");
            request.put(GraphEngineParams.format.name(), ImportType.JSON.name());
            Future<Object> req = Patterns.ask(reqRouter, request, t);
            Object obj = Await.result(req, t.duration());
            Response response = (Response) obj;
            OutputStreamValue osV = (OutputStreamValue) response.get(GraphEngineParams.output_stream.name());
            ByteArrayOutputStream os = (ByteArrayOutputStream) osV.getOutputStream();
            FileUtils.writeByteArrayToFile(new File("A vocational education course.json"), os.toByteArray());
            logger.info("Result: \n" + new String(os.toByteArray())); // Prints
                                                                             // the
                                                                             // string
                                                                             // content
                                                                             // read
                                                                             // from
                                                                             // input
                                                                             // stream

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    @Test
    public void testLoadGraph() {
        try {
            ActorRef reqRouter = initReqRouter();
            long t1 = System.currentTimeMillis();
            Request request = new Request();
            request.getContext().put(GraphHeaderParams.graph_id.name(), "JAVA_CS");
            request.setManagerName(GraphEngineManagers.GRAPH_MANAGER);
            request.setOperation("loadGraph");

            Future<Object> req = Patterns.ask(reqRouter, request, t);
            Object arg1 = Await.result(req, t.duration());
            long t2 = System.currentTimeMillis();
            logger.info("Load Time: " + (t2 - t1));
            ObjectMapper mapper = new ObjectMapper();
            logger.info("Result:" + mapper.writeValueAsString(arg1));

            Thread.sleep(15000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    @Test
    public void testValidateGraph() {
        try {
            ActorRef reqRouter = initReqRouter();
            long t1 = System.currentTimeMillis();
            Request request = new Request();
            request.getContext().put(GraphHeaderParams.graph_id.name(), "JAVA_CS");
            request.setManagerName(GraphEngineManagers.GRAPH_MANAGER);
            request.setOperation("validateGraph");

            Future<Object> req = Patterns.ask(reqRouter, request, t);
            Object arg1 = Await.result(req, t.duration());
            long t2 = System.currentTimeMillis();
            logger.info("Validate Time: " + (t2 - t1));
            ObjectMapper mapper = new ObjectMapper();
            logger.info("Result:" + mapper.writeValueAsString(arg1));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    @Test
    public void testCreateGraph() {
        try {
            ActorRef reqRouter = initReqRouter();

            String graph1 = "Graph1_" + System.currentTimeMillis();
            Future<Object> req1 = createGraph(reqRouter, graph1);

            String graph2 = "Graph2_" + System.currentTimeMillis();
            Future<Object> req2 = createGraph(reqRouter, graph2);

            String graph3 = "Graph3_" + System.currentTimeMillis();
            Future<Object> req3 = createGraph(reqRouter, graph3);

            handleFutureBlock(req1, "createGraph", GraphDACParams.graph_id.name());
            handleFutureBlock(req2, "createGraph", GraphDACParams.graph_id.name());
            handleFutureBlock(req3, "createGraph", GraphDACParams.graph_id.name());

            Future<Object> del1 = deleteGraph(reqRouter, graph1);
            Future<Object> del2 = deleteGraph(reqRouter, graph2);
            Future<Object> del3 = deleteGraph(reqRouter, graph3);
            handleFutureBlock(del1, "deleteGraph", GraphDACParams.graph_id.name());
            handleFutureBlock(del2, "deleteGraph", GraphDACParams.graph_id.name());
            handleFutureBlock(del3, "deleteGraph", GraphDACParams.graph_id.name());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //@Test
    public void testCreateNode() {
        try {
            ActorRef reqRouter = initReqRouter();
            String graph1 = "JAVA_CS";
            String objectType = "COURSE";
            Future<Object> nodeReq1 = createDataNode(reqRouter, graph1, objectType);
            Future<Object> nodeReq2 = createDataNode(reqRouter, graph1, objectType);
            Future<Object> nodeReq3 = createDataNode(reqRouter, graph1, objectType);
            handleFutureBlock(nodeReq1, "createDataNode", GraphDACParams.node_id.name());
            handleFutureBlock(nodeReq2, "createDataNode", GraphDACParams.node_id.name());
            handleFutureBlock(nodeReq3, "createDataNode", GraphDACParams.node_id.name());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //@Test
    public void testGetNodesCount() {
        try {
            ActorRef reqRouter = initReqRouter();
            String graphId = "JAVA_CS";
            Request request = new Request();
            request.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
            request.setManagerName(GraphEngineManagers.SEARCH_MANAGER);
            request.setOperation("getNodesCount");
            SearchCriteria sc = new SearchCriteria();
            sc.setNodeType(SystemNodeTypes.DEFINITION_NODE.name());
            request.put(GraphDACParams.search_criteria.name(), sc);
            Future<Object> req = Patterns.ask(reqRouter, request, timeout);
            Object arg1 = Await.result(req, t.duration());
            ObjectMapper mapper = new ObjectMapper();
            logger.info("Result:" + mapper.writeValueAsString(arg1));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Future<Object> createDataNode(ActorRef reqRouter, String graphId, String objectType) {
        Request request = new Request();
        request.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
        request.setManagerName(GraphEngineManagers.NODE_MANAGER);
        request.setOperation("createDataNode");
        request.put(GraphDACParams.object_type.name(), objectType);
        Future<Object> req = Patterns.ask(reqRouter, request, timeout);
        return req;
    }

    private Future<Object> createGraph(ActorRef reqRouter, String graphId) {
        Request request = new Request();
        request.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
        request.setManagerName(GraphEngineManagers.GRAPH_MANAGER);
        request.setOperation("createGraph");
        Future<Object> req = Patterns.ask(reqRouter, request, timeout);
        return req;
    }

    private Future<Object> deleteGraph(ActorRef reqRouter, String graphId) {
        Request request = new Request();
        request.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
        request.setManagerName(GraphEngineManagers.GRAPH_MANAGER);
        request.setOperation("deleteGraph");
        Future<Object> req = Patterns.ask(reqRouter, request, timeout);
        return req;
    }

    private void handleFutureBlock(Future<Object> req, String operation, String param) {

        try {
            Object arg1 = Await.result(req, t.duration());
            System.out.println(operation + " response: " + arg1);
            if (arg1 instanceof Response) {
                Response ar = (Response) arg1;
                logger.info(ar.getResult());
                logger.info(ar.get(param));
                logger.info(ar.getParams());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
