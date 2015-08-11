package com.ilimi.graph.engine.importtest;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

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
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.engine.mgr.impl.GraphMgrTest;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.engine.router.RequestRouter;

public class LiteracyWorkSheetCSVImportV2Test {

    long timeout = 50000;
    Timeout t = new Timeout(Duration.create(30, TimeUnit.SECONDS));
    String graphId = "literacy_v2";

    private ActorRef initReqRouter() throws Exception {
        ActorSystem system = ActorSystem.create("MySystem");
        ActorRef reqRouter = system.actorOf(Props.create(RequestRouter.class));

        Future<Object> future = Patterns.ask(reqRouter, "init", timeout);
        Object response = Await.result(future, t.duration());
        Thread.sleep(2000);
        System.out.println("Response from request router: " + response);
        return reqRouter;
    }

    @Test(priority = 2)
    public void testImportDefinitions() {
        try {
            ActorRef reqRouter = initReqRouter();

            long t1 = System.currentTimeMillis();
            Request request = new Request();
            request.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
            request.setManagerName(GraphEngineManagers.NODE_MANAGER);
            request.setOperation("importDefinitions");
            // Change the file path.
            InputStream inputStream = GraphMgrTest.class.getClassLoader().getResourceAsStream("worksheet_definitions.json");
            DataInputStream dis = new DataInputStream(inputStream);
            byte[] b = new byte[dis.available()];
            dis.readFully(b);
            request.put(GraphEngineParams.input_stream.name(), new String(b));
            Future<Object> res = Patterns.ask(reqRouter, request, t);
            
            handleFutureBlock(res, "importDefinitions", GraphDACParams.graph_id.name());

            long t2 = System.currentTimeMillis();
            System.out.println("Literacy V2 Worksheet Definition Import Time: " + (t2 - t1));
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
                System.out.println(ar.getParams());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
