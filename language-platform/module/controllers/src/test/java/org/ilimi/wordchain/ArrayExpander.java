package org.ilimi.wordchain;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.traversal.BranchState;

@SuppressWarnings("rawtypes")
public class ArrayExpander implements PathExpander {

	private Direction[] directions;
	private RelationshipType[][] types;

	public ArrayExpander(Direction[] directions, RelationshipType[][] types) {
		this.types = types;
		this.directions = directions;
	}

	public Iterable<Relationship> expand(Path path, BranchState state) {
		return path.endNode().getRelationships(directions[path.length()%3], types[path.length()%3]);
	}

	public ArrayExpander reverse() {
		return new ArrayExpander(directions, types);
	}
}