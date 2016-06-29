package org.ekstep.language.wordchain.traverser;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;

public interface ITraverserInterface {
	
	public void setEvaluator(Evaluator evaluator);

	public TraversalDescription getTraversalDescription(int maxTraversalDepth, int minTraversalDepth, String graphId);

	public Traverser traverse(Node startNode);

	TraversalDescription createTraversalDescription(int maxTraversalDepth, int minTraversalDepth, String graphId);

}
