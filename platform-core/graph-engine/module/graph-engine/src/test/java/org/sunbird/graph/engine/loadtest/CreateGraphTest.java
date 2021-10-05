package org.sunbird.graph.engine.loadtest;

import org.sunbird.common.dto.Request;
import org.sunbird.graph.common.enums.GraphHeaderParams;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.engine.common.GraphEngineTestSetup;
import org.sunbird.graph.engine.router.GraphEngineManagers;
import org.junit.Before;
import org.junit.Test;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import scala.concurrent.Future;

public class CreateGraphTest extends GraphEngineTestSetup {

    ActorRef reqRouter = null;
    String SCENARIO_NAME ="CREATE_GRAPH"; 
    
    @Before
    public void init() throws Exception {
        reqRouter = TestUtil.initReqRouter();
    }
    
    @Test
    public void testCreateGraph() {
        try {

            String graph1 = "GRAPH_" + System.currentTimeMillis()+"_"+Thread.currentThread().getId();
            Future<Object> req1 = createGraph(reqRouter, graph1);

            TestUtil.handleFutureBlock(req1, "createGraph", GraphDACParams.graph_id.name());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private Future<Object> createGraph(ActorRef reqRouter, String graphId) {
        Request request = new Request();
        request.getContext().put(GraphHeaderParams.request_id.name(), "REQUEST_"+Thread.currentThread().getId());
        request.getContext().put(GraphHeaderParams.scenario_name.name(), SCENARIO_NAME);
        request.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
        request.setManagerName(GraphEngineManagers.GRAPH_MANAGER);
        request.setOperation("createGraph");
        Future<Object> req = Patterns.ask(reqRouter, request, TestUtil.timeout);
        return req;
    }

}
