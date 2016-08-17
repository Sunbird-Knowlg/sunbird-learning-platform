package org.ekstep.language.wordchian;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.util.WordCacheUtil;

import com.ilimi.graph.dac.model.Node;

public class EnglishWordUtil {
	private Node wordNode;
	
	public EnglishWordUtil(Node wordNode){
		this.wordNode = wordNode;
	}
	
	public String getRhymingSound(){
		String lemma = (String) wordNode.getMetadata().get(LanguageParams.lemma.name());
		String arpabets = WordCacheUtil.getArpabets(lemma);
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

	public String getFirstAkshara() {
		String lemma = (String) wordNode.getMetadata().get(LanguageParams.lemma.name());
		String text = "" + lemma.charAt(0);
		return text;
	}

	public List<String> getLastAkshara() {
		String lemma = (String) wordNode.getMetadata().get(LanguageParams.lemma.name());
		String text = "" + lemma.charAt(lemma.length()-1);
		return Arrays.asList(text);
	}
}
