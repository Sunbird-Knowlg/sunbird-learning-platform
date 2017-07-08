package org.ekstep.language.services;

import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.language.common.LanguageBaseActor;
import org.ekstep.language.common.enums.LanguageOperations;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.importer.ImportDictionary;
import org.ekstep.language.models.DictionaryObject;

import com.ilimi.common.dto.Request;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.common.util.PlatformLogger;;
import com.ilimi.common.util.PlatformLogger;

import akka.actor.ActorRef;

public class ImportActor extends LanguageBaseActor {

	private static ILogger LOGGER = PlatformLogManager.getLogger();

	@Override
	public void onReceive(Object msg) throws Exception {
		LOGGER.log("Received Command: " , msg);
		if (msg instanceof Request) {
			Request request = (Request) msg;
			String languageId = (String) request.getContext().get(LanguageParams.language_id.name());
			String operation = request.getOperation();
			try {
				if (StringUtils.equalsIgnoreCase(LanguageOperations.transformWordNetData.name(), operation)) {
					try (InputStream stream = (InputStream) request.get(LanguageParams.input_stream.name())) {
						ImportDictionary id = new ImportDictionary();
						String sourceType = (String) request.get(LanguageParams.source_type.name());
						DictionaryObject dictionaryObject = id.transformData(languageId, sourceType, stream);
						OK(LanguageParams.dictionary.name(), dictionaryObject, getSender());
					}
				} else {
					LOGGER.log("Unsupported operation: " , operation);
					unhandled(msg);
				}
			} catch (Exception e) {
				handleException(e, getSender());
			}
		} else {
			LOGGER.log("Unsupported operation!");
			unhandled(msg);
		}

	}

	@Override
	protected void invokeMethod(Request request, ActorRef parent) {
	}
}