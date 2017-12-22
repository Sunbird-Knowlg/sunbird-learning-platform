package org.ekstep.language.transliterate.actor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Request;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.logger.PlatformLogger;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.language.common.LanguageBaseActor;
import org.ekstep.language.common.enums.LanguageErrorCodes;
import org.ekstep.language.common.enums.LanguageOperations;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.util.WordCacheUtil;
import org.ekstep.language.util.WordUtil;

import akka.actor.ActorRef;

// TODO: Auto-generated Javadoc
/**
 * The Class TransliteratorActor, provides akka actor operations for
 * transliterate, phonetic spelling and other related service
 *
 * @author karthik
 */
public class TransliteratorActor extends LanguageBaseActor {

	/** The logger. */
	

	/** The word util. */
	private WordUtil wordUtil = new WordUtil();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.graph.common.mgr.BaseGraphManager#onReceive(java.lang.Object)
	 */
	@Override
	public void onReceive(Object msg) throws Exception {
		Request request = (Request) msg;
		PlatformLogger.log(request.getRequestId() + " | Received Command: " , request);
		String languageId = (String) request.getContext().get(LanguageParams.language_id.name());
		String operation = request.getOperation();
		try {
			if (StringUtils.equalsIgnoreCase(LanguageOperations.getArpabets.name(), operation)) {
				String word = (String) request.get(LanguageParams.word.name());
				String arpabets = WordCacheUtil.getArpabets(word);
				OK(LanguageParams.arpabets.name(), arpabets, getSender());
			} else if (StringUtils.equalsIgnoreCase(LanguageOperations.getSyllables.name(), operation)) {
				String word = (String) request.get(LanguageParams.word.name());
				List<String> syllables = getSyllables(languageId, word);
				OK(LanguageParams.syllables.name(), syllables, getSender());
			} else if (StringUtils.equalsIgnoreCase(LanguageOperations.getPhoneticSpellingByLanguage.name(),
					operation)) {
				String word = (String) request.get(LanguageParams.word.name());
				Boolean addEndVirama = (Boolean) request.get(LanguageParams.addClosingVirama.name());
				if (null == addEndVirama)
					addEndVirama = false;
				String phoneticSpellingOfWord = wordUtil.getPhoneticSpellingByLanguage(languageId, word, addEndVirama);
				OK(LanguageParams.phonetic_spelling.name(), phoneticSpellingOfWord, getSender());
			} else if (StringUtils.equalsIgnoreCase(LanguageOperations.getSimilarSoundWords.name(), operation)) {
				String word = (String) request.get(LanguageParams.word.name());
				Set<String> similarSoundWords = WordCacheUtil.getSimilarSoundWords(word);
				OK(LanguageParams.similar_sound_words.name(), similarSoundWords, getSender());
			} else if (StringUtils.equalsIgnoreCase(LanguageOperations.transliterate.name(), operation)) {
				String text = (String) request.get(LanguageParams.text.name());
				Boolean addEndVirama = (Boolean) request.get(LanguageParams.addClosingVirama.name());
				if (null == addEndVirama)
					addEndVirama = false;
				String translatedText = wordUtil.transliterateText(languageId, text, addEndVirama);
				OK(LanguageParams.output.name(), translatedText, getSender());
			} else if (StringUtils.equalsIgnoreCase(LanguageOperations.loadWordsArpabetsMap.name(), operation)) {
				InputStream in = (InputStream) request.get(LanguageParams.input_stream.name());
				WordCacheUtil.loadWordArpabetCollection(in);
				OK(getSender());
			} else {
				PlatformLogger.log("Unsupported operation: " + operation);
				throw new ClientException(LanguageErrorCodes.ERR_INVALID_OPERATION.name(),
						"Unsupported operation: " + operation);
			}
		} catch (Exception e) {
			handleException(e, getSender());
		}
	}

	/**
	 * Gets the syllables for any given word
	 *
	 * @param languageId
	 *            the language id
	 * @param word
	 *            the word
	 * @return the syllables
	 */
	private List<String> getSyllables(String languageId, String word) {
		Node wordNode = wordUtil.searchWord(languageId, word);

		List<String> syllables = new ArrayList<>();
		if (wordNode != null && wordNode.getMetadata().get("syllables") != null) {
			Object syllablesObj = (Object) wordNode.getMetadata().get("syllables");
			if (syllablesObj instanceof String[]) {
				String[] arr = (String[]) syllablesObj;
				if (null != arr && arr.length > 0) {
					for (String str : arr) {
						syllables.add(str);
					}
				}
			} else if (syllablesObj instanceof String) {
				if (StringUtils.isNotBlank(syllablesObj.toString())) {
					String str = syllablesObj.toString();
					if (StringUtils.isNotBlank(str))
						syllables.add(str.toLowerCase());
				}
			}
		} else {
			syllables = wordUtil.buildSyllables(languageId, word);
		}

		return syllables;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.graph.common.mgr.BaseGraphManager#invokeMethod(org.ekstep.common
	 * .dto.Request, akka.actor.ActorRef)
	 */
	@Override
	protected void invokeMethod(Request request, ActorRef parent) {
	}

}
