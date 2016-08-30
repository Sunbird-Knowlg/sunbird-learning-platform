package org.ekstep.language.wordchian;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.util.WordCacheUtil;

import com.ilimi.graph.dac.model.Node;

// TODO: Auto-generated Javadoc
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
	 * Gets the rhyming sound.
	 *
	 * @return the rhyming sound
	 */
	public String getRhymingSound() {
		String lemma = (String) wordNode.getMetadata().get(LanguageParams.lemma.name());
		String arpabets = WordCacheUtil.getArpabets(lemma);
		if (!StringUtils.isEmpty(arpabets)) {
			String arpabetArr[] = arpabets.split("\\s");
			int arpabetLength = arpabetArr.length;
			if (arpabetLength > 1) {
				String rhymingText = (arpabetLength > 3)
						? (arpabetArr[arpabetLength - 2] + " " + arpabetArr[arpabetLength - 1])
						: (arpabetArr[arpabetLength - 1]);
				return rhymingText;
			}
		}
		return null;
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
