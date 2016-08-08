package org.ekstep.language.wordchian;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.measures.entity.WordComplexity;
import org.ekstep.language.util.WordCacheUtil;

import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;

public class EnglishLanguageWordSet extends BaseWordSet {


	private WordCacheUtil wordCacheUtil;
    private static Logger LOGGER = LogManager.getLogger(EnglishLanguageWordSet.class.getName());
	
	public EnglishLanguageWordSet(String languageId, Node wordNode, WordComplexity wc){
		super(languageId, wordNode, wc, LOGGER);
		wordCacheUtil = new WordCacheUtil();		
	}
	
	protected String getRymingSoundText(){
		String lemma = (String) wordNode.getMetadata().get(LanguageParams.lemma.name());
		String arpabets = wordCacheUtil.getArpabets(lemma);
		if (!StringUtils.isEmpty(arpabets)){
			String arpabetArr[] = arpabets.split("\\s");
			int arpabetLength = arpabetArr.length;
			if(arpabetLength > 1){
				String rhymingText = (arpabetLength > 3) ? (arpabetArr[arpabetLength-2] + " " + arpabetArr[arpabetLength -1]) : (arpabetArr[arpabetLength -1]); 
				return rhymingText;
			}
		}
		return null;
	}

	@Override
	protected String getStartsWithAksharaText() {
		String lemma = (String) wordNode.getMetadata().get(LanguageParams.lemma.name());
		String text = "" + lemma.charAt(0);
		return text;
	}

	@Override
	protected List<String> getEndsWithAksharaText() {
		String lemma = (String) wordNode.getMetadata().get(LanguageParams.lemma.name());
		String text = "" + lemma.charAt(lemma.length()-1);
		return Arrays.asList(text);
	}

}
