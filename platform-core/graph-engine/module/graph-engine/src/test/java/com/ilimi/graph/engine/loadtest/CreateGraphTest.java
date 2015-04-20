package com.ilimi.graph.engine.loadtest;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.pattern.Patterns;

import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.engine.router.GraphEngineManagers;

public class CreateGraphTest {

    ActorRef reqRouter = null;
    String SCENARIO_NAME ="CREATE_GRAPH"; 
    
//    @BeforeTest
    public void init() throws Exception {
        String logFileName = SCENARIO_NAME +"_" + System.currentTimeMillis();
        System.out.println("Logs are captured in "+logFileName+".log file.");
//        LoggerUtil.config(logFileName);
        reqRouter = TestUtil.initReqRouter();
    }
    
//    @Test(threadPoolSize=100, invocationCount=100)
    public void testCreateGraph() {
        try {

            String graph1 = "GRAPH_" + System.currentTimeMillis()+"_"+Thread.currentThread().getId();
            Future<Object> req1 = createGraph(reqRouter, graph1);

            TestUtil.handleFutureBlock(req1, "createGraph", GraphDACParams.GRAPH_ID.name());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private Future<Object> createGraph(ActorRef reqRouter, String graphId) {
        Request request = new Request();
        request.getContext().put(GraphHeaderParams.REQUEST_ID.name(), "REQUEST_"+Thread.currentThread().getId());
        request.getContext().put(GraphHeaderParams.SCENARIO_NAME.name(), SCENARIO_NAME);
        request.getContext().put(GraphHeaderParams.GRAPH_ID.name(), graphId);
        request.setManagerName(GraphEngineManagers.GRAPH_MANAGER);
        request.setOperation("createGraph");
        Future<Object> req = Patterns.ask(reqRouter, request, TestUtil.timeout);
        return req;
    }

}
