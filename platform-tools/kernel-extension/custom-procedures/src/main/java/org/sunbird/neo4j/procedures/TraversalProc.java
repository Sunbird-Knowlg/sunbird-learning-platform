package org.sunbird.neo4j.procedures;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.traversal.BranchState;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Uniqueness;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

public class TraversalProc {
	
	public static final long BREADTH_FIRST_TRAVERSAL = 0;
    public static final long DEPTH_FIRST_TRAVERSAL = 1;

	// This field declares that we need a GraphDatabaseService
	// as context when any procedure in this class is invoked
	@Context
	public GraphDatabaseService db;

	// This gives us a log instance that outputs messages to the
	// standard log, `neo4j.log`
	@Context
	public Log log;

	/**
	 * @param graphId
	 * @param startNodeIds
	 * @param traversal
	 * @param fromDepth
	 * @param toDepth
	 * @param endNodeIds
	 * @param endRelations
	 * @param uniqueness
	 * @param wordIds
	 * @param relationMap
	 * @param pathExpander
	 * 
	 * @return
	 */
	@Procedure(value = "ekstep.procs.traverse")
	public Stream<PathResult> traverse(@Name("graphId") String graphId, @Name("startNodeIds") List<String> startNodeIds,
			@Name("traversal") Long traversal, @Name("fromDepth") Long fromDepth, @Name("toDepth") Long toDepth,
			@Name("endNodeIds") List<String> endNodeIds, @Name("endRelations") List<String> endRelations,
			@Name("uniqueness") List<String> uniqueness, @Name("wordIds") List<String> wordIds,
			@Name("relationMap") Map<String, String> relationMap,
			@Name("pathExpander") Map<String, Object> pathExpander) {

		System.out.println("traversal procedure begin");
		try {
			TraversalDescription td = db.traversalDescription();
	        if (null != traversal && traversal == DEPTH_FIRST_TRAVERSAL)
	            td = td.depthFirst();
	        else
	            td = td.breadthFirst();
	        if (null != fromDepth && fromDepth > 0)
	            td = td.evaluator(Evaluators.fromDepth(fromDepth.intValue()));
	        if (null != toDepth && toDepth > 0)
	            td = td.evaluator(Evaluators.toDepth(toDepth.intValue()));
			
	        if (null != endNodeIds && endNodeIds.size() > 0) {
	            Node[] nodeArray = new Node[endNodeIds.size()];
	            for (int i = 0; i < endNodeIds.size(); i++) {
	            	Node node = db.findNode(Label.label(graphId), "IL_UNIQUE_ID", endNodeIds.get(i));
	                nodeArray[i] = node;
	            }
	            td = td.evaluator(Evaluators.pruneWhereEndNodeIs(nodeArray));
	        }
	        
	        if (null != endRelations && endRelations.size() > 0) {
	            RelationshipType type = RelationshipType.withName(endRelations.get(0));
	            if (endRelations.size() > 1) {
	            	RelationshipType[] relationTypes = new RelationshipType[endRelations.size() - 1];
	                for (int i = 1; i < endRelations.size(); i++) {
	                    relationTypes[i - 1] = RelationshipType.withName(endRelations.get(i));
	                }
	                td = td.evaluator(Evaluators.pruneWhereLastRelationshipTypeIs(type, relationTypes));
	            } else {
	                td = td.evaluator(Evaluators.pruneWhereLastRelationshipTypeIs(type));
	            }
	        }
	        
	        if(null == uniqueness || uniqueness.isEmpty()){
				td = td.uniqueness(Uniqueness.NONE);
			} else if (null != uniqueness) {
				for(String u: uniqueness){
					td = td.uniqueness(getUniqueness(u));
				}
			}
			
			if (null != wordIds && !wordIds.isEmpty())
				td = td.evaluator(new WordIdEvaluator(wordIds));

			if (null != relationMap && !relationMap.isEmpty()) {
				for (Entry<String, String> entry : relationMap.entrySet()) {
					Direction direction = getDirection(entry.getValue());
					td = td.relationships(RelationshipType.withName(entry.getKey()), direction);
				}
			}
			
			if (null != pathExpander && !pathExpander.isEmpty()) {
				ArrayExpander expander = new ArrayExpander(pathExpander);
				td = td.expand(expander);
			}
			
			Node[] startNodes = new Node[startNodeIds.size()];
			if (null != startNodeIds && startNodeIds.size() > 0) {
	            for (int i = 0; i < startNodeIds.size(); i++) {
	            	Node node = db.findNode(Label.label(graphId), "IL_UNIQUE_ID", startNodeIds.get(i));
	            	System.out.println(startNodeIds.get(i) + " - " + node.getId());
	            	startNodes[i] = node;
	            }
	        }
			
			ResourceIterator<Path> pathsIterator = td.traverse(startNodes).iterator();
			System.out.println("got pahts... ");
			Builder<PathResult> builder = Stream.builder();
			if (null != pathsIterator) {
	        	List<Path> finalPaths = removeSubPaths(pathsIterator);
	        	System.out.println("final paths: " + finalPaths.size());
	        	for (Path path : finalPaths) {
	        		builder.add(new PathResult(path));
	        	}
			}
			return builder.build();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Error in traversal", e);
		}
		return null;
	}

