package org.ekstep.language.wordchain.traverser;

import java.util.List;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;

public abstract class AbstractTraverser implements ITraverserInterface {

	protected TraversalDescription traversalDescription;

	@Override
	public abstract TraversalDescription getTraversalDescription(int traversalDepth, String graphId);

	public void setEvaluators(List<Evaluator> evaluators) {
		for (Evaluator evaluator : evaluators) {
			traversalDescription.evaluator(evaluator);
		}
	}

	public void setEvaluator(Evaluator evaluator) {
		traversalDescription.evaluator(evaluator);
	}

	@Override
	public Traverser traverse(final Node startNode, int traversalDepth, String graphId, List<Evaluator> evaluators) {
		traversalDescription = getTraversalDescription(traversalDepth, graphId);
		setEvaluators(evaluators);
		return traversalDescription.traverse(startNode);
	}

	public TraversalDescription getTraversalDescription() {
		return traversalDescription;
	}
	
}
