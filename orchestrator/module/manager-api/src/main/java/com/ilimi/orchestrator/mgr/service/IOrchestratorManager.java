package com.ilimi.orchestrator.mgr.service;

import java.util.List;

import com.ilimi.orchestrator.dac.model.OrchestratorScript;

public interface IOrchestratorManager {

	void registerScript(OrchestratorScript script);
	
	void registerCommand(OrchestratorScript script);
	
	void updateScript(String name, OrchestratorScript script);
	
	void updateCommand(String name, OrchestratorScript command);
	
	OrchestratorScript getScript(String name);

	List<OrchestratorScript> getAllScripts();

	List<OrchestratorScript> getAllCommands();

}
