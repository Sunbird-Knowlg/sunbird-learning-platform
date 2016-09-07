package org.ekstep.language.wordchian;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.language.cache.VarnaCache;
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
				if (arpabetLength == 2)
					return arpabetArr[1];
				VarnaCache varnaCache = VarnaCache.getInstance();
				String last = arpabetArr[arpabetLength-1];
				String sound = "";
				sound = getRhymingSoundChars(sound, arpabetArr, 2, varnaCache);
				String rhymingText = sound + last; 
				return rhymingText;
			}
		}
		return null;
	}
	
	private String getRhymingSoundChars(String sound, String arpabetArr[], int index, VarnaCache varnaCache) {
		String languageId = "en";
		int arpabetLength = arpabetArr.length;
		String lastVarna = arpabetArr[arpabetLength-index];
		sound = lastVarna + " " + sound;
		Node varna = varnaCache.getVarna(languageId, lastVarna);
		if (null != varna) {
			String type = (String) varna.getMetadata().get("type");
			if (StringUtils.equalsIgnoreCase("Vowel", type)) {
				return sound;
			} else {
				if (index < arpabetLength)
					return getRhymingSoundChars(sound, arpabetArr, index+1, varnaCache);
				else
					return sound;
			}
		} else {
			if (index < arpabetLength)
				return getRhymingSoundChars(sound, arpabetArr, index+1, varnaCache);
			else
				return sound;
		}
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
