package com.ilimi.orchestrator.interpreter;

import java.util.List;

import com.ilimi.common.dto.Response;
import com.ilimi.orchestrator.dac.model.OrchestratorScript;

public interface Executor {

	Response initCommands();

	Response execute(OrchestratorScript script, List<Object> params);
}
