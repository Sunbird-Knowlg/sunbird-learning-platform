
package org.sunbird.graph.engine.mgr.impl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.sunbird.common.dto.Property;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.dto.ResponseParams;
import org.sunbird.common.dto.ResponseParams.StatusType;
import org.sunbird.graph.common.enums.GraphEngineParams;
import org.sunbird.graph.common.enums.GraphHeaderParams;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.mgr.impl.Neo4JBoltSearchMgrImpl;
import org.sunbird.graph.engine.router.GraphEngineManagers;
import org.sunbird.graph.engine.router.RequestRouter;
import org.sunbird.graph.enums.ImportType;
import org.sunbird.graph.importer.InputStreamValue;
import org.sunbird.graph.importer.OutputStreamValue;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.dispatch.Futures;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class TestGraphImportUsingCSV {

    long timeout = 50000;
    Timeout t = new Timeout(Duration.create(30, TimeUnit.SECONDS));
    String graphId = "LEARNING_MAP_TEST";

    private ActorRef initReqRouter() throws Exception {
        ActorSystem system = ActorSystem.create("MySystem");
        ActorRef reqRouter = system.actorOf(Props.create(RequestRouter.class));

        Future<Object> future = Patterns.ask(reqRouter, "init", timeout);
        Object response = Await.result(future, t.duration());
        Thread.sleep(2000);
        System.out.println("Response from request router: " + response);
        return reqRouter;
    }

    private Request getRequest() {
        Request request = new Request();
        request.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
        request.setManagerName(GraphEngineManagers.GRAPH_MANAGER);
        request.setOperation("importGraph");
        request.put(GraphEngineParams.format.name(), ImportType.CSV.name());
        return request;
    }

    private void deleteGraph() throws Exception {
        ActorRef reqRouter = initReqRouter();
        Request request = getRequest();
        request.setOperation("deleteGraph");
        Future<Object> req = Patterns.ask(reqRouter, request, t);
        Object obj = Await.result(req, t.duration());
        Response response = (Response) obj;
        ResponseParams params = response.getParams();
        assertEquals(StatusType.successful.name(), params.getStatus());
    }

    private void testImportDefinitionNodes() throws Exception {
        ActorRef reqRouter = initReqRouter();
        Request request = getRequest();
        request.setManagerName(GraphEngineManagers.NODE_MANAGER);
        request.setOperation("importDefinitions");
        InputStream inputStream = GraphMgrTest.class.getClassLoader().getResourceAsStream("testCSVs/test_definition.json");
        DataInputStream dis = new DataInputStream(inputStream);
        byte[] b = new byte[dis.available()];
        dis.readFully(b);
        request.put(GraphEngineParams.input_stream.name(), new String(b));
        Future<Object> req = Patterns.ask(reqRouter, request, t);
        Object obj = Await.result(req, t.duration());
        Response response = (Response) obj;
        ResponseParams params = response.getParams();
        assertEquals(StatusType.successful.name(), params.getStatus());

    }

    private String getNodeProperty(String uniqueId, String propertyName) throws Exception {

        Request request = getRequest();
        request.setOperation("getNodeProperty");
        request.put(GraphDACParams.node_id.name(), uniqueId);
        request.put(GraphDACParams.property_key.name(), propertyName);
		Future<Object> req = Futures.successful(new Neo4JBoltSearchMgrImpl().getNodeProperty(request));
        Object obj = Await.result(req, t.duration());
        Response response = (Response) obj;
        Property property = (Property) response.get(GraphDACParams.property.name());
        String val = (String) property.getPropertyValue();
        return val;
    }

    //@Test
    public void testForRequiredColumsHandling() throws Exception {
        ActorRef reqRouter = initReqRouter();
        Request request = getRequest();
        InputStream inputStream = GraphMgrTest.class.getClassLoader().getResourceAsStream("testCSVs/CSV-withoutRequiredColumns.csv");
        request.put(GraphEngineParams.input_stream.name(), new InputStreamValue(inputStream));
        Future<Object> req = Patterns.ask(reqRouter, request, t);
        Object obj = Await.result(req, t.duration());
        Response response = (Response) obj;
        ResponseParams params = response.getParams();
        assertEquals(StatusType.failed.name(), params.getStatus());
        assertTrue(params.getErrmsg().startsWith("Required columns are missing"));
        deleteGraph();
    }

    //@Test
    public void testForRequiredDataMissing() throws Exception {
        ActorRef reqRouter = initReqRouter();
        Request request = getRequest();
        InputStream inputStream = GraphMgrTest.class.getClassLoader().getResourceAsStream("testCSVs/CSV-withoutRequiredData.csv");
        request.put(GraphEngineParams.input_stream.name(), new InputStreamValue(inputStream));
        Future<Object> req = Patterns.ask(reqRouter, request, t);
        Object obj = Await.result(req, t.duration());
        Response response = (Response) obj;
        ResponseParams params = response.getParams();
        assertEquals(StatusType.failed.name(), params.getStatus());
        assertTrue(params.getErrmsg().startsWith("Required data(uniqueId, objectType) is missing for the row[2]"));
        deleteGraph();
    }

    //@Test
    public void testForDefinitionNodeMissedValidation() throws Exception {
        ActorRef reqRouter = initReqRouter();
        Request request = getRequest();
        InputStream inputStream = GraphMgrTest.class.getClassLoader().getResourceAsStream("testCSVs/CSV-NoDefinitionNodeCheck.csv");
        request.put(GraphEngineParams.input_stream.name(), new InputStreamValue(inputStream));
        Future<Object> req = Patterns.ask(reqRouter, request, t);
        Object obj = Await.result(req, t.duration());
        Response response = (Response) obj;
        ResponseParams params = response.getParams();
        assertEquals(StatusType.successful.name(), params.getStatus());
        OutputStreamValue osV = (OutputStreamValue) response.get(GraphEngineParams.output_stream.name());
        ByteArrayOutputStream os = (ByteArrayOutputStream) osV.getOutputStream();
        String output = new String(os.toByteArray());
        assertTrue(output.contains("Definition node not found for Object Type: TEST_OBJECT"));
        deleteGraph();
    }

    //@Test
    public void testForUpdateNodeMetadata() throws Exception {
        testImportDefinitionNodes();
        ActorRef reqRouter = initReqRouter();
        Request request = getRequest();
        InputStream inputStream = GraphMgrTest.class.getClassLoader().getResourceAsStream("testCSVs/CSV-NodesAndTags.csv");
        request.put(GraphEngineParams.input_stream.name(), new InputStreamValue(inputStream));
        Future<Object> req = Patterns.ask(reqRouter, request, t);
        Object obj = Await.result(req, t.duration());
        Response response = (Response) obj;
        ResponseParams params = response.getParams();
        assertEquals(StatusType.successful.name(), params.getStatus());

        String description = getNodeProperty("Num:C1", "description");
        System.out.println("Description:" + description);
        assertEquals("Geometry", description);

        request = getRequest();
        inputStream = GraphMgrTest.class.getClassLoader().getResourceAsStream("testCSVs/CSV-UpdatedNode.csv");
        request.put(GraphEngineParams.input_stream.name(), new InputStreamValue(inputStream));
        req = Patterns.ask(reqRouter, request, t);
        obj = Await.result(req, t.duration());
        response = (Response) obj;
        params = response.getParams();
        assertEquals(StatusType.successful.name(), params.getStatus());

        description = getNodeProperty("Num:C1", "description");
        assertEquals("Desc of Geometry", description);
        deleteGraph();
    }

    //@Test
    public void testForDefinitionNodeUpdate() throws Exception {
        ActorRef reqRouter = initReqRouter();
        Request request = getRequest();
        request.setManagerName(GraphEngineManagers.NODE_MANAGER);
        request.setOperation("importDefinitions");
        InputStream inputStream = GraphMgrTest.class.getClassLoader().getResourceAsStream("testCSVs/test_definition.json");
        DataInputStream dis = new DataInputStream(inputStream);
        byte[] b = new byte[dis.available()];
        dis.readFully(b);
        request.put(GraphEngineParams.input_stream.name(), new String(b));
        Future<Object> req = Patterns.ask(reqRouter, request, t);
        Object obj = Await.result(req, t.duration());
        Response response = (Response) obj;
        ResponseParams params = response.getParams();
        assertEquals(StatusType.successful.name(), params.getStatus());

        Thread.sleep(10000);

        request = getRequest();
        inputStream = GraphMgrTest.class.getClassLoader().getResourceAsStream("testCSVs/CSV-NodesAndTags.csv");
        request.put(GraphEngineParams.input_stream.name(), new InputStreamValue(inputStream));
        req = Patterns.ask(reqRouter, request, t);
        obj = Await.result(req, t.duration());
        response = (Response) obj;
        params = response.getParams();
        assertEquals(StatusType.successful.name(), params.getStatus());
        OutputStreamValue osV = (OutputStreamValue) response.get(GraphEngineParams.output_stream.name());
        ByteArrayOutputStream os = (ByteArrayOutputStream) osV.getOutputStream();
        String output = new String(os.toByteArray());
        assertFalse(output.contains("Required Metadata description not set"));

        request = getRequest();
        request.setManagerName(GraphEngineManagers.NODE_MANAGER);
        request.setOperation("importDefinitions");
        inputStream = GraphMgrTest.class.getClassLoader().getResourceAsStream("testCSVs/test_definition_updated.json");
        dis = new DataInputStream(inputStream);
        b = new byte[dis.available()];
        dis.readFully(b);
        request.put(GraphEngineParams.input_stream.name(), new String(b));
        req = Patterns.ask(reqRouter, request, t);
        obj = Await.result(req, t.duration());
        response = (Response) obj;
        params = response.getParams();
        assertEquals(StatusType.successful.name(), params.getStatus());

        request = getRequest();
        inputStream = GraphMgrTest.class.getClassLoader().getResourceAsStream("testCSVs/CSV-NodesAndTags.csv");
        request.put(GraphEngineParams.input_stream.name(), new InputStreamValue(inputStream));
        req = Patterns.ask(reqRouter, request, t);
        obj = Await.result(req, t.duration());
        response = (Response) obj;
        params = response.getParams();
        assertEquals(StatusType.successful.name(), params.getStatus());
        osV = (OutputStreamValue) response.get(GraphEngineParams.output_stream.name());
        os = (ByteArrayOutputStream) osV.getOutputStream();
        output = new String(os.toByteArray());
        assertTrue(output.contains("Required Metadata description not set"));

        deleteGraph();

    }
}
