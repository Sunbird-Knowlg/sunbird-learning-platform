package org.ekstep.language.wordchain.evaluators;

import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;

public class PathLengthEvaluator implements Evaluator {

	private int minLength;

	public PathLengthEvaluator(int minLength) {
		super();
		this.minLength = minLength;
	}

	@Override
	public Evaluation evaluate(Path path) {
		if(path.length() < minLength){
			return Evaluation.EXCLUDE_AND_PRUNE;
		}
		return Evaluation.INCLUDE_AND_CONTINUE;
	}
}
