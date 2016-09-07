package org.ekstep.language.wordchian;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.measures.entity.WordComplexity;

import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;

public class RhymingSoundSet extends BaseWordSet {

	private static Logger LOGGER =  LogManager.getLogger(RhymingSoundSet.class.getName());
	private String rhymingSound;
	private static final String RHYMING_SOUND = "rhymingSound";
	
	public RhymingSoundSet(String languageId, Node wordNode, WordComplexity wc, List<Relation> existingWordSetRelatios) {
		super(languageId, wordNode, wc, existingWordSetRelatios, LOGGER);
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
		if(StringUtils.isNotBlank(rhymingSound)){
			String rhymingSoundLemma = RHYMING_SOUND + "_" + rhymingSound;
			if(!isExist(LanguageParams.RhymingSound.name(), rhymingSoundLemma))
				createRhymingSoundSet(rhymingSoundLemma);
		} else
			isExist(LanguageParams.RhymingSound.name(), "");
	}
	
	private void createRhymingSoundSet(String rhymingSound){
		String setId = getWordSet(rhymingSound, LanguageParams.RhymingSound.name());
		if(StringUtils.isBlank(setId)){
			createWordSetCollection(rhymingSound, LanguageParams.RhymingSound.name());
		}else{
			addMemberToSet(setId);
		}
	}

}
