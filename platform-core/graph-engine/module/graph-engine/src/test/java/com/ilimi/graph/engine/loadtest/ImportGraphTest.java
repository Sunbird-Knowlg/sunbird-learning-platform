package com.ilimi.graph.engine.loadtest;

import java.io.InputStream;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.pattern.Patterns;

import com.ilimi.graph.common.Request;
import com.ilimi.graph.common.dto.StringValue;
import com.ilimi.graph.common.enums.GraphEngineParams;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.engine.mgr.impl.GraphMgrTest;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.importer.InputStreamValue;

public class ImportGraphTest {
    ActorRef reqRouter = null;

//    @BeforeTest
    public void init() throws Exception {
        reqRouter = TestUtil.initReqRouter();
    }

//    @AfterTest
    public void destroy() throws Exception {
        Thread.sleep(10000);
    }

//    @Test(threadPoolSize = 100, invocationCount = 100)
    public void testImportGraph() {
        try {
            Request request = new Request();
            String graphId = "GRAPH_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
            request.getContext().put(GraphHeaderParams.GRAPH_ID.name(), graphId);
            request.getContext().put(GraphHeaderParams.REQUEST_ID.name(), "REQUEST_" + Thread.currentThread().getId());
            request.getContext().put(GraphHeaderParams.SCENARIO_NAME.name(), "IMPORT_GRAPH");
            request.setManagerName(GraphEngineManagers.GRAPH_MANAGER);
            request.setOperation("importGraph");
            request.put(GraphEngineParams.FORMAT.name(), new StringValue("JSON"));

            // Change the file path.
            InputStream inputStream = GraphMgrTest.class.getClassLoader().getResourceAsStream("Java Programming for the Cloud.json");

            request.put(GraphEngineParams.INPUT_STREAM.name(), new InputStreamValue(inputStream));
            Future<Object> req = Patterns.ask(reqRouter, request, TestUtil.timeout);

            TestUtil.handleFutureBlock(req, "importGraph", GraphDACParams.GRAPH_ID.name());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
