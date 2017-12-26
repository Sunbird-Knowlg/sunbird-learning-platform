package org.ilimi.wordchain;


import org.neo4j.graphdb.RelationshipType;

public enum Rels implements RelationshipType
{
    startsWith, endsWith, startsWithAkshara, endsWithAkshara, hasRhymingSound, hasMember, follows
}