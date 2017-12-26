package org.ekstep.wordchain;


import org.neo4j.graphdb.RelationshipType;

public enum Rels implements RelationshipType
{
    startsWith, endsWith, startsWithAkshara, endsWithAkshara, hasRhymingSound, hasMember, follows
}