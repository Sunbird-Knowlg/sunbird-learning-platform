package org.ekstep.language.wordchian;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.measures.entity.WordComplexity;

import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;

public class PhoneticBoundarySet extends BaseWordSet {

	private static Logger LOGGER =  LogManager.getLogger(RhymingSoundSet.class.getName());
	private String startsWithAkshara;
	private List<String> endsWithAkshara;
	private static final String STARTS_WITH = "startsWith";
	private static final String ENDS_WITH = "endsWith";

	
	public PhoneticBoundarySet(String languageId, Node wordNode, WordComplexity wc, List<Relation> existingWordChainRelatios) {
		super(languageId, wordNode, wc, existingWordChainRelatios, LOGGER);
		init();
	}

	private void init(){
		if(languageId.equalsIgnoreCase("en")){
			EnglishWordUtil util = new EnglishWordUtil(wordNode); 
			startsWithAkshara = util.getFirstAkshara();
			endsWithAkshara = util.getLastAkshara();
		}else{
			IndicWordUtil util = new IndicWordUtil(languageId, wc);
			startsWithAkshara = util.getFirstAkshara();
			endsWithAkshara = util.getLastAkshara();
		}
	}
	
	public void create(){

		if(!isExist(LanguageParams.PrefixBoundary.name(), STARTS_WITH + "_" + startsWithAkshara))
			createPhoneticBoundarySet(startsWithAkshara, LanguageParams.PrefixBoundary.name());
		
		if(!isExist(LanguageParams.SuffixBoundary.name(), endsWithAkshara)){
			for(String lemma : endsWithAkshara){
				createPhoneticBoundarySet(lemma, LanguageParams.SuffixBoundary.name());						
			}			
		}

	}

	private void createPhoneticBoundarySet(String lemma, String type){

		String phoneticBoundarySetID;
		String connectingPBSetID;
		String actualLemma;
		String connectingLemma;
		
		if(type.equalsIgnoreCase(LanguageParams.PrefixBoundary.name())){
			actualLemma = STARTS_WITH + "_" + lemma;
			connectingLemma = ENDS_WITH + "_" + lemma;
			phoneticBoundarySetID = getWordSet(actualLemma, type);
			connectingPBSetID = getWordSet(connectingLemma, LanguageParams.SuffixBoundary.name());
		}else{
			actualLemma = ENDS_WITH + "_" + lemma;
			connectingLemma = STARTS_WITH + "_" + lemma;
			phoneticBoundarySetID = getWordSet(actualLemma, type);
			connectingPBSetID = getWordSet(connectingLemma, LanguageParams.PrefixBoundary.name());
		}
		
		boolean followRelCreate = false;

		if(StringUtils.isBlank(phoneticBoundarySetID) && StringUtils.isNotBlank(connectingPBSetID)){
			followRelCreate = true;
		}
		
		if(StringUtils.isBlank(phoneticBoundarySetID)){
			phoneticBoundarySetID = createWordSetCollection(actualLemma, type);
		}else{
			addMemberToSet(phoneticBoundarySetID);
		}

		if(followRelCreate){
			if(type.equalsIgnoreCase(LanguageParams.PrefixBoundary.name()))
				createRelation(connectingPBSetID, phoneticBoundarySetID, RelationTypes.FOLLOWS.relationName());
			else
				createRelation(phoneticBoundarySetID, connectingPBSetID, RelationTypes.FOLLOWS.relationName());
		}
	}
	
}