	public List<Path> removeSubPaths(ResourceIterator<Path> pathsIterator) {
		List<Path> finalPaths = new ArrayList<Path>();
		try {
			Path previousPath = null;
			int previousPathLength = 0;
			while (pathsIterator.hasNext()) {
				Path traversedPath = pathsIterator.next();
				System.out.println(traversedPath.startNode().getId() + " - " + traversedPath.endNode().getId() + " : " + traversedPath.length());
				if (traversedPath.length() > previousPathLength) {
					previousPath = traversedPath;
					previousPathLength = traversedPath.length();
				} else if (traversedPath.length() == previousPathLength) {
					if (previousPath != null) {
						finalPaths.add(previousPath);
					}
					previousPath = traversedPath;
					previousPathLength = traversedPath.length();
				} else {
					if (previousPath != null) {
						finalPaths.add(previousPath);
						previousPath = null;
					}
				}
			}

			if (previousPath != null) {
				finalPaths.add(previousPath);
				previousPath = null;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return finalPaths;
	}
	
	private Direction getDirection(String direction) throws Exception {
		switch (direction.toUpperCase()) {
		case "INCOMING": {
			return Direction.INCOMING;
		}
		case "OUTGOING": {
			return Direction.OUTGOING;
		}
		case "BOTH": {
			return Direction.BOTH;
		}
		default: {
			throw new Exception("Invalid direction");
		}
		}
	}

	private Uniqueness getUniqueness(String uniqueness) {
		return Uniqueness.valueOf(uniqueness);
	}

	public static class PathResult {
		public Node startNode;
		public Node endNode;
		public List<Node> nodes;
		public List<Relationship> relations;

		public PathResult(Path path) {
			this.startNode = path.startNode();
			this.endNode = path.endNode();
			this.nodes = new ArrayList<Node>();
			if (null != path.nodes())
				for (Node pathNode : path.nodes())
					this.nodes.add(pathNode);
			this.relations = new ArrayList<Relationship>();
			if (null != path.relationships())
				for (Relationship rel : path.relationships())
					this.relations.add(rel);
		}
	}

	public class WordIdEvaluator implements Evaluator {

		private List<String> ids = new ArrayList<String>();

		public WordIdEvaluator(List<String> ids) {
			super();
			this.ids = ids;
		}

		@Override
		public Evaluation evaluate(Path path) {
			Node endNode = path.endNode();
			if (endNode.hasProperty("IL_FUNC_OBJECT_TYPE")) {
				String objectType = (String) endNode.getProperty("IL_FUNC_OBJECT_TYPE");
				if (objectType.equalsIgnoreCase("Word")) {
					if (endNode.hasProperty("IL_UNIQUE_ID")) {
						String identifier = (String) endNode.getProperty("IL_UNIQUE_ID");
						if (ids.contains(identifier)) {
							return Evaluation.INCLUDE_AND_CONTINUE;
						}
					}
				} else {
					return Evaluation.INCLUDE_AND_CONTINUE;
				}
			}
			return Evaluation.EXCLUDE_AND_PRUNE;
		}
	}

	@SuppressWarnings("rawtypes")
	public class ArrayExpander implements PathExpander {

		private Direction[] directions;
		private RelationshipType[] types;
		private int nodeCount;

		public ArrayExpander(Direction[] directions, RelationshipType[] types, int nodeCount) {
			this.types = types;
			this.directions = directions;
			this.nodeCount = nodeCount;
		}

		@SuppressWarnings("unchecked")
		public ArrayExpander(Map<String, Object> pathExpander) throws Exception {
			if (pathExpander != null && !pathExpander.isEmpty()) {
				ArrayList<String> relationTypes = (ArrayList<String>) pathExpander.get("relationTypes");
				if (relationTypes == null) {
					throw new Exception("RelationTypes is mandatory for path expander");
				}
				ArrayList<String> directionsList = (ArrayList<String>) pathExpander.get("directions");
				if (directionsList == null) {
					throw new Exception("Directions field is mandatory for path expander");
				}
				Integer nodeCount = Integer.parseInt((String) pathExpander.get("nodeCount"));

				RelationshipType[] types = new RelationshipType[relationTypes.size()];
				Direction[] directions = new Direction[directionsList.size()];
				int count = 0;
				for (String relationType : relationTypes) {
					types[count] = getRelations(relationType);
					count++;
				}
				count = 0;
				for (String direction : directionsList) {
					directions[count] = getDirectionObject(direction);
					count++;
				}
				this.directions = directions;
				this.types = types;
				this.nodeCount = nodeCount;
			}
		}

		private RelationshipType getRelations(String relation) throws Exception {
			return RelationshipType.withName(relation);
		}

		private Direction getDirectionObject(String direction) throws Exception {
			switch (direction) {
			case "INCOMING": {
				return Direction.INCOMING;
			}
			case "OUTGOING": {
				return Direction.OUTGOING;
			}
			case "BOTH": {
				return Direction.BOTH;
			}
			default: {
				throw new Exception("Invalid direction");
			}
			}
		}

		public Iterable<Relationship> expand(Path path, BranchState state) {
			return path.endNode().getRelationships(directions[path.length() % nodeCount],
					types[path.length() % nodeCount]);
		}

		public ArrayExpander reverse() {
			return new ArrayExpander(directions, types, nodeCount);
		}
	}

}
