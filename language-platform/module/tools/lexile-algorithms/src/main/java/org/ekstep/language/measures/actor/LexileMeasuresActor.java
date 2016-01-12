package org.ekstep.language.measures.actor;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.common.LanguageBaseActor;
import org.ekstep.language.common.enums.LanguageOperations;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.measures.ParagraphMeasures;
import org.ekstep.language.measures.WordMeasures;
import org.ekstep.language.measures.entity.ParagraphComplexity;
import org.ekstep.language.measures.entity.WordComplexity;
import org.ekstep.language.measures.meta.OrthographicVectors;
import org.ekstep.language.measures.meta.PhonologicVectors;
import org.ekstep.language.measures.meta.SyllableMap;

import com.ilimi.common.dto.Request;

import akka.actor.ActorRef;

public class LexileMeasuresActor extends LanguageBaseActor {

    private static Logger LOGGER = LogManager.getLogger(LexileMeasuresActor.class.getName());

    @Override
    public void onReceive(Object msg) throws Exception {
        LOGGER.info("Received Command: " + msg);
        if (msg instanceof Request) {
            Request request = (Request) msg;
            String operation = request.getOperation();
            if (StringUtils.equalsIgnoreCase(LanguageOperations.getWordComplexity.name(), operation)) {
                String word = (String) request.get(LanguageParams.word.name());
                String languageId = (String) request.get(LanguageParams.language_id.name());
                WordComplexity wc = WordMeasures.getWordComplexity(languageId, word);
                OK(LanguageParams.word_complexity.name(), wc, getSender());
            } else if (StringUtils.equalsIgnoreCase(LanguageOperations.getTextComplexity.name(), operation)) {
                String text = (String) request.get(LanguageParams.text.name());
                String languageId = (String) request.get(LanguageParams.language_id.name());
                ParagraphComplexity pc = ParagraphMeasures.getTextComplexity(languageId, text);
                OK(LanguageParams.text_complexity.name(), pc, getSender());
            } else if (StringUtils.equalsIgnoreCase(LanguageOperations.loadLanguageVectors.name(), operation)) {
                String languageId = (String) request.get(LanguageParams.language_id.name());
                SyllableMap.loadSyllables(languageId);
                OrthographicVectors.load(languageId);
                PhonologicVectors.load(languageId);
                OK(getSender());
            } else {
                LOGGER.info("Unsupported operation: " + operation);
                unhandled(msg);
            }
        } else {
            LOGGER.info("Unsupported operation!");
            unhandled(msg);
        }
    }

    @Override
    protected void invokeMethod(Request request, ActorRef parent) {
    }
}