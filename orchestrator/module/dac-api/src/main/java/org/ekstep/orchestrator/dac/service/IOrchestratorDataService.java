package org.ekstep.orchestrator.dac.service;

import java.util.List;

import org.ekstep.orchestrator.dac.model.OrchestratorScript;

public interface IOrchestratorDataService {

	OrchestratorScript getScript(String name);

	String createScript(OrchestratorScript script);

	String createCommand(OrchestratorScript command);

	void updateScript(OrchestratorScript script);

	List<OrchestratorScript> getAllScripts();

	List<OrchestratorScript> getAllCommands();
	
	List<OrchestratorScript> getScriptsByRequestPath(String url, String type);

	boolean doConnectionEstablish();
}
