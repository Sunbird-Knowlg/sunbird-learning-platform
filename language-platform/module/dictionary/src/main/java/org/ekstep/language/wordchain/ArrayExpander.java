package org.ekstep.language.wordchain;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.traversal.BranchState;

@SuppressWarnings("rawtypes")
public class ArrayExpander implements PathExpander {

	private Direction[] directions;
	private RelationshipType[] types;
	private int nodeCount;

	public ArrayExpander(Direction[] directions, RelationshipType[] types, int nodeCount) {
		this.types = types;
		this.directions = directions;
		this.nodeCount = nodeCount;
	}

	public Iterable<Relationship> expand(Path path, BranchState state) {
		return path.endNode().getRelationships(directions[path.length()%nodeCount], types[path.length()%nodeCount]);
	}

	public ArrayExpander reverse() {
		return new ArrayExpander(directions, types, nodeCount);
	}
}