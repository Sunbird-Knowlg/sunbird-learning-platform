package com.ilimi.graph.engine.importtest;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.graph.common.enums.GraphEngineParams;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.engine.router.RequestRouter;
import com.ilimi.graph.enums.ImportType;
import com.ilimi.graph.importer.InputStreamValue;

public class GraphImportTest {

    long timeout = 50000;
    Timeout t = new Timeout(Duration.create(30, TimeUnit.SECONDS));
    
    ActorRef reqRouter = null;
    
    @BeforeClass
    private void initReqRouter() throws Exception {
        ActorSystem system = ActorSystem.create("MySystem");
        reqRouter = system.actorOf(Props.create(RequestRouter.class));

        Future<Object> future = Patterns.ask(reqRouter, "init", timeout);
        Object response = Await.result(future, t.duration());
        Thread.sleep(2000);
        System.out.println("Response from request router: " + response);
    }
    
    //@Test(priority =1, dataProvider="definitions", dataProviderClass=GraphImportDataProvider.class)
    public void testImportDefinitions(String graphId, String fileName, String message) {
        try {
            long t1 = System.currentTimeMillis();
            Request request = new Request();
            request.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
            request.setManagerName(GraphEngineManagers.NODE_MANAGER);
            request.setOperation("importDefinitions");
            InputStream inputStream = GraphImportTest.class.getClassLoader().getResourceAsStream(fileName);
            DataInputStream dis = new DataInputStream(inputStream);
            byte[] b = new byte[dis.available()];
            dis.readFully(b);
            request.put(GraphEngineParams.input_stream.name(), new String(b));
            Patterns.ask(reqRouter, request, t);

            long t2 = System.currentTimeMillis();
            System.out.println(message+" Import Time: " + (t2 - t1));
            Thread.sleep(15000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    //@Test(priority =2, dataProvider="csvdata", dataProviderClass=GraphImportDataProvider.class)
    public void testImportData(String graphId, String fileName, String message) {
        try {
            Request request = new Request();
            request.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
            request.setManagerName(GraphEngineManagers.GRAPH_MANAGER);
            request.setOperation("importGraph");
            request.put(GraphEngineParams.format.name(), ImportType.CSV.name());

            InputStream inputStream = GraphImportTest.class.getClassLoader().getResourceAsStream(fileName);

            request.put(GraphEngineParams.input_stream.name(), new InputStreamValue(inputStream));
            Future<Object> req = Patterns.ask(reqRouter, request, t);

            Object obj = Await.result(req, t.duration());
            Response response = (Response) obj;
//            OutputStreamValue osV = (OutputStreamValue) response.get(GraphEngineParams.output_stream.name());
            System.out.println(response.getResult());
//            if(osV == null) {
//                System.out.println(response.getResult());
//            } else {
//                ByteArrayOutputStream os = (ByteArrayOutputStream) osV.getOutputStream();
//                FileUtils.writeByteArrayToFile(new File("Literacy-GraphEngine-WithResult.csv"), os.toByteArray());
//            }
            System.out.println(message+" imported.");
            Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
