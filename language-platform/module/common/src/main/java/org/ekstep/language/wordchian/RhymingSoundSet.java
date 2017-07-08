package org.ekstep.language.wordchian;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.measures.entity.WordComplexity;

import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;

/**
 * The Class RhymingSoundSet, provides functionality to add word into its
 * corresponding RhymingSound WordSet
 *
 * @author karthik
 */
public class RhymingSoundSet extends BaseWordSet {

	/** The logger. */
	private static ILogger LOGGER = PlatformLogManager.getLogger();

	/** The rhyming sound. */
	private String rhymingSound;

	/** The Constant RHYMING_SOUND. */
	private static final String RHYMING_SOUND = "rhymingSound";

	/**
	 * Instantiates a new rhyming sound set.
	 *
	 * @param languageId
	 *            the language id
	 * @param wordNode
	 *            the word node
	 * @param wc
	 *            the wc
	 * @param existingWordSetRelatios
	 *            the existing word set relatios
	 */
	public RhymingSoundSet(String languageId, Node wordNode, WordComplexity wc,
			List<Relation> existingWordSetRelatios) {
		super(languageId, wordNode, wc, existingWordSetRelatios, LOGGER);
		init();
	}

	/**
	 * Inits rhymingSound based on its language.
	 */
	private void init() {
		if (languageId.equalsIgnoreCase("en")) {
			rhymingSound = new EnglishWordUtil(wordNode).getRhymingSound();
		} else {
			rhymingSound = new IndicWordUtil(languageId, wc).getRhymingSound();
		}
	}

	/**
	 * Creates the RhymingSoundSet if rhymingSound is not found in existing
	 * relations
	 */
	public void create(){
		LOGGER.log("Rhyming sound is " + rhymingSound);
		if(StringUtils.isNotBlank(rhymingSound)){
			String rhymingSoundLemma = RHYMING_SOUND + "_" + rhymingSound;
			if(!isExist(LanguageParams.RhymingSound.name(), rhymingSoundLemma))
				createRhymingSoundSet(rhymingSoundLemma);
		} else {
			LOGGER.log("Deleting existing rhyming sound relation");
			removeSetRelation(LanguageParams.RhymingSound.name());
		}
	}

	/**
	 * Creates the rhyming sound set if it is not already existed and add word
	 * into member of RhymingSound set
	 *
	 * @param rhymingSound
	 *            the rhyming sound
	 */
	private void createRhymingSoundSet(String rhymingSound) {
		LOGGER.log("create RhymingSound " + rhymingSound + "for the word"
				+ (String) wordNode.getMetadata().get(LanguageParams.lemma.name()));

		String setId = getWordSet(rhymingSound, LanguageParams.RhymingSound.name());

		if (StringUtils.isBlank(setId)) {
			// create RhymingSound set and add word as member if RhymingSound is
			// not existed
			createWordSetCollection(rhymingSound, LanguageParams.RhymingSound.name());
		} else {
			// add word as member of RhymingSound
			addMemberToSet(setId);
		}
	}

}
