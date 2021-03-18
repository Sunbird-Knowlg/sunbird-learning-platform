package org.sunbird.graph.engine.loadtest;

import java.io.InputStream;

import org.sunbird.common.dto.Request;
import org.sunbird.graph.common.enums.GraphEngineParams;
import org.sunbird.graph.common.enums.GraphHeaderParams;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.engine.common.GraphEngineTestSetup;
import org.sunbird.graph.engine.mgr.impl.GraphMgrTest;
import org.sunbird.graph.engine.router.GraphEngineManagers;
import org.sunbird.graph.importer.InputStreamValue;
import org.junit.Before;
import org.junit.Test;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import scala.concurrent.Future;

public class ImportGraphTest {
    ActorRef reqRouter = null;

    @Before
    public void init() throws Exception {
        reqRouter = TestUtil.initReqRouter();
    }

    @Test
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
