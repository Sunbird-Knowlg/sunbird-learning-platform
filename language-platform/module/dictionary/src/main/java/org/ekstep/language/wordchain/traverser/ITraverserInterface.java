package org.ekstep.language.wordchain.traverser;

import java.util.List;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;

public interface ITraverserInterface {

	public TraversalDescription getTraversalDescription(int traversalDepth, String graphId);

	public Traverser traverse(final Node startNode, int traversalDepth, String graphId, List<Evaluator> evaluators);

}
