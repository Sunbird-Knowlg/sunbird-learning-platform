package com.ilimi.graph.dac.router;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.ilimi.graph.common.BaseRequestRouter;
import com.ilimi.graph.dac.mgr.impl.GraphDACGraphMgrImpl;
import com.ilimi.graph.dac.mgr.impl.GraphDACNodeMgrImpl;
import com.ilimi.graph.dac.mgr.impl.GraphDACSearchMgrImpl;

import akka.actor.ActorRef;

public class GraphDACActorPoolMgr {

    private static final String DEFAULT_GRAPH_ID = "*";

    private static Map<String, Map<String, ActorRef>> actorMap = null;
    private static Map<String, Map<String, Method>> methodMap = null;

    private static ActorRef dacRouter;

    static {
        actorMap = new HashMap<String, Map<String, ActorRef>>();
        Map<String, ActorRef> defaultActorMap = new HashMap<String, ActorRef>();
        actorMap.put(DEFAULT_GRAPH_ID, defaultActorMap);

        methodMap = new HashMap<String, Map<String, Method>>();
    }

    public static ActorRef getDacRouter() {
        return dacRouter;
    }

    public static void setDacRouter(ActorRef router) {
        dacRouter = router;
    }

    public static void initMethodMap(String managerName) {
        Class<?> cls = null;
        if (StringUtils.equals(GraphDACManagers.DAC_GRAPH_MANAGER, managerName)) {
            cls = GraphDACGraphMgrImpl.class;
        } else if (StringUtils.equals(GraphDACManagers.DAC_NODE_MANAGER, managerName)) {
            cls = GraphDACNodeMgrImpl.class;
        } else if (StringUtils.equals(GraphDACManagers.DAC_SEARCH_MANAGER, managerName)) {
            cls = GraphDACSearchMgrImpl.class;
        }
        if (null != cls) {
            Map<String, Method> map = BaseRequestRouter.getMethodMap(cls);
            if (null != map && !map.isEmpty())
                methodMap.put(managerName, map);
        }
    }

    public static Method getMethod(String managerName, String operation) {
        Map<String, Method> map = methodMap.get(managerName);
        if (null != map && !map.isEmpty()) {
            return map.get(operation);
        }
        return null;
    }

    public static void addActorRefToPool(String graphId, String managerName, ActorRef ref) {
        Map<String, ActorRef> actorRefs = null;
        if (StringUtils.isNotBlank(managerName) && null != ref) {
            if (StringUtils.isNotBlank(graphId)) {
                actorRefs = actorMap.get(graphId);
                if (null == actorRefs) {
                    actorRefs = new HashMap<String, ActorRef>();
                    actorRefs.put(managerName, ref);
                    actorMap.put(graphId, actorRefs);
                } else {
                    actorRefs.put(managerName, ref);
                }
            } else {
                actorRefs = actorMap.get(DEFAULT_GRAPH_ID);
                actorRefs.put(managerName, ref);
            }
        }
    }

    public static ActorRef getActorRefFromPool(String graphId, String managerName) {
        if (StringUtils.isNotBlank(managerName)) {
            Map<String, ActorRef> actorRefs = null;
            if (StringUtils.isNotBlank(graphId)) {
                actorRefs = actorMap.get(graphId);
                if (null != actorRefs) {
                    return actorRefs.get(managerName);
                } else {
                    actorRefs = actorMap.get(DEFAULT_GRAPH_ID);
                    return actorRefs.get(managerName);
                }
            } else {
                actorRefs = actorMap.get(DEFAULT_GRAPH_ID);
                return actorRefs.get(managerName);
            }
        }
        return null;
    }

}
