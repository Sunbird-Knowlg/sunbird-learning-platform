package com.ilimi.orchestrator.mgr.service.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ilimi.common.exception.ResourceNotFoundException;
import com.ilimi.orchestrator.dac.model.OrchestratorScript;
import com.ilimi.orchestrator.dac.model.RequestPath;
import com.ilimi.orchestrator.dac.model.RequestRouters;
import com.ilimi.orchestrator.dac.model.RequestTypes;
import com.ilimi.orchestrator.dac.model.ScriptTypes;
import com.ilimi.orchestrator.dac.service.IOrchestratorDataService;
import com.ilimi.orchestrator.mgr.exception.OrchestratorErrorCodes;
import com.ilimi.orchestrator.mgr.exception.OrchestratorException;
import com.ilimi.orchestrator.mgr.service.IOrchestratorManager;

@Component
public class OrchestratorManagerImpl implements IOrchestratorManager {

    @Autowired
    private IOrchestratorDataService daoService;

    @Override
    public void registerScript(OrchestratorScript script) {
        validateScript(script);
        try {
            List<OrchestratorScript> scripts = daoService.getScriptsByRequestPath(script.getRequestPath().getUrl(),
                    script.getRequestPath().getType());
            if (null == scripts || scripts.isEmpty())
                daoService.createScript(script);
            else {
                throw new OrchestratorException(OrchestratorErrorCodes.ERR_CREATE_SCRIPT.name(),
                        "A script already exists for the request path");
            }
        } catch (Exception e) {
            throw new OrchestratorException(OrchestratorErrorCodes.ERR_CREATE_SCRIPT.name(), e.getMessage(), e);
        }
    }

    @Override
    public void registerCommand(OrchestratorScript command) {
        validateCommand(command);
        try {
            List<OrchestratorScript> scripts = null;
            if (null != command.getRequestPath() && StringUtils.isNotBlank(command.getRequestPath().getUrl())) {
                scripts = daoService.getScriptsByRequestPath(command.getRequestPath().getUrl(),
                        command.getRequestPath().getType());
            }
            if (null == scripts || scripts.isEmpty())
                daoService.createCommand(command);
            else {
                throw new OrchestratorException(OrchestratorErrorCodes.ERR_CREATE_COMMAND.name(),
                        "A command already exists for the request path");
            }
        } catch (Exception e) {
            throw new OrchestratorException(OrchestratorErrorCodes.ERR_CREATE_COMMAND.name(), e.getMessage(), e);
        }
    }

    @Override
    public List<OrchestratorScript> getAllScripts() {
        try {
            return daoService.getAllScripts();
        } catch (Exception e) {
            throw new OrchestratorException(OrchestratorErrorCodes.ERR_LIST_SCRIPTS.name(), e.getMessage(), e);
        }
    }

    @Override
    public List<OrchestratorScript> getAllCommands() {
        try {
            return daoService.getAllCommands();
        } catch (Exception e) {
            throw new OrchestratorException(OrchestratorErrorCodes.ERR_LIST_COMMANDS.name(), e.getMessage(), e);
        }
    }

    @Override
    public void updateScript(String name, OrchestratorScript script) {
        OrchestratorScript dbScript = daoService.getScript(name);
        if (null == dbScript)
            throw new ResourceNotFoundException(OrchestratorErrorCodes.ERR_SCRIPT_NOT_FOUND.name(),
                    "Script not found for name: " + name);
        if (StringUtils.equalsIgnoreCase(ScriptTypes.SCRIPT.name(), dbScript.getType())) {
            validateScript(script);
            List<OrchestratorScript> scripts = daoService.getScriptsByRequestPath(script.getRequestPath().getUrl(),
                    script.getRequestPath().getType());
            boolean valid = true;
            if (null != scripts && !scripts.isEmpty()) {
                for (OrchestratorScript s : scripts) {
                    if (!StringUtils.equals(s.getName(), dbScript.getName())) {
                        valid = false;
                        break;
                    }
                }
            }
            if (valid) {
                dbScript.setName(script.getName());
                dbScript.setBody(script.getBody());
                dbScript.setDescription(script.getDescription());
                dbScript.setParameters(script.getParameters());
                dbScript.setRequestPath(script.getRequestPath());
                daoService.updateScript(dbScript);
            } else {
                throw new OrchestratorException(OrchestratorErrorCodes.ERR_UPDATE_SCRIPT.name(),
                        "A script already exists for the given request path");
            }
        } else {
            throw new OrchestratorException(OrchestratorErrorCodes.ERR_UPDATE_SCRIPT.name(), name + " is not a script");
        }
    }

