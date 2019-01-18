package org.ekstep.async.router;

import akka.actor.ActorRef;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Actor Pool for {@link AsyncRequestRouter}
 *
 * @see AsyncRequestRouter
 * @see AsyncRequestRouterPool
 */
public class AsyncActorPool {

    private static Map<String, ActorRef> actorRefMap = new HashMap<>();

    public static ActorRef getActorRefFromPool(String managerName) {
        if (StringUtils.isNotBlank(managerName)) {
            return actorRefMap.get(managerName);
        }
        return null;
    }

    public static void addActorRefToPool(String managerName, ActorRef ref) {
        actorRefMap.put(managerName, ref);
    }

}
