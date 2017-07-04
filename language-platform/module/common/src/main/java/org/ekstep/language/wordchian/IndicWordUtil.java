package org.ekstep.language.wordchian;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.language.common.enums.LanguageErrorCodes;
import org.ekstep.language.measures.entity.WordComplexity;
import org.ekstep.language.measures.meta.SyllableMap;

import com.ilimi.common.dto.Property;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.mgr.BaseManager;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;
import com.ilimi.graph.engine.router.GraphEngineManagers;

// TODO: Auto-generated Javadoc
/**
 * The Class IndicWordUtil, utility class provides functionality to find out
 * FirstAkshara, LastAkasharas and RhymingSound text for all indian language
 *
 * @author karthik
 */
public class IndicWordUtil extends BaseManager {

	/** The logger. */
	private static PlatformLogger<IndicWordUtil> LOGGER = new PlatformLogger<>(IndicWordUtil.class.getName());

	/** The language id. */
	private String languageId;

	/** The wc. */
	private WordComplexity wc;

	/**
	 * Instantiates a new indic word util.
	 *
	 * @param languageId
	 *            the language id
	 * @param wc
	 *            the wc
	 */
	public IndicWordUtil(String languageId, WordComplexity wc) {
		this.languageId = languageId;
		this.wc = wc;
	}

	/**
	 * Gets the first akshara of given lemma.
	 *
	 * @return the first akshara
	 */
	public String getFirstAkshara() {
		String unicodeNotation = wc.getUnicode().toUpperCase();
		Map<String, String> unicodeTypeMap = wc.getUnicodeTypeMap();
		String syllables[] = StringUtils.split(unicodeNotation);

		String firstSyllable = syllables[0];
		String[] firstSyllableUnicodes = parseUnicodes(firstSyllable);
		String firstCharUnicode = firstSyllableUnicodes[0];

		if (unicodeTypeMap.get(firstCharUnicode).equalsIgnoreCase(SyllableMap.CONSONANT_CODE)
				|| unicodeTypeMap.get(firstCharUnicode).equalsIgnoreCase(SyllableMap.VOWEL_CODE)) {
			String text = getTextValue(firstCharUnicode);
			return text;
		}
		return null;
	}

	/**
	 * Gets the last aksharas. when last character represents vowel_sign, return
	 * its corresponding vowel and consonant character(second last character).
	 * when last character represents default_vowel or close_vowel, return the
	 * consonant character(second last character). when last character
	 * represents consonant, return it
	 * 
	 * @return the last akshara
	 */
	public List<String> getLastAksharas() {

		String unicodeNotation = wc.getUnicode().toUpperCase();
		Map<String, String> unicodeTypeMap = wc.getUnicodeTypeMap();
		String syllables[] = StringUtils.split(unicodeNotation);
		List<String> result = new ArrayList<String>();
		String lastSyllable = syllables[syllables.length - 1];
		String[] lastSyllableUnicodes = parseUnicodes(lastSyllable);
		String lastCharUnicode = lastSyllableUnicodes[lastSyllableUnicodes.length - 1];
		String secondLastCharUnicode = "";

		if (lastSyllableUnicodes.length > 1) {
			secondLastCharUnicode = lastSyllableUnicodes[lastSyllableUnicodes.length - 2];
		}

		if (isDefualtVowel(lastCharUnicode, unicodeTypeMap)) {
			if (StringUtils.isNotEmpty(secondLastCharUnicode)
					&& unicodeTypeMap.get(secondLastCharUnicode).equalsIgnoreCase(SyllableMap.CONSONANT_CODE)) {
				String text = getTextValue(secondLastCharUnicode);
				result.add(text);
			}

		} else if (unicodeTypeMap.get(lastCharUnicode).equalsIgnoreCase(SyllableMap.CONSONANT_CODE)) {
			String text = getTextValue(secondLastCharUnicode);
			result.add(text);
		} else if (unicodeTypeMap.get(lastCharUnicode).equalsIgnoreCase(SyllableMap.VOWEL_SIGN_CODE)
				&& StringUtils.isNotEmpty(secondLastCharUnicode)
				&& unicodeTypeMap.get(secondLastCharUnicode).equalsIgnoreCase(SyllableMap.CONSONANT_CODE)) {
			// get vowel associated with this vowel_sign
			String vowelUnicode = getVowelUnicode(languageId, lastCharUnicode);
			String text = getTextValue(vowelUnicode);
			result.add(text);
			text = getTextValue(secondLastCharUnicode);
			result.add(text);

		} else if (unicodeTypeMap.get(lastCharUnicode).equalsIgnoreCase(SyllableMap.CLOSE_VOWEL_CODE)
				&& StringUtils.isNotEmpty(secondLastCharUnicode)
				&& unicodeTypeMap.get(secondLastCharUnicode).equalsIgnoreCase(SyllableMap.CONSONANT_CODE)) {
			String text = getTextValue(secondLastCharUnicode);
			result.add(text);
		}
		return result;
	}

