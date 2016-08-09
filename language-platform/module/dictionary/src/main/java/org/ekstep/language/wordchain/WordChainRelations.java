package org.ekstep.language.wordchain;


import org.neo4j.graphdb.RelationshipType;

public enum WordChainRelations implements RelationshipType
{
    startsWithAkshara, endsWithAkshara, startsWithConsonant, endsWithConsonant, hasRhymingSound, startsWith, endsWith, hasMember
}