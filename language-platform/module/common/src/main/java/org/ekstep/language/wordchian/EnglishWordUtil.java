package org.ekstep.language.wordchian;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.language.cache.VarnaCache;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.util.WordCacheUtil;

import com.ilimi.graph.dac.model.Node;

/**
 * The Class EnglishWordUtil, utility class provides functionality to find out
 * FirstAkshara, LastAkasharas and RhymingSound text for English language
 *
 * @author karthik
 */
public class EnglishWordUtil {

	/** The word node. */
	private Node wordNode;

	/**
	 * Instantiates a new english word util.
	 *
	 * @param wordNode
	 *            the word node
	 */
	public EnglishWordUtil(Node wordNode) {
		this.wordNode = wordNode;
	}

	/**
	 * Gets the rhyming sound of the current word.
	 *
	 * @return the rhyming sound
	 */
	public String getRhymingSound() {
		String lemma = (String) wordNode.getMetadata().get(LanguageParams.lemma.name());
		String arpabets = WordCacheUtil.getArpabets(lemma);
		if (StringUtils.isNotBlank(arpabets)) {
			String arpabetArr[] = arpabets.split("\\s");
			int arpabetLength = arpabetArr.length;
			if (arpabetLength > 1) {
				// if word has only two arpabets, last char is the rhyming sound
				if (arpabetLength == 2)
					return arpabetArr[1];
				VarnaCache varnaCache = VarnaCache.getInstance();
				// last character is always part of the rhyming sound
				String last = arpabetArr[arpabetLength - 1];
				String sound = "";
				// get the remaining prefix of the rhyming sound
				sound = getRhymingSoundChars(sound, arpabetArr, 2, varnaCache);
				String rhymingText = sound + last;
				return rhymingText;
			}
		}
		return null;
	}

	/**
	 * Recursive function to get the rhyming sounds of words with more than 2
	 * arpabets. This function returns the rhyming sound of the word, excluding
	 * the last arpabet. 
	 * Iterates through the arpabets of the given word and gets the rhyming sound using the following rules: 
	 * - if last character is vowel, return the character as the rhyming soung 
	 * - else if last character is consonant: 
	 * 		- else if no more characters are left, add the character to rhyming sound and return 
	 * 		- else if word has more characters, add the character to rhyming sound and repeat the process
	 * 
	 * @param sound rhyming sound string that is updated by this method
	 * @param arpabetArr list of arpabets of the input word
	 * @param index the arpabets list index from which the rhyming sound
	 * @param varnaCache the VarnaCache object
	 * @return rhyming sound of the word
	 */
	private String getRhymingSoundChars(String sound, String arpabetArr[], int index, VarnaCache varnaCache) {
		String languageId = "en";
		int arpabetLength = arpabetArr.length;
		String lastVarna = arpabetArr[arpabetLength - index];
		sound = lastVarna + " " + sound;
		Node varna = varnaCache.getVarna(languageId, lastVarna);
		if (null != varna) {
			String type = (String) varna.getMetadata().get("type");
			if (StringUtils.equalsIgnoreCase("Vowel", type)) {
				return sound;
			} else {
				if (index < arpabetLength)
					return getRhymingSoundChars(sound, arpabetArr, index + 1, varnaCache);
				else
					return sound;
			}
		} else {
			if (index < arpabetLength)
				return getRhymingSoundChars(sound, arpabetArr, index + 1, varnaCache);
			else
				return sound;
		}
	}

	/**
	 * Gets the first akshara.
	 *
	 * @return the first akshara
	 */
	public String getFirstAkshara() {
		String lemma = (String) wordNode.getMetadata().get(LanguageParams.lemma.name());
		String text = "" + lemma.charAt(0);
		return text;
	}

	/**
	 * Gets the last aksharas.
	 *
	 * @return the last aksharas
	 */
	public List<String> getLastAksharas() {
		String lemma = (String) wordNode.getMetadata().get(LanguageParams.lemma.name());
		String text = "" + lemma.charAt(lemma.length() - 1);
		return Arrays.asList(text);
	}
}
