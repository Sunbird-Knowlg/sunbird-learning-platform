package com.ilimi.orchestrator.interpreter.command;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.MiddlewareException;
import com.ilimi.orchestrator.dac.model.OrchestratorScript;
import com.ilimi.orchestrator.dac.model.ScriptTypes;
import com.ilimi.orchestrator.interpreter.OrchestratorRequest;
import com.ilimi.orchestrator.interpreter.actor.TclExecutorActorRef;
import com.ilimi.orchestrator.interpreter.exception.ExecutionErrorCodes;
import com.ilimi.orchestrator.router.AkkaRequestRouter;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import scala.concurrent.Await;
import scala.concurrent.Future;
import tcl.lang.Command;
import tcl.lang.Interp;
import tcl.lang.TclException;
import tcl.lang.TclObject;
import tcl.pkg.java.ReflectObject;

public class ScriptCommand extends BaseSystemCommand implements Command {

    private OrchestratorScript self;

    public ScriptCommand(OrchestratorScript script) {
        if (null == script || !StringUtils.equalsIgnoreCase(ScriptTypes.SCRIPT.name(), script.getType())
                || StringUtils.isBlank(script.getName()))
            throw new MiddlewareException(ExecutionErrorCodes.ERR_INIT_INVALID_SCRIPT.name(),
                    "Cannot register an invalid script as command");
        this.self = script;
    }

    @Override
    public void cmdProc(Interp interp, TclObject[] argv) throws TclException {
        ActorRef actorRef = TclExecutorActorRef.getRef();
        try {
            List<Object> params = new ArrayList<Object>();
            if (null != argv && argv.length > 0) {
                for (int i = 1; i < argv.length; i++) {
                    TclObject param = argv[i];
                    Object obj = null;
                    if (null != param.getInternalRep() && param.getInternalRep() instanceof ReflectObject)
                        obj = ReflectObject.get(interp, param);
                    else
                        obj = param.toString();
                    params.add(obj);
                }
            }
            OrchestratorRequest request = new OrchestratorRequest();
            request.setAction(OrchestratorRequest.ACTION_TYPES.EXECUTE.name());
            request.setScript(this.self);
            request.setParams(params);
            Future<Object> future = Patterns.ask(actorRef, request, AkkaRequestRouter.timeout);
            Object result = Await.result(future, AkkaRequestRouter.WAIT_TIMEOUT.duration());
            Response res = (Response) result;
            Response response = new Response();
            response.setParams(res.getParams());
            response.setResponseCode(res.getResponseCode());
            response.setId(res.getId());
            response.getResult().putAll(res.getResult());
            TclObject tclResp = ReflectObject.newInstance(interp, response.getClass(), response);
            interp.setResult(tclResp);
        } catch (Exception e) {
            throw new MiddlewareException(ExecutionErrorCodes.ERR_SYSTEM_ERROR.name(), e.getMessage(), e);
        }
    }
}
