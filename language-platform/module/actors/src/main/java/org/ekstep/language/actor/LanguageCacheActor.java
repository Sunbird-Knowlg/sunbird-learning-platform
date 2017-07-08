package org.ekstep.language.actor;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.language.cache.GradeComplexityCache;
import org.ekstep.language.common.LanguageBaseActor;
import org.ekstep.language.common.enums.LanguageErrorCodes;
import org.ekstep.language.common.enums.LanguageOperations;
import org.ekstep.language.common.enums.LanguageParams;
import org.ekstep.language.util.GradeLevelComplexityUtil;

import com.ilimi.common.dto.Request;
import com.ilimi.common.exception.ClientException;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.common.util.PlatformLogger;
import com.ilimi.graph.dac.model.Node;

import akka.actor.ActorRef;

/**
 * The Class LanguageCacheActor, to provide basic akka actor functionality for language caching
 * operations
 *
 * @author karthik
 */
public class LanguageCacheActor extends LanguageBaseActor {

	/** The logger. */
	private static ILogger LOGGER = PlatformLogManager.getLogger();

	/** The util. */
	private GradeLevelComplexityUtil util = new GradeLevelComplexityUtil();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ilimi.graph.common.mgr.BaseGraphManager#onReceive(java.lang.Object)
	 */
	@Override
	public void onReceive(Object msg) throws Exception {
		// TODO Auto-generated method stub
		LOGGER.log("Received Command: " + msg);
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
				LOGGER.log("Unsupported operation: " + operation);
				throw new ClientException(LanguageErrorCodes.ERR_INVALID_OPERATION.name(),
						"Unsupported operation: " + operation);
			}
		} catch (Exception e) {
			LOGGER.log("Error in enrich actor", e.getMessage(), e);
			handleException(e, getSender());
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ilimi.graph.common.mgr.BaseGraphManager#invokeMethod(com.ilimi.common
	 * .dto.Request, akka.actor.ActorRef)
	 */
	@Override
	protected void invokeMethod(Request request, ActorRef parent) {
	}

}
