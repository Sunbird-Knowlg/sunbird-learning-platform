package org.sunbird.graph.engine.loadtest;

import org.sunbird.common.dto.Request;
import org.sunbird.graph.common.enums.GraphHeaderParams;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.dac.enums.RelationTypes;
import org.sunbird.graph.dac.model.Traverser;
import org.sunbird.graph.engine.router.GraphEngineManagers;
import org.neo4j.graphdb.Direction;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import scala.concurrent.Future;

/**
 * 
 * TEMP_CS has - No. of Nodes:1365 (includes data and sequence nodes) No. of
 * Relations:1543
 * 
 * @author mahesh
 * 
 */

public class TraverseGraphTest {

    ActorRef reqRouter = null;
    private String graphId = "TEMP_CS";
    private String startNodeId = "COURSE_TEMP_CS";
    String SCENARIO_NAME = "TRAVERSE_GRAPH";

//    @BeforeTest
    public void init() throws Exception {
        String logFileName = SCENARIO_NAME + "_" + System.currentTimeMillis();
        LoggerUtil.config(logFileName);
        reqRouter = TestUtil.initReqRouter();
        Thread.sleep(5000);

    }

    private Future<Object> traverse(ActorRef reqRouter, String graphId) {
        Traverser traverser = new Traverser(graphId, startNodeId);
        traverser.addRelationMap(RelationTypes.HIERARCHY.relationName(), Direction.OUTGOING.name());
        Request request = new Request();
        request.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
        request.getContext().put(GraphHeaderParams.request_id.name(), "REQUEST_" + Thread.currentThread().getId());
        request.getContext().put(GraphHeaderParams.scenario_name.name(), SCENARIO_NAME);
        request.put(GraphDACParams.traversal_description.name(), traverser);
        request.setManagerName(GraphEngineManagers.SEARCH_MANAGER);
        request.setOperation("traverse");
        Future<Object> resp = Patterns.ask(reqRouter, request, TestUtil.timeout);
        return resp;
    }
}
