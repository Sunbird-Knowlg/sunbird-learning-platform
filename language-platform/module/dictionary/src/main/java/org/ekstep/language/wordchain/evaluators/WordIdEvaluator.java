package org.ekstep.language.wordchain.evaluators;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;

public class WordIdEvaluator implements Evaluator {

	private String ATTRIB_IDENTIFIER = "identifier";
	private List<String> ids = new ArrayList<String>();

	public WordIdEvaluator(List<String> ids) {
		super();
		this.ids = ids;
	}

	@Override
	public Evaluation evaluate(Path path) {
		Node endNode = path.endNode();
		if (endNode.hasProperty(ATTRIB_IDENTIFIER)) {
			String identifier = (String) endNode.getProperty(ATTRIB_IDENTIFIER);

			for (String id : ids) {
				if (id.equalsIgnoreCase(identifier)) {
					return Evaluation.EXCLUDE_AND_PRUNE;
				}
			}
		}
		return Evaluation.INCLUDE_AND_CONTINUE;
	}
}
