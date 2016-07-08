package org.ekstep.language.wordchian;

import java.util.List;

import com.ilimi.graph.dac.model.Relation;

public interface IPhoneticBoundary {
	
	Relation getStartsWithAksharaRelation() throws Exception;

	List<Relation> getEndsWithAksharaRelation() throws Exception;
	
	List<Relation> getRhymingSoundRelation() throws Exception;
}
