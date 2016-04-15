package org.ekstep.language.measures.actor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.common.LanguageBaseActor;
import org.ekstep.language.common.enums.LanguageErrorCodes;
import org.ekstep.language.common.enums.LanguageOperations;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.measures.ParagraphMeasures;
import org.ekstep.language.measures.WordMeasures;
import org.ekstep.language.measures.entity.ComplexityMeasures;
import org.ekstep.language.measures.entity.ParagraphComplexity;
import org.ekstep.language.measures.entity.WordComplexity;
import org.ekstep.language.measures.meta.OrthographicVectors;
import org.ekstep.language.measures.meta.PhonologicVectors;
import org.ekstep.language.measures.meta.SyllableMap;

import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ClientException;

import akka.actor.ActorRef;

public class LexileMeasuresActor extends LanguageBaseActor {

	private static Logger LOGGER = LogManager.getLogger(LexileMeasuresActor.class.getName());

	@SuppressWarnings("unchecked")
	@Override
	public void onReceive(Object msg) throws Exception {
		LOGGER.info("Received Command: " + msg);
		Request request = (Request) msg;
		String languageId = (String) request.getContext().get(LanguageParams.language_id.name());
		String operation = request.getOperation();
		try {
			if (StringUtils.equalsIgnoreCase(LanguageOperations.computeWordComplexity.name(), operation)) {
				String word = (String) request.get(LanguageParams.word.name());
				WordComplexity wc = WordMeasures.getWordComplexity(languageId, word);
				OK(LanguageParams.word_complexity.name(), wc.getMeasures(), getSender());
			} else if (StringUtils.equalsIgnoreCase(LanguageOperations.computeTextComplexity.name(), operation)) {
				String text = (String) request.get(LanguageParams.text.name());
				ParagraphComplexity pc = ParagraphMeasures.getTextComplexity(languageId, text);
				OK(LanguageParams.text_complexity.name(), pc, getSender());
			} else if (StringUtils.equalsIgnoreCase(LanguageOperations.analyseTexts.name(), operation)) {
			    Map<String, String> texts = (Map<String, String>) request.get(LanguageParams.texts.name());
			    Map<String, Object> response = ParagraphMeasures.analyseTexts(languageId, texts);
                OK(LanguageParams.text_complexity.name(), response, getSender());
            } else if (StringUtils.equalsIgnoreCase(LanguageOperations.loadLanguageVectors.name(), operation)) {
				SyllableMap.loadSyllables(languageId);
				OrthographicVectors.load(languageId);
				PhonologicVectors.load(languageId);
				OK(getSender());
			} else if (StringUtils.equalsIgnoreCase(LanguageOperations.computeComplexity.name(), operation)) {
				List<String> words = (List<String>) request.get(LanguageParams.words.name());
				List<String> texts = (List<String>) request.get(LanguageParams.texts.name());
				Map<String, ComplexityMeasures> map = new HashMap<String, ComplexityMeasures>();
				if (null != words && !words.isEmpty()) {
					for (String word : words) {
						map.put(word, WordMeasures.getWordComplexity(languageId, word).getMeasures());
					}
				}
				if (null != texts && !texts.isEmpty()) {
					for (String text : texts) {
						map.put(text, ParagraphMeasures.getTextComplexity(languageId, text).measures());
					}
				}
				OK(LanguageParams.complexity_measures.name(), map, getSender());
			} else if (StringUtils.equalsIgnoreCase(LanguageOperations.getWordFeatures.name(), operation)) {
				List<String> words = (List<String>) request.get(LanguageParams.words.name());
				if (null == words) {
					words = new ArrayList<String>();
				}
				String word = (String) request.get(LanguageParams.word.name());
				if (StringUtils.isNotBlank(word))
					words.add(word);

				Map<String, WordComplexity> map = new HashMap<String, WordComplexity>();
				if (null != words && !words.isEmpty()) {
					for (String w : words) {
						map.put(w, WordMeasures.getWordComplexity(languageId, w));
					}
				}
				OK(LanguageParams.word_features.name(), map, getSender());
			} else {
				LOGGER.info("Unsupported operation: " + operation);
				throw new ClientException(LanguageErrorCodes.ERR_INVALID_OPERATION.name(),
						"Unsupported operation: " + operation);
			}
		} catch (Exception e) {
			handleException(e, getSender());
		}
	}

	@Override
	protected void invokeMethod(Request request, ActorRef parent) {
	}
}