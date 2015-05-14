package com.ilimi.graph.engine.loadtest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.routing.SmallestMailboxPool;
import akka.util.Timeout;

import com.ilimi.common.dto.Response;
import com.ilimi.graph.engine.router.RequestRouter;

public class TestUtil {

    public static Timeout timeout = new Timeout(Duration.create(50, TimeUnit.SECONDS));

    public static ActorRef initReqRouter() throws Exception {
        ActorSystem system = ActorSystem.create("MySystem");
        Props props = Props.create(RequestRouter.class);
        ActorRef reqRouter = system.actorOf(new SmallestMailboxPool(20).props(props));

        Future<Object> future = Patterns.ask(reqRouter, "init", timeout);
        Object response = Await.result(future, timeout.duration());
        Thread.sleep(2000);
        System.out.println("Response from request router: " + response);
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
                    System.out.println(ar.getResult());
                    System.out.println(ar.get(param));
                }
                System.out.println(ar.getParams());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
