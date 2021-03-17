package org.sunbird.graph.engine.mgr.impl;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.graph.common.enums.GraphEngineParams;
import org.sunbird.graph.common.enums.GraphHeaderParams;
import org.sunbird.graph.dac.enums.GraphDACParams;
import org.sunbird.graph.engine.router.GraphEngineManagers;
import org.sunbird.graph.engine.router.RequestRouter;
import org.sunbird.graph.enums.ImportType;
import org.sunbird.graph.importer.InputStreamValue;
import org.sunbird.graph.importer.OutputStreamValue;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class CSVImportTest {

    long timeout = 50000;
    Timeout t = new Timeout(Duration.create(30, TimeUnit.SECONDS));
    private static final Logger logger = LogManager.getLogger("PerformanceTestLogger");

    private ActorRef initReqRouter() throws Exception {
        ActorSystem system = ActorSystem.create("MySystem");
        ActorRef reqRouter = system.actorOf(Props.create(RequestRouter.class));

        Future<Object> future = Patterns.ask(reqRouter, "init", timeout);
        Object response = Await.result(future, t.duration());
        Thread.sleep(2000);
        logger.info("Response from request router: " + response);
        return reqRouter;
    }

//    @Test
    public void testImportDefinitions() {
        try {
            ActorRef reqRouter = initReqRouter();

            long t1 = System.currentTimeMillis();
            Request request = new Request();
            request.getContext().put(GraphHeaderParams.graph_id.name(), "LEARNING_MAP_2");
            request.setManagerName(GraphEngineManagers.NODE_MANAGER);
            request.setOperation("importDefinitions");
            // Change the file path.
            InputStream inputStream = GraphMgrTest.class.getClassLoader().getResourceAsStream("taxonomy_definitions.json");
            DataInputStream dis = new DataInputStream(inputStream);
            byte[] b = new byte[dis.available()];
            dis.readFully(b);
            request.put(GraphEngineParams.input_stream.name(), new String(b));
            Future<Object> req = Patterns.ask(reqRouter, request, t);

            handleFutureBlock(req, "importDefinitions", GraphDACParams.graph_id.name());
            long t2 = System.currentTimeMillis();
            logger.info("Import Time: " + (t2 - t1));
            Thread.sleep(15000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
//    @Test
    public void testImportData() {
        try {
            ActorRef reqRouter = initReqRouter();

            Request request = new Request();
            request.getContext().put(GraphHeaderParams.graph_id.name(), "LEARNING_MAP_2");
            request.setManagerName(GraphEngineManagers.GRAPH_MANAGER);
            request.setOperation("importGraph");
            request.put(GraphEngineParams.format.name(), ImportType.CSV.name());

            // Change the file path.
            InputStream inputStream = GraphMgrTest.class.getClassLoader().getResourceAsStream("Literacy-GraphEngine.csv");

            request.put(GraphEngineParams.input_stream.name(), new InputStreamValue(inputStream));
            Future<Object> req = Patterns.ask(reqRouter, request, t);

            Object obj = Await.result(req, t.duration());
            Response response = (Response) obj;
            OutputStreamValue osV = (OutputStreamValue) response.get(GraphEngineParams.output_stream.name());
            if(osV == null) {
                logger.info(response.getResult());
            } else {
                ByteArrayOutputStream os = (ByteArrayOutputStream) osV.getOutputStream();
                FileUtils.writeByteArrayToFile(new File("Literacy-GraphEngine-WithResult.csv"), os.toByteArray());
                logger.info("Result: \n"+new String(os.toByteArray()));   //Prints the string content read from input stream
            }
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
                logger.info(ar.getResult());
                logger.info(ar.get(param));
                logger.info(ar.getParams());
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }
}
