package com.ilimi.graph.engine.exporttest;

import java.io.ByteArrayOutputStream;
import java.io.File;
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
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.engine.router.RequestRouter;
import com.ilimi.graph.enums.ImportType;
import com.ilimi.graph.importer.OutputStreamValue;

public class NodeExportTest {

    long timeout = 50000;
    Timeout t = new Timeout(Duration.create(30, TimeUnit.SECONDS));
    String graphId = "LITERACY";

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
    public void test() {
        try {
            ActorRef reqRouter = initReqRouter();
            Request request = new Request();
            request.getContext().put(GraphHeaderParams.GRAPH_ID.name(), graphId);
            request.setManagerName(GraphEngineManagers.GRAPH_MANAGER);
            request.setOperation("exportGraph");
            request.put(GraphEngineParams.FORMAT.name(), new StringValue(ImportType.RDF.name()));
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
