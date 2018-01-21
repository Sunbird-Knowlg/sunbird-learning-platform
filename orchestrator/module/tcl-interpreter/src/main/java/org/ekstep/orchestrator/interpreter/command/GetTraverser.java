package org.ekstep.orchestrator.interpreter.command;

import java.util.List;
import java.util.Map;

import org.ekstep.graph.dac.model.Traverser;
import org.ekstep.orchestrator.interpreter.ICommand;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.traversal.Uniqueness;

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
				
				List<String> wordIds = (List<String>) request.get("wordIds");
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
				org.ekstep.graph.dac.model.Traverser searchTraverser = new org.ekstep.graph.dac.model.Traverser(graphId, startNodeId);
				searchTraverser = addRelations(searchTraverser, relationMap);
				searchTraverser = addUniquenessCriteria(searchTraverser, uniquenessList);
				searchTraverser = addWordIds(searchTraverser, wordIds);
				searchTraverser = addExpander(searchTraverser, pathExpander);
				searchTraverser = searchTraverser.fromDepth(minLength);
				searchTraverser = searchTraverser.toDepth(maxLength);

				TclObject tclResp = ReflectObject.newInstance(interp, searchTraverser.getClass(), searchTraverser);
				interp.setResult(tclResp);
			} catch (Exception e) {
				throw new TclException(interp, "Error: " + e.getMessage());
			}
		} else {
			throw new TclNumArgsException(interp, 1, argv, "Invalid arguments to get_traversal_description command");
		}
	}

	private Traverser addWordIds(Traverser traverser, List<String> wordIds) {
		if(wordIds != null){
			for (String wordId : wordIds)
				traverser = traverser.addWordId(wordId);
		}
		return traverser;
	}

	private Traverser addExpander(Traverser traverser, Map<String, Object> pathExpander) {
		if(pathExpander != null){
			traverser = traverser.setPathExpander(pathExpander);
		}
		return traverser;
	}

	private Traverser addUniquenessCriteria(Traverser traverser, List<String> uniquenessList) {
		if(null == uniquenessList || uniquenessList.isEmpty()){
			traverser = traverser.addUniqueness(Uniqueness.NONE.name());
		} else {
			for(String uniqueness: uniquenessList){
				traverser = traverser.addUniqueness(uniqueness);
			}
		}
		return traverser;
	}

	private Traverser addRelations(Traverser traverser, Map<String, String> relationMap) throws Exception {
		if(relationMap != null){
			for(Map.Entry<String, String> entry: relationMap.entrySet()){
				String relationName = entry.getKey();
				String direction =  entry.getValue();
				traverser = traverser.addRelationMap(relationName, getDirection(direction));
			}
		}
		return traverser;
	}
	
	private String getDirection(String direction) throws Exception {
		switch (direction) {
		case "INCOMING": {
			return Direction.INCOMING.name();
		}
		case "OUTGOING": {
			return Direction.OUTGOING.name();
		}
		case "BOTH": {
			return Direction.BOTH.name();
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
