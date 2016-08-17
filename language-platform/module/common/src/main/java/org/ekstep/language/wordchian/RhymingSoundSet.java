package org.ekstep.language.wordchian;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.measures.entity.WordComplexity;

import com.ilimi.graph.dac.model.Node;

public class RhymingSoundSet extends BaseWordSet {

	private static Logger LOGGER =  LogManager.getLogger(RhymingSoundSet.class.getName());
	private String rhymingSound;
	private static final String RHYMING_SOUND = "rhymingSound";
	
	public RhymingSoundSet(String languageId, Node wordNode, WordComplexity wc) {
		super(languageId, wordNode, wc, LOGGER);
		init();
	}

	private void init(){
		if(languageId.equalsIgnoreCase("en")){
			rhymingSound = new EnglishWordUtil(wordNode).getRhymingSound();
		}else{
			rhymingSound = new IndicWordUtil(languageId, wc).getRymingSound();
		}
	}
	
	public void create(){
		String rhymingSoundText = RHYMING_SOUND + "_" + rhymingSound;

		if(StringUtils.isNotBlank(rhymingSoundText)){
			createRhymingSoundSet(rhymingSoundText);
		}
	}
	
	private void createRhymingSoundSet(String rhymingSound){
		String setId = getWordSet(languageId, rhymingSound, LanguageParams.RhymingSound.name());
		if(StringUtils.isBlank(setId)){
			createWordSetCollection(languageId, wordNode.getIdentifier(), rhymingSound, LanguageParams.RhymingSound.name());
		}else{
			addMemberToSet(languageId, setId,  wordNode.getIdentifier());
		}
	}

}
