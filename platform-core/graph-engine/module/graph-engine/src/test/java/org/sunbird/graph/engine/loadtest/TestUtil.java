package org.sunbird.graph.engine.loadtest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sunbird.common.dto.Response;
import org.sunbird.graph.engine.router.ActorBootstrap;
import org.sunbird.graph.engine.router.GraphEngineActorPoolMgr;

import akka.actor.ActorRef;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class TestUtil {

    public static Timeout timeout = new Timeout(Duration.create(50, TimeUnit.SECONDS));
    private static final Logger logger = LogManager.getLogger("PerformanceTestLogger");

    public static ActorRef initReqRouter() throws Exception {
        ActorBootstrap.getActorSystem();
        ActorRef reqRouter = GraphEngineActorPoolMgr.getRequestRouter();
        Thread.sleep(2000);
        logger.info("Request Router: " + reqRouter);
        return reqRouter;
    }

    public static Map<String, Object> getMetadata(int value) {
        Map<String, Object> metadata = new HashMap<String, Object>();
        for (int i = 0; i < 20; i++) {
            metadata.put("KEY_" + i, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_" + i);
        }

        switch (value) {
        case 0:
            metadata.put("LEVEL", 0);
            metadata.put("META_TYPE", "TYPE_1");
            metadata.put("META_POLICY", "POLICY_1");
            break;
        case 1:
            metadata.put("LEVEL", 1);
            metadata.put("META_TYPE", "TYPE_1");
            metadata.put("META_POLICY", "POLICY_1");
            break;
        case 2:
            metadata.put("LEVEL", 2);
            metadata.put("META_TYPE", "TYPE_2");
            metadata.put("META_POLICY", "POLICY_1");
            break;

        case 3:
            metadata.put("LEVEL", 3);
            metadata.put("META_TYPE", "TYPE_2");
            metadata.put("META_POLICY", "POLICY_1");
            break;
        case 4:
            metadata.put("LEVEL", 4);
            metadata.put("META_TYPE", "TYPE_3");
            metadata.put("META_POLICY", "POLICY_1");
            break;
        case 5:
            metadata.put("LEVEL", 5);
            metadata.put("META_TYPE", "TYPE_3");
            metadata.put("META_POLICY", "POLICY_2");
            break;
        case 6:
            metadata.put("LEVEL", 6);
            metadata.put("META_TYPE", "TYPE_4");
            metadata.put("META_POLICY", "POLICY_2");
            break;
        case 7:
            metadata.put("LEVEL", 7);
            metadata.put("META_TYPE", "TYPE_4");
            metadata.put("META_POLICY", "POLICY_2");
            break;
        case 8:
            metadata.put("LEVEL", 8);
            metadata.put("META_TYPE", "TYPE_5");
            metadata.put("META_POLICY", "POLICY_2");
            break;
        case 9:
            metadata.put("LEVEL", 9);
            metadata.put("META_TYPE", "TYPE_5");
            metadata.put("META_POLICY", "POLICY_2");
            break;
        }

        return metadata;
    }

    public static void handleFutureBlock(Future<Object> req, String operation, String param) {
        try {
            Object arg1 = Await.result(req, timeout.duration());
            if (arg1 instanceof Response) {
                Response ar = (Response) arg1;
                if (StringUtils.isNotBlank(param)) {
                    logger.info(ar.getResult());
                    logger.info(ar.get(param));
                }
                logger.info(ar.getParams());
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }
}
