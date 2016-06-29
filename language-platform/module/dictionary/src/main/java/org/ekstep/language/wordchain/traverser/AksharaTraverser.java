package org.ekstep.language.wordchain.traverser;

import org.ekstep.language.wordchain.WordChainRelations;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Uniqueness;

import com.ilimi.graph.dac.util.Neo4jGraphFactory;

public class AksharaTraverser extends AbstractTraverser {

	@Override
	public TraversalDescription getTraversalDescription(int maxTraversalDepth, int minTraversalDepth, String graphId) {
		GraphDatabaseService graphDb = Neo4jGraphFactory.getGraphDb(graphId);
		traversalDescription = graphDb.traversalDescription().depthFirst()
				//.relationships(WordChainRelations.endsWithAkshara, Direction.OUTGOING)
				//.relationships(WordChainRelations.startsWithAkshara, Direction.INCOMING)
				.relationships(WordChainRelations.endsWith, Direction.OUTGOING)
				.relationships(WordChainRelations.startsWith, Direction.OUTGOING)
				.uniqueness(Uniqueness.NODE_GLOBAL)
				.uniqueness(Uniqueness.RELATIONSHIP_GLOBAL)
				.evaluator(Evaluators.toDepth(maxTraversalDepth))
				.evaluator(Evaluators.fromDepth(minTraversalDepth));
		return traversalDescription;
	}
}
