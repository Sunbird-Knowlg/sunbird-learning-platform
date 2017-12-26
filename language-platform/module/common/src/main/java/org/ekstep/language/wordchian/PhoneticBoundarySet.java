package org.ekstep.language.wordchian;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.logger.PlatformLogger;
import org.ekstep.graph.dac.enums.RelationTypes;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.dac.model.Relation;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.measures.entity.WordComplexity;

// TODO: Auto-generated Javadoc
/**
 * The Class PhoneticBoundarySet, provides functionality to add word into its
 * corresponding PrefixBoundary and SuffixBoundary WordSets
 *
 * @author karthik
 */
public class PhoneticBoundarySet extends BaseWordSet {

	/** The starts with akshara. */
	private String startsWithAkshara;

	/** The ends with akshara. */
	private List<String> endsWithAksharas;

	/** The Constant STARTS_WITH. */
	private static final String STARTS_WITH = "startsWith";

	/** The Constant ENDS_WITH. */
	private static final String ENDS_WITH = "endsWith";

	/**
	 * Instantiates a new phonetic boundary set.
	 *
	 * @param languageId
	 *            the language id
	 * @param wordNode
	 *            the word node
	 * @param wc
	 *            the wc
	 * @param existingWordChainRelatios
	 *            the existing word chain relatios
	 */
	public PhoneticBoundarySet(String languageId, Node wordNode, WordComplexity wc,
			List<Relation> existingWordChainRelatios) {
		super(languageId, wordNode, wc, existingWordChainRelatios);
		init();
	}

	/**
	 * Inits startWithAkshara and endsWithAkshara based on its language.
	 */
	private void init() {
		if (languageId.equalsIgnoreCase("en")) {
			EnglishWordUtil util = new EnglishWordUtil(wordNode);
			startsWithAkshara = util.getFirstAkshara();
			endsWithAksharas = util.getLastAksharas();
		} else {
			IndicWordUtil util = new IndicWordUtil(languageId, wc);
			startsWithAkshara = util.getFirstAkshara();
			endsWithAksharas = util.getLastAksharas();
		}
	}

	/**
	 * Creates the PhoneticBoundarySets for startsWithAkshara and
	 * endsWithAkshara if it is not found in existing relations
	 */
	public void create() {

		if (!isExist(LanguageParams.PrefixBoundary.name(), STARTS_WITH + "_" + startsWithAkshara))
			createPhoneticBoundarySet(startsWithAkshara, LanguageParams.PrefixBoundary.name());

		if (!isExist(LanguageParams.SuffixBoundary.name(), endsWithAksharas)) {
			for (String lemma : endsWithAksharas) {
				createPhoneticBoundarySet(lemma, LanguageParams.SuffixBoundary.name());
			}
		}

	}

	/**
	 * Creates the phonetic boundary sets. each PhoneticBoundary set will have
	 * its own connecting PhonecticBoundary set. for ex: startsWith_T set will
	 * be associated with endsWith_T set with "follows" relation to form
	 * word_chains through traversal
	 * 
	 * @param lemma
	 *            the lemma
	 * @param type
	 *            the type
	 */
	private void createPhoneticBoundarySet(String lemma, String type) {

		String phoneticBoundarySetID;
		String connectingPBSetID;
		String actualLemma;
		String connectingLemma;

		PlatformLogger.log("create " + type + " set " + lemma + "for the word"
				+ (String) wordNode.getMetadata().get(LanguageParams.lemma.name()));

		if (type.equalsIgnoreCase(LanguageParams.PrefixBoundary.name())) {
			actualLemma = STARTS_WITH + "_" + lemma;
			connectingLemma = ENDS_WITH + "_" + lemma;
			phoneticBoundarySetID = getWordSet(actualLemma, type);
			connectingPBSetID = getWordSet(connectingLemma, LanguageParams.SuffixBoundary.name());
		} else {
			actualLemma = ENDS_WITH + "_" + lemma;
			connectingLemma = STARTS_WITH + "_" + lemma;
			phoneticBoundarySetID = getWordSet(actualLemma, type);
			connectingPBSetID = getWordSet(connectingLemma, LanguageParams.PrefixBoundary.name());
		}

		boolean followRelCreate = false;

		// when connecting PhoneticBoundary set is found and actual
		// PhonecticBoundary set is yet to be created
		// need to create follows relation between them first time
		if (StringUtils.isBlank(phoneticBoundarySetID) && StringUtils.isNotBlank(connectingPBSetID)) {
			followRelCreate = true;
		}

		if (StringUtils.isBlank(phoneticBoundarySetID)) {
			phoneticBoundarySetID = createWordSetCollection(actualLemma, type);
		} else {
			addMemberToSet(phoneticBoundarySetID);
		}

		if (followRelCreate) {
			if (type.equalsIgnoreCase(LanguageParams.PrefixBoundary.name()))
				createRelation(connectingPBSetID, phoneticBoundarySetID, RelationTypes.FOLLOWS.relationName());
			else
				createRelation(phoneticBoundarySetID, connectingPBSetID, RelationTypes.FOLLOWS.relationName());
		}
	}

}
