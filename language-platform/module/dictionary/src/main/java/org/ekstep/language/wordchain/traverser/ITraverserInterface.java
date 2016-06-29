package org.ekstep.language.wordchain.traverser;

import java.util.List;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;

public interface ITraverserInterface {
	
	public Traverser traverse(final Node startNode, int maxTraversalDepth, int minTraversalDepth, String graphId);

	public void setEvaluator(Evaluator evaluator);

	public TraversalDescription getTraversalDescription();

	public TraversalDescription getTraversalDescription(int maxTraversalDepth, int minTraversalDepth, String graphId);

}
