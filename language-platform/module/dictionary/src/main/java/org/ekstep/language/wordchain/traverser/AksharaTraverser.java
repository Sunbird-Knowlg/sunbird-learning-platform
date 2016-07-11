package org.ekstep.language.wordchain.traverser;

import org.ekstep.language.wordchain.WordChainRelations;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Uniqueness;

public class AksharaTraverser extends AbstractTraverser {

	@Override
	public TraversalDescription enhanceTraversalDescription(int maxTraversalDepth, int minTraversalDepth, String graphId) {
		traversalDescription = traversalDescription
				.relationships(WordChainRelations.endsWithAkshara, Direction.OUTGOING)
				.relationships(WordChainRelations.startsWithAkshara, Direction.INCOMING)
				.uniqueness(Uniqueness.NODE_GLOBAL)
				.uniqueness(Uniqueness.RELATIONSHIP_GLOBAL)
				.evaluator(Evaluators.toDepth(maxTraversalDepth))
				.evaluator(Evaluators.fromDepth(minTraversalDepth));
		return traversalDescription;
	}

}