	/**
	 * Gets the rhyming sound.
	 *
	 * @return the rhyming sound
	 */
	public String getRhymingSound() {

		String unicodeNotation = wc.getUnicode().toUpperCase();
		Map<String, String> unicodeTypeMap = wc.getUnicodeTypeMap();
		String syllables[] = StringUtils.split(unicodeNotation);
		String lastSyllable = syllables[syllables.length - 1];

		if (syllables.length > 1) {
			String secondLastSyllable = syllables[syllables.length - 2];
			String[] secondLastSyllableUnicodes = parseUnicodes(secondLastSyllable);
			String secondLastSyllablelastUnicode = secondLastSyllableUnicodes[secondLastSyllableUnicodes.length - 1];
			String rhymingSoundText = "";
			// add second last syllable into rhymingSoundText when its ends
			// either vowel_sign or close_vowel
			if (!isDefualtVowel(secondLastSyllablelastUnicode, unicodeTypeMap)
					&& (unicodeTypeMap.get(secondLastSyllablelastUnicode).equalsIgnoreCase(SyllableMap.VOWEL_SIGN_CODE)
							|| unicodeTypeMap.get(secondLastSyllablelastUnicode)
									.equalsIgnoreCase(SyllableMap.CLOSE_VOWEL_CODE))) {
				String secondLastSyllableSecondlastUnicode = secondLastSyllableUnicodes[secondLastSyllableUnicodes.length
						- 2];
				if (unicodeTypeMap.get(secondLastSyllableSecondlastUnicode)
						.equalsIgnoreCase(SyllableMap.CONSONANT_CODE)) {
					rhymingSoundText = secondLastSyllableSecondlastUnicode;
				}
			}
			// append last syllable into rhymingSoundText
			rhymingSoundText += lastSyllable;
			rhymingSoundText = rhymingSoundText.replace("\\", " ");
			return rhymingSoundText;
		}
		return null;
	}

	/**
	 * Parses the unicodes from syllable string
	 *
	 * @param syllable
	 *            the syllable
	 * @return the string[]
	 */
	private String[] parseUnicodes(String syllable) {

		String[] syllableUnicodes = syllable.split("\\\\");
		List<String> list = new ArrayList<String>();

		// trim modifier unicode
		for (String s : syllableUnicodes) {
			if (StringUtils.isNotEmpty(s)) {
				if (s.endsWith("M"))
					s = s.substring(0, 4);
				list.add(s);
			}
		}
		return list.toArray(new String[list.size()]);
	}

	/**
	 * Gets the text value from unicode
	 *
	 * @param unicode
	 *            the unicode
	 * @return the text value
	 */
	private String getTextValue(String unicode) {
		int hexVal = Integer.parseInt(unicode, 16);
		return "" + (char) hexVal;
	}

	/**
	 * Checks if is defualt vowel.
	 *
	 * @param unicode
	 *            the unicode
	 * @param unicodeTypeMap
	 *            the unicode type map
	 * @return true, if is defualt vowel
	 */
	private boolean isDefualtVowel(String unicode, Map<String, String> unicodeTypeMap) {
		if (unicodeTypeMap.get(unicode) == null && unicode.length() == 5 && unicode.endsWith("A")) {// default
																									// vowel
			return true;
		}
		return false;
	}

	/**
	 * Gets the vowel unicode associated with given VowelSign unicode
	 *
	 * @param languageId
	 *            the language id
	 * @param vowelSignUnicode
	 *            the vowel sign unicode
	 * @return the vowel unicode
	 */
	@SuppressWarnings("unchecked")
	public String getVowelUnicode(String languageId, String vowelSignUnicode) {
		Property vowelSignProp = new Property(GraphDACParams.unicode.name(), vowelSignUnicode);
		Response varnaRes = getDataNodeByProperty(languageId, vowelSignProp);
		Node varnaNode = null;
		if (!checkError(varnaRes)) {
			List<Node> nodes = (List<Node>) varnaRes.get(GraphDACParams.node_list.name());
			if (null != nodes && nodes.size() > 0)
				varnaNode = nodes.get(0);
			String vowelUnicode = "";
			String langageVarnaType = (String) varnaNode.getMetadata().get(GraphDACParams.type.name());
			if (langageVarnaType.equalsIgnoreCase("VowelSign")) {
				// get vowelSign unicode
				Relation associatedTo = (Relation) varnaNode.getOutRelations().get(0);
				if (associatedTo != null) {
					Map<String, Object> vowelMetaData = associatedTo.getEndNodeMetadata();
					vowelUnicode = (String) vowelMetaData.get(GraphDACParams.unicode.name());
				}
			}
			return vowelUnicode;
		} else
			throw new ServerException(LanguageErrorCodes.ERROR_PHONETIC_BOUNTARY_LOOKUP.name(),
					getErrorMessage(varnaRes));

	}

	/**
	 * Gets the data node by property.
	 *
	 * @param languageId
	 *            the language id
	 * @param property
	 *            the property
	 * @return the data node by property
	 */
	protected Response getDataNodeByProperty(String languageId, Property property) {
		Request request = getRequest(languageId, GraphEngineManagers.SEARCH_MANAGER, "getNodesByProperty");
		request.put(GraphDACParams.metadata.name(), property);
		request.put(GraphDACParams.get_tags.name(), true);
		Response findRes = getResponse(request, LOGGER);
		if (!checkError(findRes)) {
			return findRes;
		}
		return null;
	}

}
