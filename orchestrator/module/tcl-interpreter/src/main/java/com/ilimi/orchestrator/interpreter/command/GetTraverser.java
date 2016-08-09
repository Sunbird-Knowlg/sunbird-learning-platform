package com.ilimi.orchestrator.interpreter.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ekstep.language.wordchain.ArrayExpander;
import org.ekstep.language.wordchain.WordChainRelations;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Uniqueness;

import com.ilimi.orchestrator.interpreter.ICommand;

import tcl.lang.Command;
import tcl.lang.Interp;
import tcl.lang.TclException;
import tcl.lang.TclNumArgsException;
import tcl.lang.TclObject;
import tcl.pkg.java.ReflectObject;

public class GetTraverser implements ICommand, Command {

	@SuppressWarnings("unchecked")
	@Override
	public void cmdProc(Interp interp, TclObject[] argv) throws TclException {
		if (argv.length == 3) {
			try {
				TclObject tclObject = argv[1];
				Object obj = ReflectObject.get(interp, tclObject);
				String graphId = (String) obj;

				tclObject = argv[2];
				obj = ReflectObject.get(interp, tclObject);
				Map<String, Object> request = (Map<String, Object>) obj;
				
				Map<String, String> relationMap = (Map<String, String>) request.get("relations");
				Map<String, Object> pathExpander = (Map<String, Object>) request.get("pathExpander");
				
				if(pathExpander == null && relationMap == null){
					throw new Exception("Either path expander or relations map is mandatory");
				}
				
				List<String> uniquenessList = (List<String>) request.get("uniqueness");

				int minLength;
				int maxLength;
				try{
					minLength = Integer.parseInt((String)request.get("minLength"));
				} catch (NumberFormatException e){
					Double temp = Double.parseDouble((String)request.get("minLength"));
					minLength = temp.intValue();
				}
				
				try{
					maxLength = Integer.parseInt((String)request.get("maxLength"));
				} catch (NumberFormatException e){
					Double temp = Double.parseDouble((String)request.get("maxLength"));
					maxLength = temp.intValue();
				}
				
				String startNodeId = (String) request.get("startNodeId");
				
				if(startNodeId == null || startNodeId.isEmpty()){
					throw new Exception("Start node is mandatory");
				}

				ArrayExpander orderedPathExpander = null;
				if(pathExpander != null){
					ArrayList<String> relationTypes = (ArrayList<String>) pathExpander.get("relationTypes");
					if(relationTypes == null){
						throw new Exception("RelationTypes is mandatory for path expander");
					}
					ArrayList<String> directionsList = (ArrayList<String>) pathExpander.get("directions");
					if(directionsList == null){
						throw new Exception("Directions field is mandatory for path expander");
					}
					Integer nodeCount = Integer.parseInt((String)pathExpander.get("nodeCount"));
					
					RelationshipType[] types = new RelationshipType[relationTypes.size()];
					Direction[] directions = new Direction[directionsList.size()];
					int count=0;
					for(String relationType: relationTypes){
						types[count] = getRelations(relationType);
						count++;
					}
					count=0;
					for(String direction: directionsList){
						directions[count] = getDirection(direction);
						count++;
					}
					orderedPathExpander = new ArrayExpander(directions, types, nodeCount);
				}
				
				com.ilimi.graph.dac.model.Traverser searchTraverser = new com.ilimi.graph.dac.model.Traverser(graphId, startNodeId);
				TraversalDescription traversalDescription = searchTraverser.getBaseTraversalDescription();
				traversalDescription = addRelations(traversalDescription, relationMap);
				traversalDescription = addUniquenessCriteria(traversalDescription, uniquenessList);
				traversalDescription = addExpander(traversalDescription, orderedPathExpander);
				traversalDescription = traversalDescription.evaluator(Evaluators.fromDepth(minLength))
						.evaluator(Evaluators.toDepth(maxLength));
				
				searchTraverser.setTraversalDescription(traversalDescription);

				TclObject tclResp = ReflectObject.newInstance(interp, searchTraverser.getClass(), searchTraverser);
				interp.setResult(tclResp);
			} catch (Exception e) {
				throw new TclException(interp, "Error: " + e.getMessage());
			}
		} else {
			throw new TclNumArgsException(interp, 1, argv, "Invalid arguments to get_traversal_description command");
		}
	}

	private TraversalDescription addExpander(TraversalDescription traversalDescription, ArrayExpander orderedPathExpander) {
		if(orderedPathExpander != null){
			traversalDescription = traversalDescription.expand(orderedPathExpander);
		}
		return traversalDescription;
	}

	private TraversalDescription addUniquenessCriteria(TraversalDescription traversalDescription, List<String> uniquenessList) {
		if(uniquenessList.isEmpty()){
			traversalDescription = traversalDescription.uniqueness(Uniqueness.NONE);
		}
		for(String uniqueness: uniquenessList){
			traversalDescription = traversalDescription.uniqueness(getUniqueness(uniqueness));
		}
		return traversalDescription;
	}

	private Uniqueness getUniqueness(String uniqueness) {
		return Uniqueness.valueOf(uniqueness);
	}

	private TraversalDescription addRelations(TraversalDescription traversalDescription, Map<String, String> relationMap) throws Exception {
		if(relationMap != null){
			for(Map.Entry<String, String> entry: relationMap.entrySet()){
				String relationName = entry.getKey();
				String direction =  entry.getValue();
				traversalDescription = traversalDescription.relationships(getRelations(relationName), getDirection(direction));
			}
		}
		return traversalDescription;
	}
	
	private RelationshipType getRelations(String relation) throws Exception {
		return WordChainRelations.valueOf(relation);
	}

	private Direction getDirection(String direction) throws Exception {
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

	@Override
	public String getCommandName() {
		return "get_traverser";
	}
}
