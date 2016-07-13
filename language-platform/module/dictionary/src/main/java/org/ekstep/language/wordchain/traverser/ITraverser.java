package org.ekstep.language.wordchain.traverser;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;

public interface ITraverser {
	
	public void setEvaluator(Evaluator evaluator);

	public TraversalDescription enhanceTraversalDescription(int maxTraversalDepth, int minTraversalDepth, String graphId);
	
	public TraversalDescription getTraversalDescription();

	public Traverser traverse(Node startNode);

	public void setTraversalDescription(TraversalDescription traversalDescription);

}