    @Override
    public void updateCommand(String name, OrchestratorScript command) {
        OrchestratorScript dbCommand = daoService.getScript(name);
        if (null == dbCommand)
            throw new ResourceNotFoundException(OrchestratorErrorCodes.ERR_COMMAND_NOT_FOUND.name(),
                    "Command not found for name: " + name);
        if (StringUtils.equalsIgnoreCase(ScriptTypes.COMMAND.name(), dbCommand.getType())) {
            validateCommand(command);
            boolean valid = true;
            List<OrchestratorScript> scripts = null;
            if (null != command.getRequestPath() && StringUtils.isNotBlank(command.getRequestPath().getUrl())) {
                scripts = daoService.getScriptsByRequestPath(command.getRequestPath().getUrl(),
                        command.getRequestPath().getType());
                if (null != scripts && !scripts.isEmpty()) {
                    for (OrchestratorScript s : scripts) {
                        if (!StringUtils.equals(s.getName(), dbCommand.getName())) {
                            valid = false;
                            break;
                        }
                    }
                }
            }
            if (valid) {
                dbCommand.setName(command.getName());
                dbCommand.setDescription(command.getDescription());
                dbCommand.setActorPath(command.getActorPath());
                dbCommand.setParameters(command.getParameters());
                dbCommand.setRequestPath(command.getRequestPath());
                dbCommand.setCmdClass(command.getCmdClass());
                daoService.updateScript(dbCommand);
            } else {
                throw new OrchestratorException(OrchestratorErrorCodes.ERR_UPDATE_COMMAND.name(),
                        "A command already exists for the request path");
            }
        } else {
            throw new OrchestratorException(OrchestratorErrorCodes.ERR_UPDATE_COMMAND.name(),
                    name + " is not a command");
        }
    }

    @Override
    public OrchestratorScript getScript(String name) {
        OrchestratorScript script = daoService.getScript(name);
        if (null == script)
            throw new ResourceNotFoundException(OrchestratorErrorCodes.ERR_SCRIPT_NOT_FOUND.name(),
                    "Script not found: " + name);
        return daoService.getScript(name);
    }

    private void validateScript(OrchestratorScript script) {
        if (null == script)
            throw new OrchestratorException(OrchestratorErrorCodes.ERR_BLANK_SCRIPT.name(),
                    "Blank script cannot be registered");
        if (StringUtils.isBlank(script.getName()) || script.getName().indexOf(' ') >= 0)
            throw new OrchestratorException(OrchestratorErrorCodes.ERR_INVALID_SCRIPT.name(),
                    "Script name cannot be blank");
        if (StringUtils.isBlank(script.getBody()))
            throw new OrchestratorException(OrchestratorErrorCodes.ERR_INVALID_SCRIPT.name(),
                    "Script body cannot be blank");
        script.setName(script.getName().trim());
        if (null == script.getRequestPath()) {
            RequestPath path = new RequestPath();
            path.setType(RequestTypes.POST.name());
            path.setUrl("/v1/exec/" + script.getName());
            script.setRequestPath(path);
        }
        if (null == script.getRequestPath())
            throw new OrchestratorException(OrchestratorErrorCodes.ERR_INVALID_SCRIPT.name(),
                    "Script should be registered with a valid request path");
        validateRequestPath(script.getRequestPath(), OrchestratorErrorCodes.ERR_INVALID_SCRIPT.name());
    }

    private void validateRequestPath(RequestPath path, String errorCode) {
        if (!RequestTypes.isValidRequestType(path.getType()))
            throw new OrchestratorException(errorCode,
                    "Invalid request type. Request type should be one of GET, POST, PATCH, PUT or DELETE");
        if (StringUtils.isBlank(path.getUrl()))
            throw new OrchestratorException(errorCode, "Request URL cannot be blank");
        if (!path.getUrl().startsWith("/"))
            path.setUrl("/" + path.getUrl().trim());
        String url = path.getUrl();
        String[] tokens = url.split("/");
        int count = 0;
        for (String token : tokens) {
            if (token.indexOf(' ') >= 0)
                throw new OrchestratorException(errorCode, "Request URL cannot contain spaces");
            if (token.equals("*"))
                count += 1;
        }
        if (count > 0) {
            if (null == path.getPathParams() || path.getPathParams().size() != count)
                throw new OrchestratorException(errorCode, "Request URL cannot contain spaces");
        }
        if (StringUtils.equals(RequestTypes.GET.name(), path.getType()))
            if (null != path.getBodyParams() && !path.getBodyParams().isEmpty())
                throw new OrchestratorException(errorCode, "GET request cannot have body parameters");
    }

    private void validateCommand(OrchestratorScript command) {
        if (null == command)
            throw new OrchestratorException(OrchestratorErrorCodes.ERR_BLANK_COMMAND.name(),
                    "Blank command cannot be registered");
        if (StringUtils.isBlank(command.getName()))
            throw new OrchestratorException(OrchestratorErrorCodes.ERR_INVALID_COMMAND.name(),
                    "Command name cannot be blank");
        if (null != command.getRequestPath() && StringUtils.isNotBlank(command.getRequestPath().getUrl()))
            validateRequestPath(command.getRequestPath(), OrchestratorErrorCodes.ERR_INVALID_COMMAND.name());
        if (StringUtils.isBlank(command.getCmdClass())
                && (null == command.getActorPath() || StringUtils.isBlank(command.getActorPath().getManager())
                        || StringUtils.isBlank(command.getActorPath().getOperation())))
            throw new OrchestratorException(OrchestratorErrorCodes.ERR_INVALID_COMMAND.name(),
                    "Command should either have command class information or actor path details");
        if (StringUtils.isBlank(command.getCmdClass())) {
            if (StringUtils.isBlank(command.getActorPath().getRouter()))
                command.getActorPath().setRouter(RequestRouters.GRAPH_REQUEST_ROUTER.name());
            if (!RequestRouters.isValidRequestRouter(command.getActorPath().getRouter()))
                throw new OrchestratorException(OrchestratorErrorCodes.ERR_INVALID_COMMAND.name(),
                        "UnSupported Request Router for the command");
        }
        command.setName(command.getName().trim());
    }

}
