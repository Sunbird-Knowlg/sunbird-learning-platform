package org.ekstep.language.actor;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Request;
import org.ekstep.common.exception.ClientException;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.language.cache.GradeComplexityCache;
import org.ekstep.language.common.LanguageBaseActor;
import org.ekstep.language.common.enums.LanguageErrorCodes;
import org.ekstep.language.common.enums.LanguageOperations;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.util.GradeLevelComplexityUtil;
import org.ekstep.telemetry.logger.TelemetryManager;

import akka.actor.ActorRef;

/**
 * The Class LanguageCacheActor, to provide basic akka actor functionality for language caching
 * operations
 *
 * @author karthik
 */
public class LanguageCacheActor extends LanguageBaseActor {

	/** The logger. */
	

	/** The util. */
	private GradeLevelComplexityUtil util = new GradeLevelComplexityUtil();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ekstep.graph.common.mgr.BaseGraphManager#onReceive(java.lang.Object)
	 */
	@Override
	public void onReceive(Object msg) throws Exception {
		// TODO Auto-generated method stub
		TelemetryManager.log("Received Command: " + msg);
		Request request = (Request) msg;
		String languageId = (String) request.getContext().get(LanguageParams.language_id.name());
		String operation = request.getOperation();
		try {
			if (StringUtils.equalsIgnoreCase(LanguageOperations.loadGradeLevelComplexityCache.name(), operation)) {
				GradeComplexityCache.getInstance().loadGradeLevelComplexity(languageId);
				OK(getSender());
			} else if (StringUtils.equalsIgnoreCase(LanguageOperations.loadGradeLevelComplexity.name(), operation)) {
				String node_id = (String) request.get(LanguageParams.node_id.name());
				GradeComplexityCache.getInstance().loadGradeLevelComplexity(languageId, node_id);
				OK(getSender());
			} else if (StringUtils.equalsIgnoreCase(LanguageOperations.getGradeLevelComplexities.name(), operation)) {
				List<Node> gradeLevelComplexities = GradeComplexityCache.getInstance()
						.getGradeLevelComplexity(languageId);
				OK(LanguageParams.grade_level_complexity.name(), gradeLevelComplexities, getSender());
			} else if (StringUtils.equalsIgnoreCase(LanguageOperations.validateComplexityRange.name(), operation)) {
				Node gradeLevelComplexity = (Node) request.get(LanguageParams.grade_level_complexity.name());
				util.validateComplexityRange(languageId, gradeLevelComplexity);
				OK(getSender());
			} else {
				TelemetryManager.log("Unsupported operation: " + operation);
				throw new ClientException(LanguageErrorCodes.ERR_INVALID_OPERATION.name(),
						"Unsupported operation: " + operation);
			}
		} catch (Exception e) {
			TelemetryManager.error("Error in enrich actor" + e.getMessage(), e);
			handleException(e, getSender());
		}

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
