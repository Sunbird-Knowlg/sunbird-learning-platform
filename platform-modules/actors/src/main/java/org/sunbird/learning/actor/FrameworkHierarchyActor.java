package org.sunbird.learning.actor;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Request;
import org.sunbird.common.exception.ClientException;
import org.sunbird.graph.common.mgr.BaseGraphManager;
import org.sunbird.learning.common.enums.LearningErrorCodes;
import org.sunbird.learning.framework.FrameworkHierarchy;
import org.sunbird.learning.framework.FrameworkHierarchyOperations;
import org.sunbird.telemetry.logger.TelemetryManager;

import akka.actor.ActorRef;

import java.util.Map;

/**
 * @author pradyumna
 *
 */
public class FrameworkHierarchyActor extends BaseGraphManager {

	FrameworkHierarchy fwHierarchy = new FrameworkHierarchy();

	/* (non-Javadoc)
	 * @see org.sunbird.graph.common.mgr.BaseGraphManager#invokeMethod(org.sunbird.common.dto.Request, akka.actor.ActorRef)
	 */
	@Override
	protected void invokeMethod(Request request, ActorRef parent) {
		String methodName = request.getOperation();
		try {
			if (StringUtils.isBlank(methodName)) {
				throw new ClientException(LearningErrorCodes.ERR_INVALID_OPERATION.name(),
						"Operation '" + methodName + "' not found");
			} else {
				if (StringUtils.equalsIgnoreCase(FrameworkHierarchyOperations.generateFrameworkHierarchy.name(), methodName)) {
					String id = (String) request.get("identifier");
					fwHierarchy.generateFrameworkHierarchy(id);
					OK(parent);
				} else if(StringUtils.equalsIgnoreCase(FrameworkHierarchyOperations.getFrameworkHierarchy.name(), methodName)){
					String frameworkId = (String) request.get("identifier");
					Map<String,Object> frameworkData = fwHierarchy.getFrameworkHierarchy(frameworkId);
					OK("framework", frameworkData, sender());
				} else {
					TelemetryManager.log("Unsupported operation: " + methodName);
					throw new ClientException(LearningErrorCodes.ERR_INVALID_OPERATION.name(),
							"Unsupported operation: " + methodName);
				}
			}
		} catch (Exception e) {
			ERROR(e.getCause(), parent);
		}
		
	}
}
