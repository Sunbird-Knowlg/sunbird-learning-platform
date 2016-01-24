package com.ilimi.orchestrator.interpreter.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.MiddlewareException;
import com.ilimi.orchestrator.dac.model.OrchestratorScript;
import com.ilimi.orchestrator.dac.model.ScriptParams;
import com.ilimi.orchestrator.dac.model.ScriptTypes;
import com.ilimi.orchestrator.interpreter.Executor;
import com.ilimi.orchestrator.interpreter.OrchestratorRequest;
import com.ilimi.orchestrator.interpreter.actor.TclExecutorActorRef;
import com.ilimi.orchestrator.interpreter.exception.ExecutionErrorCodes;
import com.ilimi.orchestrator.mgr.service.IOrchestratorManager;
import com.ilimi.orchestrator.mgr.service.OrchestratorScriptMap;
import com.ilimi.orchestrator.router.AkkaRequestRouter;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.routing.Broadcast;
import scala.concurrent.Await;
import scala.concurrent.Future;

@Component
public class TclExecutor implements Executor {

    @Autowired
    private IOrchestratorManager manager;

    @Override
    public Response initCommands() {
        ActorRef actorRef = TclExecutorActorRef.getRef();
        if (null != actorRef) {
            List<OrchestratorScript> scripts = manager.getAllScripts();
            List<OrchestratorScript> commands = manager.getAllCommands();
            OrchestratorScriptMap.loadScripts(scripts, commands);
            OrchestratorRequest request = new OrchestratorRequest();
            request.setAction(OrchestratorRequest.ACTION_TYPES.INIT.name());
            request.setScripts(commands);
            if (null != scripts && !scripts.isEmpty()) {
                if (null == request.getScripts())
                    request.setScripts(new ArrayList<OrchestratorScript>());
                request.getScripts().addAll(scripts);
            }
            actorRef.tell(new Broadcast(request), actorRef);
            return null;
        } else {
            throw new MiddlewareException(ExecutionErrorCodes.ERR_INIT_ERROR.name(), "Executor actor not initialized");
        }
    }

    @Override
    public Response execute(OrchestratorScript script, Map<String, Object> params) {
        if (null == script || StringUtils.isBlank(script.getName()) || StringUtils.isBlank(script.getType())) {
            throw new MiddlewareException(ExecutionErrorCodes.ERR_INVALID_REQUEST.name(),
                    "Executor actor not initialized");
        }
        ActorRef actorRef = TclExecutorActorRef.getRef();
        if (null != actorRef) {
            if (StringUtils.equalsIgnoreCase(ScriptTypes.COMMAND.name(), script.getType())) {
                String body = script.getName();
                if (null != script.getParameters() && !script.getParameters().isEmpty()) {
                    for (ScriptParams param : script.getParameters()) {
                        body += " $" + param.getName();
                    }
                }
                script.setBody(body);
            }
            OrchestratorRequest request = new OrchestratorRequest();
            request.setAction(OrchestratorRequest.ACTION_TYPES.EXECUTE.name());
            request.setScript(script);
            request.setParams(params);
            Future<Object> future = Patterns.ask(actorRef, request, AkkaRequestRouter.timeout);
            try {
                Object result = Await.result(future, AkkaRequestRouter.WAIT_TIMEOUT.duration());
                if (result instanceof Response) {
                    return (Response) result;
                } else {
                    throw new MiddlewareException(ExecutionErrorCodes.ERR_EXEC_ERROR.name(),
                            "Execute returned an invalid response");
                }
            } catch (MiddlewareException e) {
                throw e;
            } catch (Exception e) {
                throw new MiddlewareException(ExecutionErrorCodes.ERR_SYSTEM_ERROR.name(), e.getMessage(), e);
            }
        } else {
            throw new MiddlewareException(ExecutionErrorCodes.ERR_EXEC_ERROR.name(), "Executor actor not initialized");
        }
    }

    @PostConstruct
    public void initExecutor() {
        List<OrchestratorScript> commands = manager.getAllCommands();
        List<OrchestratorScript> scripts = manager.getAllScripts();
        if (null != scripts && !scripts.isEmpty()) {
            if (null == commands)
                commands = new ArrayList<OrchestratorScript>();
            commands.addAll(scripts);
        }
        TclExecutorActorRef.initExecutorActor(commands);
    }

}
