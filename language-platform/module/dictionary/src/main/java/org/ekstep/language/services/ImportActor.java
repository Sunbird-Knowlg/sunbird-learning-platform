package org.ekstep.language.services;

import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Request;
import org.ekstep.language.common.LanguageBaseActor;
import org.ekstep.language.common.enums.LanguageOperations;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.importer.ImportDictionary;
import org.ekstep.language.models.DictionaryObject;
import org.ekstep.telemetry.logger.TelemetryManager;

import akka.actor.ActorRef;

public class ImportActor extends LanguageBaseActor {

	

	@Override
	public void onReceive(Object msg) throws Exception {
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
					TelemetryManager.log("Unsupported operation: " + operation);
					unhandled(msg);
				}
			} catch (Exception e) {
				handleException(e, getSender());
			}
		} else {
			TelemetryManager.log("Unsupported operation!");
			unhandled(msg);
		}

	}

	@Override
	protected void invokeMethod(Request request, ActorRef parent) {
	}
}