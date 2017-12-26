package org.ekstep.graph.engine.exporttest;

import java.io.ByteArrayInputStream;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.graph.common.enums.GraphEngineParams;
import org.ekstep.graph.common.enums.GraphHeaderParams;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.engine.router.GraphEngineManagers;
import org.ekstep.graph.engine.router.RequestRouter;
import org.ekstep.graph.importer.InputStreamValue;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

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
        return reqRouter;
    }
    
//    @Test
    public void test() {
        try {
            ActorRef reqRouter = initReqRouter();
            Request request = new Request();
            request.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
            request.put(GraphDACParams.node_id.name(), "Lit:C1");
            request.setManagerName(GraphEngineManagers.NODE_MANAGER);
            request.setOperation("exportNode");
            Future<Object> req = Patterns.ask(reqRouter, request, t);
            Object obj = Await.result(req, t.duration());
            Response response = (Response) obj;
            InputStreamValue isV = (InputStreamValue) response.get(GraphEngineParams.input_stream.name());
            ByteArrayInputStream is = (ByteArrayInputStream) isV.getInputStream();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
