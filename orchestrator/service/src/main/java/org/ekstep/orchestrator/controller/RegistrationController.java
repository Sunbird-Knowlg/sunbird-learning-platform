package org.ekstep.orchestrator.controller;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import org.ekstep.orchestrator.dac.model.OrchestratorScript;
import org.ekstep.orchestrator.dac.model.ScriptTypes;
import org.ekstep.orchestrator.interpreter.Executor;
import org.ekstep.orchestrator.mgr.exception.OrchestratorErrorCodes;
import org.ekstep.orchestrator.mgr.exception.OrchestratorException;
import org.ekstep.orchestrator.mgr.service.IOrchestratorManager;

@Controller
@RequestMapping("/v1/orchestrator")
public class RegistrationController extends BaseOrchestratorController {

    @Autowired
    private IOrchestratorManager manager;
    
    @Autowired
    private Executor executor;

    private ObjectMapper mapper = new ObjectMapper();

    @RequestMapping(value = "/register/{type:.+}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> registerScript(@PathVariable(value = "type") String type,
            @RequestBody Map<String, Object> map) {
        String apiId = "register.script";
        try {
            if (StringUtils.equalsIgnoreCase(ScriptTypes.COMMAND.name(), type)) {
                manager.registerCommand(getScript(map));
            } else if (StringUtils.equalsIgnoreCase(ScriptTypes.SCRIPT.name(), type)) {
                manager.registerScript(getScript(map));
            } else {
                throw new OrchestratorException(OrchestratorErrorCodes.ERR_INVALID_REGISTER_REQUEST.name(),
                        "Invalid type");
            }
            return getResponseEntity(getSuccessResponse(), apiId);
        } catch (Exception e) {
            return getExceptionResponseEntity(e, apiId);
        }
    }

    @RequestMapping(value = "/update/{type:.+}/{id:.+}", method = RequestMethod.PATCH)
    @ResponseBody
    public ResponseEntity<Response> update(@PathVariable(value = "type") String type,
            @PathVariable(value = "id") String id, @RequestBody Map<String, Object> map) {
        String apiId = "update.script";
        try {
            if (StringUtils.equalsIgnoreCase(ScriptTypes.COMMAND.name(), type)) {
                manager.updateCommand(id, getScript(map));
            } else if (StringUtils.equalsIgnoreCase(ScriptTypes.SCRIPT.name(), type)) {
                manager.updateScript(id, getScript(map));
            } else {
                throw new OrchestratorException(OrchestratorErrorCodes.ERR_INVALID_REGISTER_REQUEST.name(),
                        "Invalid type");
            }
            return getResponseEntity(getSuccessResponse(), apiId);
        } catch (Exception e) {
            return getExceptionResponseEntity(e, apiId);
        }
    }

    @RequestMapping(value = "/{id:.+}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> getScript(@PathVariable(value = "id") String name) {
        String apiId = "script.find";
        try {
            OrchestratorScript script = manager.getScript(name);
            Response response = getSuccessResponse();
            response.put("script", script);
            return getResponseEntity(response, apiId);
        } catch (Exception e) {
            return getExceptionResponseEntity(e, apiId);
        }
    }
    
    @RequestMapping(value = "/load/commands", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> load() {
        String apiId = "commands.load";
        try {
            executor.initCommands();
            Response response = getSuccessResponse();
            return getResponseEntity(response, apiId);
        } catch (Exception e) {
            return getExceptionResponseEntity(e, apiId);
        }
    }

    private OrchestratorScript getScript(Map<String, Object> requestMap) {
        if (null != requestMap && !requestMap.isEmpty()) {
            try {
                String strRequest = mapper.writeValueAsString(requestMap);
                OrchestratorScript script = mapper.readValue(strRequest, OrchestratorScript.class);
                return script;
            } catch (Exception e) {
                throw new ClientException(OrchestratorErrorCodes.ERR_INVALID_REGISTER_REQUEST.name(),
                        "Error! Invalid Request format", e);
            }
        }
        return null;
    }
}
