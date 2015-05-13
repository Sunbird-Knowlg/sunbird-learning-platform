package com.ilimi.graph.engine.mgr.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;

import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.Response;
import com.ilimi.graph.common.enums.GraphEngineParams;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.model.SearchConditions;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.dac.util.Neo4jGraphFactory;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.engine.router.RequestRouter;
import com.ilimi.graph.enums.ImportType;
import com.ilimi.graph.importer.InputStreamValue;
import com.ilimi.graph.importer.OutputStreamValue;

public class GraphMgrTest {

    long timeout = 50000;
    Timeout t = new Timeout(Duration.create(30, TimeUnit.SECONDS));

    private ActorRef initReqRouter() throws Exception {
        ActorSystem system = ActorSystem.create("MySystem");
        ActorRef reqRouter = system.actorOf(Props.create(RequestRouter.class));

        Future<Object> future = Patterns.ask(reqRouter, "init", timeout);
        Object response = Await.result(future, t.duration());
        Thread.sleep(2000);
        System.out.println("Response from request router: " + response);
        return reqRouter;
    }

    @Test
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
            System.out.println("Import Time: " + (t2 - t1));
            Thread.sleep(15000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
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
            System.out.println("Result: \n" + new String(os.toByteArray())); // Prints
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

    @Test
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
            System.out.println("Load Time: " + (t2 - t1));
            ObjectMapper mapper = new ObjectMapper();
            System.out.println("Result:" + mapper.writeValueAsString(arg1));

            Thread.sleep(15000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
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
            System.out.println("Validate Time: " + (t2 - t1));
            ObjectMapper mapper = new ObjectMapper();
            System.out.println("Result:" + mapper.writeValueAsString(arg1));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
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

    @Test
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

    @Test
    public void testSearchNodes() {
        try {
            ActorRef reqRouter = initReqRouter();
            String graphId = "JAVA_CS";
            ObjectMapper mapper = new ObjectMapper();
            Neo4jGraphFactory.getGraphDb(graphId);

            Request req0 = new Request();
            req0.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
            req0.setManagerName(GraphEngineManagers.SEARCH_MANAGER);
            req0.setOperation("getNodesCount");
            SearchCriteria sc0 = new SearchCriteria();
            sc0.countQuery(true);
            req0.put(GraphDACParams.search_criteria.name(), sc0);
            Future<Object> res0 = Patterns.ask(reqRouter, req0, timeout);
            Object arg0 = Await.result(res0, t.duration());
            System.out.println("Result:" + mapper.writeValueAsString(arg0));
            Thread.sleep(5000);

            Request request = new Request();
            request.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
            request.setManagerName(GraphEngineManagers.SEARCH_MANAGER);
            request.setOperation("searchNodes");
            SearchCriteria sc = new SearchCriteria();
            sc.add(SearchConditions.eq(SystemProperties.IL_SYS_NODE_TYPE.name(), SystemNodeTypes.DEFINITION_NODE.name()));
            // sc.returnField("OUT_RELATION_OBJECTS");
            request.put(GraphDACParams.search_criteria.name(), sc);
            Future<Object> req = Patterns.ask(reqRouter, request, timeout);
            Object arg1 = Await.result(req, t.duration());
            System.out.println("Result:" + mapper.writeValueAsString(arg1));

            Request request2 = new Request();
            request2.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
            request2.setManagerName(GraphEngineManagers.SEARCH_MANAGER);
            request2.setOperation("searchNodes");
            SearchCriteria sc2 = new SearchCriteria();
            // sc2.add(SearchConditions.eq(SystemProperties.IL_UNIQUE_ID.name(),
            // SystemNodeTypes.DEFINITION_NODE.name() + "_COURSE"));
            sc.add(SearchConditions.eq(SystemProperties.IL_SYS_NODE_TYPE.name(), SystemNodeTypes.DATA_NODE.name()));
            // sc2.returnField("OUT_RELATION_OBJECTS");

            request2.put(GraphDACParams.search_criteria.name(), sc2);
            Future<Object> req2 = Patterns.ask(reqRouter, request2, timeout);
            Object arg2 = Await.result(req2, t.duration());
            System.out.println("Result:" + mapper.writeValueAsString(arg2));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetNodesCount() {
        try {
            ActorRef reqRouter = initReqRouter();
            String graphId = "JAVA_CS";
            Request request = new Request();
            request.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
            request.setManagerName(GraphEngineManagers.SEARCH_MANAGER);
            request.setOperation("getNodesCount");
            SearchCriteria sc = new SearchCriteria();
            sc.add(SearchConditions.eq(SystemProperties.IL_SYS_NODE_TYPE.name(), SystemNodeTypes.DEFINITION_NODE.name()));
            request.put(GraphDACParams.search_criteria.name(), sc);
            Future<Object> req = Patterns.ask(reqRouter, request, timeout);
            Object arg1 = Await.result(req, t.duration());
            ObjectMapper mapper = new ObjectMapper();
            System.out.println("Result:" + mapper.writeValueAsString(arg1));
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
                System.out.println(ar.getResult());
                System.out.println(ar.get(param));
                System.out.println(ar.getParams());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
