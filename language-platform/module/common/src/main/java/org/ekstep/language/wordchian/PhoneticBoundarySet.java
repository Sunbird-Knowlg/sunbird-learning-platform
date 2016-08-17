package org.ekstep.language.wordchian;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.measures.entity.WordComplexity;

import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.model.Node;

public class PhoneticBoundarySet extends BaseWordSet {

	private static Logger LOGGER =  LogManager.getLogger(RhymingSoundSet.class.getName());
	private String startsWithAkshara;
	private List<String> endsWithAkshara;
	private static final String STARTS_WITH = "startsWith";
	private static final String ENDS_WITH = "endsWith";

	
	public PhoneticBoundarySet(String languageId, Node wordNode, WordComplexity wc) {
		super(languageId, wordNode, wc, LOGGER);
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
		createPhoneticBoundarySet(startsWithAkshara, LanguageParams.PrefixBoundary.name());
		for(String lemma : endsWithAkshara)
			createPhoneticBoundarySet(lemma, LanguageParams.SuffixBoundary.name());
	}

	private void createPhoneticBoundarySet(String lemma, String type){

		String phoneticBoundarySetID;
		String connectingPBSetID;
		String actualLemma;
		String connectingLemma;
		
		if(type.equalsIgnoreCase(LanguageParams.PrefixBoundary.name())){
			actualLemma = STARTS_WITH + "_" + lemma;
			connectingLemma = ENDS_WITH + "_" + lemma;
			phoneticBoundarySetID = getWordSet(languageId, actualLemma, type);
			connectingPBSetID = getWordSet(languageId, connectingLemma, LanguageParams.SuffixBoundary.name());
		}else{
			actualLemma = ENDS_WITH + "_" + lemma;
			connectingLemma = STARTS_WITH + "_" + lemma;
			phoneticBoundarySetID = getWordSet(languageId, actualLemma, type);
			connectingPBSetID = getWordSet(languageId, connectingLemma, LanguageParams.PrefixBoundary.name());
		}
		
		boolean followRelCreate = false;

		if(StringUtils.isBlank(phoneticBoundarySetID) && StringUtils.isNotBlank(connectingPBSetID)){
			followRelCreate = true;
		}
		
		if(StringUtils.isBlank(phoneticBoundarySetID)){
			phoneticBoundarySetID = createWordSetCollection(languageId, wordNode.getIdentifier(), actualLemma, type);
		}else{
			addMemberToSet(languageId, phoneticBoundarySetID, wordNode.getIdentifier());
		}

		if(followRelCreate){
			if(type.equalsIgnoreCase(LanguageParams.PrefixBoundary.name()))
				createRelation(languageId, connectingPBSetID, phoneticBoundarySetID, RelationTypes.FOLLOWS.relationName());
			else
				createRelation(languageId, phoneticBoundarySetID, connectingPBSetID, RelationTypes.FOLLOWS.relationName());
		}
	}
	
}
