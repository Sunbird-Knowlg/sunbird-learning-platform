package com.ilimi.orchestrator.interpreter.command;

import java.util.List;
import java.util.Map;

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

public class GetTraversalDescription implements ICommand, Command {

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
				Map<String, String> relationMap = (Map<String, String>) obj;

				tclObject = argv[3];
				obj = ReflectObject.get(interp, tclObject);
				List<String> uniquenessList = (List<String>) obj;
				
				tclObject = argv[4];
				obj = ReflectObject.get(interp, tclObject);
				int minLength = (int) obj;
				
				
				tclObject = argv[5];
				obj = ReflectObject.get(interp, tclObject);
				int maxLength = (int) obj;

				com.ilimi.graph.dac.model.Traverser searchTraverser = new com.ilimi.graph.dac.model.Traverser(graphId);
				TraversalDescription traversalDescription = searchTraverser.getBaseTraversalDescription();
				
				addRelations(traversalDescription, relationMap);
				addUniquenessCriteria(traversalDescription, uniquenessList);
				traversalDescription = traversalDescription.evaluator(Evaluators.fromDepth(minLength))
						.evaluator(Evaluators.toDepth(maxLength));

				TclObject tclResp = ReflectObject.newInstance(interp, traversalDescription.getClass(), traversalDescription);
				interp.setResult(tclResp);
			} catch (Exception e) {
				throw new TclException(interp, "Unable to read response: " + e.getMessage());
			}
		} else {
			throw new TclNumArgsException(interp, 1, argv, "Invalid arguments to get_traversal_description command");
		}
	}

	private void addUniquenessCriteria(TraversalDescription traversalDescription, List<String> uniquenessList) {
		if(uniquenessList.isEmpty()){
			traversalDescription = traversalDescription.uniqueness(Uniqueness.NONE);
		}
		for(String uniqueness: uniquenessList){
			traversalDescription = traversalDescription.uniqueness(getUniqueness(uniqueness));
		}
	}

	private Uniqueness getUniqueness(String uniqueness) {
		return Uniqueness.valueOf(uniqueness);
	}

	private void addRelations(TraversalDescription traversalDescription, Map<String, String> relationMap) throws Exception {
		for(Map.Entry<String, String> entry: relationMap.entrySet()){
			String relationName = entry.getKey();
			String direction =  entry.getValue();
			traversalDescription = traversalDescription.relationships(getRelations(relationName), getDirection(direction));
		}
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
		return "get_traversal_description";
	}
}
