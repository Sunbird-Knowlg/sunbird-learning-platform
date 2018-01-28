package org.ekstep.orchestrator.mgr.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.orchestrator.dac.model.OrchestratorScript;

public class OrchestratorScriptMap {

    public static Map<String, Map<String, OrchestratorScript>> scriptMap = new HashMap<String, Map<String, OrchestratorScript>>();
    public static Map<String, OrchestratorScript> scriptNameMap = new HashMap<String, OrchestratorScript>();

    public static void loadScripts(List<OrchestratorScript> scripts, List<OrchestratorScript> commands) {
        scriptMap = new HashMap<String, Map<String, OrchestratorScript>>();
        scriptNameMap = new HashMap<String, OrchestratorScript>();
        loadScriptMap(scripts);
        loadScriptMap(commands);
    }

    private static void loadScriptMap(List<OrchestratorScript> scripts) {
        if (null != scripts && !scripts.isEmpty()) {
            for (OrchestratorScript script : scripts) {
                if (null != script.getRequestPath() && StringUtils.isNotBlank(script.getRequestPath().getUrl())) {
                    Map<String, OrchestratorScript> map = scriptMap.get(script.getRequestPath().getType());
                    if (null == map) {
                        map = new HashMap<String, OrchestratorScript>();
                        scriptMap.put(script.getRequestPath().getType(), map);
                    }
                    map.put(script.getRequestPath().getUrl(), script);
                }
                scriptNameMap.put(script.getName(), script);
            }
        }
    }
    
    public static OrchestratorScript getScriptByName(String scriptName){
    	return scriptNameMap.get(scriptName);
    }
}
