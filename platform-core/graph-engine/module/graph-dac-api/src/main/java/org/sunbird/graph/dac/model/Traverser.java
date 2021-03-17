package org.sunbird.graph.dac.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Request;
import org.sunbird.graph.service.common.GraphOperation;
import org.sunbird.graph.service.operation.Neo4JBoltSearchOperations;
import org.sunbird.graph.service.util.DriverUtil;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Value;

public class Traverser implements Serializable {

    private static final long serialVersionUID = -8621143933941005381L;
    public static final int BREADTH_FIRST_TRAVERSAL = 0;
    public static final int DEPTH_FIRST_TRAVERSAL = 1;

    private String graphId;
    private long traversal = DEPTH_FIRST_TRAVERSAL;
    private long toDepth;
    private long fromDepth;

    private List<String> startNodeIds = new ArrayList<String>();
    private List<String> endNodeIds = new ArrayList<String>();
    private List<String> endRelations = new ArrayList<String>();
    private List<String> uniqueness = new ArrayList<String>();
    private List<String> wordIds = new ArrayList<String>();
    private Map<String, String> relationMap = new HashMap<String, String>();
    private Map<String, Object> pathExpander = new HashMap<String, Object>();

	public Traverser(String graphId, String startNodeId) {
        this.graphId = graphId;
        if(startNodeId != null){
        	this.startNodeIds.add(startNodeId);
        }
    }
	
	public Traverser(String graphId) {
        this.graphId = graphId;
    }
	
	public Traverser(String graphId, List<String> startNodeIds) {
        this.graphId = graphId;
        for(String startNodeId: startNodeIds){
        	this.startNodeIds.add(startNodeId);
        }
    }
	
    public Traverser traversal(int traversal) {
        if (traversal == BREADTH_FIRST_TRAVERSAL || traversal == DEPTH_FIRST_TRAVERSAL)
            this.traversal = traversal;
        return this;
    }

    public Traverser fromDepth(int fromDepth) {
        if (fromDepth > 0)
            this.fromDepth = fromDepth;
        return this;
    }

    public Traverser toDepth(int toDepth) {
        if (toDepth > 0)
            this.toDepth = toDepth;
        return this;
    }

    public Traverser endNode(String nodeId) {
        this.endNodeIds.add(nodeId);
        return this;
    }

    public Traverser endRelations(String relation) {
        this.endRelations.add(relation);
        return this;
    }

    public void setStartNode(String startNodeId){
         this.startNodeIds.add(startNodeId);
    }
    
    public SubGraph traverse() {
        SubGraph subGraph = new SubGraph();
        Driver driver = DriverUtil.getDriver(graphId, GraphOperation.READ);
        try (Session session = driver.session()) {
        	String query = "CALL ekstep.procs.traverse";
        	Map<String, Object> params = getTraverserParams();
        	StatementResult result = session.run(query, params);
        	for (Record record : result.list()) {
				Path path = getPathObject(this.graphId, record);
				subGraph.addPath(path);
			}
        }
        return subGraph;
    }

    
    public Graph getSubGraph() {
        Graph subGraph = new Graph();
        Driver driver = DriverUtil.getDriver(graphId, GraphOperation.READ);
        try (Session session = driver.session()) {
        	String query = "CALL ekstep.procs.traverse";
        	List<Node> nodes = new ArrayList<Node>();
        	List<Relation> relations = new ArrayList<Relation>();
        	Map<String, Object> params = getTraverserParams();
        	StatementResult result = session.run(query, params);
        	for (Record record : result.list()) {
				Path path = getPathObject(this.graphId, record);
				if (null != path.getNodes())
					nodes.addAll(path.getNodes());
				if (null != path.getRelations())
					relations.addAll(path.getRelations());
			}
        	subGraph.setNodes(nodes);
			subGraph.setRelations(relations);
        }
        return subGraph;
    }
    
    private Map<String, Object> getTraverserParams() {
    	Map<String, Object> params = new HashMap<String, Object>();
		params.put("startNodeIds", this.startNodeIds);
		params.put("graphId", this.graphId);
		params.put("traversal", this.traversal);
		params.put("fromDepth", this.fromDepth);
		params.put("toDepth", this.toDepth);
		params.put("endNodeIds", this.endNodeIds);
		params.put("endRelations", this.endRelations);
		params.put("uniqueness", this.uniqueness);
		params.put("wordIds", this.wordIds);
		params.put("pathExpander", this.pathExpander);
		params.put("relationMap", this.relationMap);
		return params;
    }
    
    private Path getPathObject(String graphId, Record record) {
    	Request request = new Request();
    	Value startNodeVal = record.get("startNode");
    	Value endNodeVal = record.get("endNode");
    	Value nodesVal = record.get("nodes");
    	Value relationsVal = record.get("relations");
    	Path path = new Path(graphId);
    	if (null != startNodeVal && StringUtils.equalsIgnoreCase("NODE", startNodeVal.type().name())) {
    		org.neo4j.driver.v1.types.Node node = startNodeVal.asNode();
			Node startNode = Neo4JBoltSearchOperations.getNodeById(graphId, node.id(), true, request);
    		path.setStartNode(startNode);
    	}
    	if (null != endNodeVal && StringUtils.equalsIgnoreCase("NODE", endNodeVal.type().name())) {
    		org.neo4j.driver.v1.types.Node node = endNodeVal.asNode();
			Node endNode = Neo4JBoltSearchOperations.getNodeById(graphId, node.id(), true, request);
    		path.setEndNode(endNode);
    	}
    	if (null != nodesVal) {
    		List<Object> list = nodesVal.asList();
    		List<Node> nodes = new ArrayList<Node>();
    		for (Object obj : list) {
    			org.neo4j.driver.v1.types.Node node = (org.neo4j.driver.v1.types.Node) obj;
				Node startNode = Neo4JBoltSearchOperations.getNodeById(graphId, node.id(), true, request);
    			nodes.add(startNode);
    		}
    		path.setNodes(nodes);
    	}
    	if (null != relationsVal) {
    		List<Object> list = relationsVal.asList();
    		List<Relation> relations = new ArrayList<Relation>();
    		for (Object obj : list) {
    			org.neo4j.driver.v1.types.Relationship rel = (org.neo4j.driver.v1.types.Relationship) obj;
				Relation relation = Neo4JBoltSearchOperations.getRelationById(graphId, rel.id(), request);
    			relations.add(relation);
    		}
    		path.setRelations(relations);
    	}
    	return path;
    }

	public List<String> getUniqueness() {
		return uniqueness;
	}

	public Traverser addUniqueness(String uniqueness) {
		this.uniqueness.add(uniqueness);
		return this;
	}
	
	public List<String> getWordIds() {
		return wordIds;
	}

	public Traverser addWordId(String wordId) {
		this.wordIds.add(wordId);
		return this;
	}
	
	public Map<String, Object> getPathExpander() {
		return pathExpander;
	}

	public Traverser setPathExpander(Map<String, Object> pathExpander) {
		this.pathExpander = pathExpander;
		return this;
	}
	
	public Map<String, String> getRelationMap() {
		return relationMap;
	}

	public Traverser addRelationMap(String name, String direction) {
		this.relationMap.put(name, direction);
		return this;
	}
}
