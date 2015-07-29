package com.ilimi.graph.engine.router;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Address;
import akka.actor.AddressFromURIString;
import akka.actor.Props;
import akka.remote.routing.RemoteRouterConfig;
import akka.routing.RoundRobinPool;
import akka.routing.SmallestMailboxPool;

import com.ilimi.graph.cache.actor.GraphCacheActor;
import com.ilimi.graph.cache.actor.GraphCacheActorPoolMgr;
import com.ilimi.graph.cache.actor.GraphCacheManagers;
import com.ilimi.graph.dac.mgr.impl.GraphDACGraphMgrImpl;
import com.ilimi.graph.dac.mgr.impl.GraphDACNodeMgrImpl;
import com.ilimi.graph.dac.mgr.impl.GraphDACSearchMgrImpl;
import com.ilimi.graph.dac.router.GraphDACActorPoolMgr;
import com.ilimi.graph.dac.router.GraphDACManagers;
import com.ilimi.graph.engine.mgr.impl.CollectionManagerImpl;
import com.ilimi.graph.engine.mgr.impl.GraphMgrImpl;
import com.ilimi.graph.engine.mgr.impl.NodeManagerImpl;
import com.ilimi.graph.engine.mgr.impl.SearchManagerImpl;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class ActorBootstrap {

    private static Document document;
    private static ActorSystem system;
    private static final String DEFAULT_SYSTEM_NAME = "ActorSystem";

    static {
        try {
            InputStream inputStream = ActorBootstrap.class.getClassLoader().getResourceAsStream("actor-config.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            document = dBuilder.parse(inputStream);
            document.getDocumentElement().normalize();
            loadConfiguration();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ActorSystem getActorSystem() {
        return system;
    }

    public static void loadConfiguration() {
        try {
            Config config = ConfigFactory.load();
            /*if (null != document) {
                // init actor configuration
                NodeList configList = document.getElementsByTagName("init");
                if (null != configList && configList.getLength() > 0) {
                    Node configNode = configList.item(0);
                    if (configNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element configElement = (Element) configNode;
                        String systemName = configElement.getAttribute("system");
                        if (StringUtils.isBlank(systemName))
                            systemName = DEFAULT_SYSTEM_NAME;
                        try {
                            system = ActorSystem.create(systemName, config.getConfig(DEFAULT_SYSTEM_NAME));
                            registerShutdownHook();
                        } catch (Exception e) {
                        }
                    }
                }
                initRouters();
                createManagersPool("graph-managers");
                createManagersPool("dac-managers");
                createManagersPool("cache-managers");
                createRoutersPool();
            } else {
                system = ActorSystem.create(DEFAULT_SYSTEM_NAME, config.getConfig(DEFAULT_SYSTEM_NAME));
                createLocatConfig();
            }*/
            system = ActorSystem.create(DEFAULT_SYSTEM_NAME, config.getConfig(DEFAULT_SYSTEM_NAME));
            createLocatConfig();
            initMethodMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createLocatConfig() {
        int poolSize = 100;

        Props graphMgrProps = Props.create(GraphMgrImpl.class);
        ActorRef graphMgr = system.actorOf(new SmallestMailboxPool(poolSize).props(graphMgrProps));
        GraphEngineActorPoolMgr.addActorRefToPool(null, GraphEngineManagers.GRAPH_MANAGER, graphMgr);

        Props nodeMgrProps = Props.create(NodeManagerImpl.class);
        ActorRef nodeMgr = system.actorOf(new SmallestMailboxPool(poolSize).props(nodeMgrProps));
        GraphEngineActorPoolMgr.addActorRefToPool(null, GraphEngineManagers.NODE_MANAGER, nodeMgr);

        Props collMgrProps = Props.create(CollectionManagerImpl.class);
        ActorRef collMgr = system.actorOf(new SmallestMailboxPool(poolSize).props(collMgrProps));
        GraphEngineActorPoolMgr.addActorRefToPool(null, GraphEngineManagers.COLLECTION_MANAGER, collMgr);

        Props searchMgrProps = Props.create(SearchManagerImpl.class);
        ActorRef searchMgr = system.actorOf(new SmallestMailboxPool(poolSize).props(searchMgrProps));
        GraphEngineActorPoolMgr.addActorRefToPool(null, GraphEngineManagers.SEARCH_MANAGER, searchMgr);

        Props dacRouterProps = Props.create(DACRequestRouter.class);
        ActorRef dacRouter = system.actorOf(new SmallestMailboxPool(poolSize).props(dacRouterProps));
        GraphDACActorPoolMgr.setDacRouter(dacRouter);

        Props cacheRouterProps = Props.create(CacheRequestRouter.class);
        ActorRef cacheRouter = system.actorOf(new SmallestMailboxPool(poolSize).props(cacheRouterProps));
        GraphCacheActorPoolMgr.setCacheRouter(cacheRouter);

        Props graphDACMgrProps = Props.create(GraphDACGraphMgrImpl.class);
        ActorRef graphDACMgr = system.actorOf(new SmallestMailboxPool(poolSize).props(graphDACMgrProps));
        GraphDACActorPoolMgr.addActorRefToPool(null, GraphDACManagers.DAC_GRAPH_MANAGER, graphDACMgr);

        Props nodeDACMgrProps = Props.create(GraphDACNodeMgrImpl.class);
        ActorRef nodeDACMgr = system.actorOf(new SmallestMailboxPool(poolSize).props(nodeDACMgrProps));
        GraphDACActorPoolMgr.addActorRefToPool(null, GraphDACManagers.DAC_NODE_MANAGER, nodeDACMgr);

        Props searchDACMgrProps = Props.create(GraphDACSearchMgrImpl.class);
        ActorRef searchDACMgr = system.actorOf(new SmallestMailboxPool(poolSize).props(searchDACMgrProps));
        GraphDACActorPoolMgr.addActorRefToPool(null, GraphDACManagers.DAC_SEARCH_MANAGER, searchDACMgr);

        Props cacheMgrProps = Props.create(GraphCacheActor.class);
        ActorRef cacheMgr = system.actorOf(new SmallestMailboxPool(poolSize).props(cacheMgrProps));
        GraphCacheActorPoolMgr.addActorRefToPool(null, GraphCacheManagers.GRAPH_CACHE_MANAGER, cacheMgr);
    }

    private static void initMethodMap() {
        GraphEngineActorPoolMgr.initMethodMap(GraphEngineManagers.GRAPH_MANAGER);
        GraphEngineActorPoolMgr.initMethodMap(GraphEngineManagers.NODE_MANAGER);
        GraphEngineActorPoolMgr.initMethodMap(GraphEngineManagers.COLLECTION_MANAGER);
        GraphEngineActorPoolMgr.initMethodMap(GraphEngineManagers.SEARCH_MANAGER);

        GraphDACActorPoolMgr.initMethodMap(GraphDACManagers.DAC_GRAPH_MANAGER);
        GraphDACActorPoolMgr.initMethodMap(GraphDACManagers.DAC_NODE_MANAGER);
        GraphDACActorPoolMgr.initMethodMap(GraphDACManagers.DAC_SEARCH_MANAGER);

        GraphCacheActorPoolMgr.initMethodMap(GraphCacheManagers.GRAPH_CACHE_MANAGER);
    }

    private static void initRouters() {
        NodeList nList = document.getElementsByTagName("init-router");
        if (null != nList && nList.getLength() > 0) {
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    try {
                        String className = eElement.getAttribute("class");
                        String name = eElement.getAttribute("name");
                        String strCount = eElement.getAttribute("count");
                        int count = 1;
                        try {
                            count = Integer.parseInt(strCount);
                        } catch (Exception e) {
                        }
                        Class<?> cls = Class.forName(className);
                        Props actorProps = Props.create(cls);
                        ActorRef actor = system.actorOf(new SmallestMailboxPool(count).props(actorProps));
                        if (StringUtils.equalsIgnoreCase("RequestRouter", name))
                            GraphEngineActorPoolMgr.setRequestRouter(actor);
                        else if (StringUtils.equalsIgnoreCase("DACRouter", name))
                            GraphDACActorPoolMgr.setDacRouter(actor);
                        else if (StringUtils.equalsIgnoreCase("CacheRouter", name))
                            GraphCacheActorPoolMgr.setCacheRouter(actor);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static void createRoutersPool() {
        NodeList nList = document.getElementsByTagName("router");
        if (null != nList && nList.getLength() > 0) {
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String name = eElement.getAttribute("name");
                    String location = eElement.getAttribute("location");
                    if (StringUtils.isNotBlank(location)) {
                        ActorSelection actor = system.actorSelection(location);
                        if (StringUtils.equalsIgnoreCase("RequestRouter", name))
                            GraphEngineActorPoolMgr.setRequestRouter(actor.anchor());
                        else if (StringUtils.equalsIgnoreCase("DACRouter", name))
                            GraphDACActorPoolMgr.setDacRouter(actor.anchor());
                        else if (StringUtils.equalsIgnoreCase("CacheRouter", name))
                            GraphCacheActorPoolMgr.setCacheRouter(actor.anchor());
                    }
                }
            }
        }
    }

    private static void createManagersPool(String poolName) {
        NodeList nList = document.getElementsByTagName(poolName);
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                NodeList cList = eElement.getChildNodes();
                if (null != cList && cList.getLength() > 0) {
                    for (int i = 0; i < cList.getLength(); i++) {
                        Node cNode = cList.item(i);
                        if (cNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element cElement = (Element) cNode;
                            String classes = cElement.getAttribute("classes");
                            if (StringUtils.isNotBlank(classes)) {
                                String[] arr = classes.split(",");
                                NodeList graphList = cElement.getChildNodes();
                                if (null != graphList && graphList.getLength() > 0) {
                                    for (int j = 0; j < graphList.getLength(); j++) {
                                        Node gNode = graphList.item(j);
                                        if (gNode.getNodeType() == Node.ELEMENT_NODE) {
                                            Element gElement = (Element) gNode;
                                            String id = gElement.getAttribute("id");
                                            String strCount = gElement.getAttribute("count");
                                            int count = 1;
                                            try {
                                                count = Integer.parseInt(strCount);
                                            } catch (Exception e) {
                                            }
                                            String locations = gElement.getAttribute("location");
                                            Address[] addresses = null;
                                            if (StringUtils.isNotBlank(locations)) {
                                                String[] locationArr = locations.split(",");
                                                addresses = new Address[locationArr.length];
                                                for (int k = 0; k < locationArr.length; k++) {
                                                    addresses[k] = AddressFromURIString.parse(locationArr[k]);
                                                }
                                            }
                                            addActorsToPool(arr, count, id, poolName, addresses);
                                        }
                                    }
                                } else {
                                    addActorsToPool(arr, 1, null, poolName, null);
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    private static void addActorsToPool(String[] arr, int count, String id, String poolName, Address[] addresses) {
        for (String className : arr) {
            try {
                Class<?> cls = Class.forName(className);
                Props actorProps = Props.create(cls);
                ActorRef actor = null;
                if (null != addresses && addresses.length > 0) {
                    actor = system.actorOf(new RemoteRouterConfig(new RoundRobinPool(count), addresses).props(Props.create(cls)));
                } else {
                    actor = system.actorOf(new SmallestMailboxPool(count).props(actorProps));
                }
                if (StringUtils.equalsIgnoreCase("graph-managers", poolName))
                    GraphEngineActorPoolMgr.addActorRefToPool(id, className, actor);
                else if (StringUtils.equalsIgnoreCase("dac-managers", poolName))
                    GraphDACActorPoolMgr.addActorRefToPool(id, className, actor);
                else if (StringUtils.equalsIgnoreCase("cache-managers", poolName))
                    GraphCacheActorPoolMgr.addActorRefToPool(id, className, actor);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Shutting down Actor System...");
                system.shutdown();
            }
        });
    }
}
