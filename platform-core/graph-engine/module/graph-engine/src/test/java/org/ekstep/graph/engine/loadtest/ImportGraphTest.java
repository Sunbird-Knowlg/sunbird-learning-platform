package org.ekstep.graph.engine.loadtest;

import java.io.InputStream;

import org.ekstep.common.dto.Request;
import org.ekstep.graph.common.enums.GraphEngineParams;
import org.ekstep.graph.common.enums.GraphHeaderParams;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.engine.mgr.impl.GraphMgrTest;
import org.ekstep.graph.engine.router.GraphEngineManagers;
import org.ekstep.graph.importer.InputStreamValue;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import scala.concurrent.Future;

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
            request.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
            request.getContext().put(GraphHeaderParams.request_id.name(), "REQUEST_" + Thread.currentThread().getId());
            request.getContext().put(GraphHeaderParams.scenario_name.name(), "IMPORT_GRAPH");
            request.setManagerName(GraphEngineManagers.GRAPH_MANAGER);
            request.setOperation("importGraph");
            request.put(GraphEngineParams.format.name(), "JSON");

            // Change the file path.
            InputStream inputStream = GraphMgrTest.class.getClassLoader().getResourceAsStream("Java Programming for the Cloud.json");

            request.put(GraphEngineParams.input_stream.name(), new InputStreamValue(inputStream));
            Future<Object> req = Patterns.ask(reqRouter, request, TestUtil.timeout);

            TestUtil.handleFutureBlock(req, "importGraph", GraphDACParams.graph_id.name());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
