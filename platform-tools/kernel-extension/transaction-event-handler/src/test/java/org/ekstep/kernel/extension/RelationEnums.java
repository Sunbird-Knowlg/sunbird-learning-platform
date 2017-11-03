package org.ekstep.kernel.extension;

import org.neo4j.graphdb.RelationshipType;

public enum RelationEnums implements RelationshipType{
		associatedTo, isParentOf,
		hasSequenceMember;
}
