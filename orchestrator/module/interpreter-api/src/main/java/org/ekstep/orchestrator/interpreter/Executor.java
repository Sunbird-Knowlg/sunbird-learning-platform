package org.ekstep.orchestrator.interpreter;

import java.util.Map;

import org.ekstep.common.dto.Response;
import org.ekstep.orchestrator.dac.model.OrchestratorScript;

public interface Executor {

	Response initCommands();

	Response execute(OrchestratorScript script, Map<String, Object> params);
}
