package org.ekstep.language.wordchian;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.measures.entity.WordComplexity;
import org.ekstep.language.util.WordCacheUtil;

import com.ilimi.common.dto.Response;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;

public class EnglishPhoneticBoundary extends BasePhoneticBoundary implements IPhoneticBoundary {

	private String languageId ;
	private Node wordNode;
	private WordCacheUtil wordCacheUtil = new WordCacheUtil();
    private static Logger LOGGER = LogManager.getLogger(EnglishPhoneticBoundary.class.getName());
	
	public EnglishPhoneticBoundary(String languageId, Node wordNode, WordComplexity wc){
		this.languageId = languageId;
		this.wordNode = wordNode;
	}
	
	@Override
	public Relation getStartsWithAksharaRelation() throws Exception {
		String lemma = (String) wordNode.getMetadata().get(LanguageParams.lemma.name());
		String text = "" + lemma.charAt(0);
		Relation startsWithRelation = createPhoneticBoundaryRelation(text, LanguageParams.AksharaBoundary.name(), RelationTypes.STARTS_WITH_AKSHARA.relationName());
		return startsWithRelation;
	}

	@Override
	public List<Relation> getEndsWithAksharaRelation() throws Exception{
		String lemma = (String) wordNode.getMetadata().get(LanguageParams.lemma.name());
		String text = "" + lemma.charAt(lemma.length()-1);
		Relation endsWithRelation = createPhoneticBoundaryRelation(text, LanguageParams.AksharaBoundary.name(), RelationTypes.ENDS_WITH_AKSHARA.relationName());
		return Arrays.asList(endsWithRelation);
	}

	@Override
	public List<Relation> getRhymingSoundRelation() throws Exception {
		String lemma = (String) wordNode.getMetadata().get(LanguageParams.lemma.name());
		String arpabets = wordCacheUtil.getArpabets(lemma);
		List<Relation> relations = null;
		if (!StringUtils.isEmpty(arpabets)){
			String arpabetArr[] = arpabets.split("\\s");
			int arpabetLength = arpabetArr.length;
			if(arpabetLength > 1){
				String rhymingText = (arpabetLength > 3) ? (arpabetArr[arpabetLength-2] + " " + arpabetArr[arpabetLength -1]) : (arpabetArr[arpabetLength -1]); 
				relations = new ArrayList<Relation>();
				Relation rhymingSoundInRelation = createPhoneticBoundaryRelation(rhymingText, LanguageParams.RhymingSound.name(), RelationTypes.RYMING_SOUNDS.relationName());
				relations.add(rhymingSoundInRelation);

				Relation rhymingSoundOutRelation = new Relation(rhymingSoundInRelation.getEndNodeId(), rhymingSoundInRelation.getRelationType(), rhymingSoundInRelation.getStartNodeId());
				rhymingSoundOutRelation.setStartNodeObjectType(rhymingSoundInRelation.getEndNodeObjectType());
				rhymingSoundOutRelation.setEndNodeObjectType(rhymingSoundInRelation.getStartNodeObjectType());
				relations.add(rhymingSoundOutRelation);

				return relations;	
			}
		}
		return null;
	}

	private Relation createPhoneticBoundaryRelation(String pbText, String pbType, String relationShipType) throws Exception{
		String phoneticBoundaryId = getPhoneticBoundary(languageId, pbText, LOGGER);
		if(StringUtils.isEmpty(phoneticBoundaryId)){
			Map<String, Object> obj = new HashMap<String, Object>();
			obj.put(LanguageParams.text.name(), pbText);
			obj.put(LanguageParams.type.name(), pbType);
			Response response = createPhoneticBoundary(languageId, obj, LOGGER);
			phoneticBoundaryId = (String) response.get(GraphDACParams.node_id.name());
		}
		return createRelation(wordNode.getIdentifier(), wordNode.getObjectType(), relationShipType, phoneticBoundaryId, pbType);	
	}
}
