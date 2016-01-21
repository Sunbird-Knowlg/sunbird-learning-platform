package org.ekstep.language.actor;

import java.net.UnknownHostException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.common.LanguageBaseActor;
import org.ekstep.language.common.enums.LanguageOperations;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.parser.SSFParser;
import org.ekstep.language.util.ElasticSearchUtil;

import akka.actor.ActorRef;

import com.ilimi.common.dto.Request;

public class IndexesActor extends LanguageBaseActor {

	private static Logger LOGGER = LogManager.getLogger(IndexesActor.class
			.getName());

	@SuppressWarnings("unchecked")
	@Override
	public void onReceive(Object msg) throws Exception {
		LOGGER.info("Received Command: " + msg);
		if (msg instanceof Request) {
			Request request = (Request) msg;
			String languageId = (String) request.getContext().get(
					LanguageParams.language_id.name());
			String operation = request.getOperation();
			if (StringUtils.equalsIgnoreCase(
					LanguageOperations.loadCitations.name(), operation)) {
				String filePathOnServer = (String) request
						.get(LanguageParams.file_path.name());
				String sourceType = (String) request
						.get(LanguageParams.source_type.name());
				String grade = (String) request
						.get(LanguageParams.grade.name());
				SSFParser.parseSsfFilesFolder(filePathOnServer, sourceType, grade,
						languageId);
				OK(getSender());
			} else if (StringUtils.equalsIgnoreCase(
					LanguageOperations.citationsCount.name(), operation)) {
				List<String> words = (List<String>) request.get(LanguageParams.words.name());
				String sourceType = request
						.get(LanguageParams.source_type.name()) != null ? (String) request
						.get(LanguageParams.source_type.name()): null;
				String grade = request
						.get(LanguageParams.grade.name()) != null ? (String) request
						.get(LanguageParams.grade.name()):null;
				String pos =  request
						.get(LanguageParams.pos.name()) != null ? (String) request
						.get(LanguageParams.pos.name()):null;
				String fromDate = request
						.get(LanguageParams.from_date.name()) != null ? (String) request
						.get(LanguageParams.from_date.name()) : null;
				String toDate = request
						.get(LanguageParams.to_date.name()) != null ? (String) request
						.get(LanguageParams.to_date.name()) : null;
				getCitationsCount(words, sourceType, grade, pos, fromDate, toDate);
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

	private void getCitationsCount(List<String> words, String sourceType,
			String grade, String pos, String fromDate, String toDate) throws UnknownHostException {
		
		ElasticSearchUtil util =  new ElasticSearchUtil();
		util.search();
		
	}

	@Override
	protected void invokeMethod(Request request, ActorRef parent) {
	}
}