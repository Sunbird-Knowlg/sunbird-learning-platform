package com.ilimi.graph.engine.importtest;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
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
import com.ilimi.graph.common.dto.StringValue;
import com.ilimi.graph.common.enums.GraphEngineParams;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.engine.mgr.impl.GraphMgrTest;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.engine.router.RequestRouter;
import com.ilimi.graph.enums.ImportType;
import com.ilimi.graph.importer.InputStreamValue;
import com.ilimi.graph.importer.OutputStreamValue;

public class GamesCSVImport {

    long timeout = 50000;
    Timeout t = new Timeout(Duration.create(30, TimeUnit.SECONDS));
    String graphId = "NUMERACY";

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
    public void testImportDefinitions() {
        try {
            ActorRef reqRouter = initReqRouter();

            long t1 = System.currentTimeMillis();
            Request request = new Request();
            request.getContext().put(GraphHeaderParams.GRAPH_ID.name(), graphId);
            request.setManagerName(GraphEngineManagers.NODE_MANAGER);
            request.setOperation("importDefinitions");
            // Change the file path.
            InputStream inputStream = GraphMgrTest.class.getClassLoader().getResourceAsStream("game_definitions.json");
            DataInputStream dis = new DataInputStream(inputStream);
            byte[] b = new byte[dis.available()];
            dis.readFully(b);
            request.put(GraphEngineParams.INPUT_STREAM.name(), new StringValue(new String(b)));
            Future<Object> req = Patterns.ask(reqRouter, request, t);

            handleFutureBlock(req, "importDefinitions", GraphDACParams.GRAPH_ID.name());
            long t2 = System.currentTimeMillis();
            System.out.println("Import Time: " + (t2 - t1));
            Thread.sleep(15000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testImportData() {
        try {
            ActorRef reqRouter = initReqRouter();

            Request request = new Request();
            request.getContext().put(GraphHeaderParams.GRAPH_ID.name(), graphId);
            request.setManagerName(GraphEngineManagers.GRAPH_MANAGER);
            request.setOperation("importGraph");
            request.put(GraphEngineParams.FORMAT.name(), new StringValue(ImportType.CSV.name()));

            // Change the file path.
            InputStream inputStream = GraphMgrTest.class.getClassLoader().getResourceAsStream("Games-GraphEngine.csv");

            request.put(GraphEngineParams.INPUT_STREAM.name(), new InputStreamValue(inputStream));
            Future<Object> req = Patterns.ask(reqRouter, request, t);

            Object obj = Await.result(req, t.duration());
            Response response = (Response) obj;
            OutputStreamValue osV = (OutputStreamValue) response.get(GraphEngineParams.OUTPUT_STREAM.name());
            if(osV == null) {
                System.out.println(response.getResult());
            } else {
                ByteArrayOutputStream os = (ByteArrayOutputStream) osV.getOutputStream();
                FileUtils.writeByteArrayToFile(new File("Games-GraphEngine-WithResult.csv"), os.toByteArray());
                System.out.println("Result: \n"+new String(os.toByteArray()));   //Prints the string content read from input stream
            }
            Thread.sleep(15000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void handleFutureBlock(Future<Object> req, String operation, String param) {
        try {
            Object arg1 = Await.result(req, t.duration());
            System.out.println(operation + " response: " + arg1);
            if (arg1 instanceof Response) {
                Response ar = (Response) arg1;
                System.out.println(ar.getResult());
                System.out.println(ar.get(param));
                System.out.println(ar.getStatus());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testExportGraph() {
        try {
            ActorRef reqRouter = initReqRouter();
            Request request = new Request();
            request.getContext().put(GraphHeaderParams.GRAPH_ID.name(), graphId);
            request.setManagerName(GraphEngineManagers.GRAPH_MANAGER);
            request.setOperation("exportGraph");
            request.put(GraphEngineParams.FORMAT.name(), new StringValue(ImportType.JSON.name()));
            Future<Object> req = Patterns.ask(reqRouter, request, t);
            Object obj = Await.result(req, t.duration());
            Response response = (Response) obj;
            OutputStreamValue osV = (OutputStreamValue) response.get(GraphEngineParams.OUTPUT_STREAM.name());
            ByteArrayOutputStream os = (ByteArrayOutputStream) osV.getOutputStream();
            System.out.println("Result: \n" + new String(os.toByteArray())); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
