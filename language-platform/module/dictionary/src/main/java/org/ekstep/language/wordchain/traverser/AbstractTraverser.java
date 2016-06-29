package org.ekstep.language.wordchain.traverser;

import java.util.List;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;

public abstract class AbstractTraverser implements ITraverserInterface {

	protected TraversalDescription traversalDescription;

	@Override
	public abstract TraversalDescription getTraversalDescription(int maxTraversalDepth, int minTraversalDepth,  String graphId);

	public void setEvaluators(List<Evaluator> evaluators) {
		for (Evaluator evaluator : evaluators) {
			traversalDescription.evaluator(evaluator);
		}
	}

	@Override
	public void setEvaluator(Evaluator evaluator) {
		traversalDescription.evaluator(evaluator);
	}

	@Override
	public Traverser traverse(final Node startNode) {
		return traversalDescription.traverse(startNode);
	}
	
	@Override
	public TraversalDescription createTraversalDescription(int maxTraversalDepth, int minTraversalDepth, String graphId) {
		traversalDescription = getTraversalDescription(maxTraversalDepth, minTraversalDepth, graphId);
		return traversalDescription;
	}

}
