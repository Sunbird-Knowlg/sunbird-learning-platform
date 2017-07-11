package com.ilimi.orchestrator.interpreter.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.dto.ResponseParams.StatusType;
import com.ilimi.common.exception.MiddlewareException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.orchestrator.dac.model.OrchestratorScript;
import com.ilimi.orchestrator.dac.model.ScriptParams;
import com.ilimi.orchestrator.interpreter.exception.ExecutionErrorCodes;
import com.ilimi.orchestrator.router.AkkaRequestRouter;

import tcl.lang.Command;
import tcl.lang.Interp;
import tcl.lang.TclException;
import tcl.lang.TclObject;
import tcl.pkg.java.ReflectObject;

public class AkkaCommand implements Command {

	private List<ScriptParams> routingParams = new ArrayList<ScriptParams>();
	private Map<Integer, String> paramNames = new HashMap<Integer, String>();
	private OrchestratorScript info;

	public AkkaCommand(OrchestratorScript command) {
		if (null == command || null == command.getActorPath())
			throw new MiddlewareException(ExecutionErrorCodes.ERR_INIT_BLANK_COMMAND.name(),
					"Empty command cannot be instantiated");
		if (StringUtils.isBlank(command.getName()))
			throw new MiddlewareException(ExecutionErrorCodes.ERR_INIT_INVALID_COMMAND.name(),
					"Command name cannot be blank");
		this.info = command;
		if (null != command.getParameters() && !command.getParameters().isEmpty()) {
			for (ScriptParams param : command.getParameters()) {
				if (param.isRoutingParam()) {
					routingParams.add(param);
				}
				paramNames.put(param.getIndex(), param.getName());
			}
		}
	}

	@Override
	public void cmdProc(Interp interp, TclObject[] argv) throws TclException {
		if (null != argv && argv.length == (paramNames.size() + 1)) {
			Request req = new Request();
			req.setManagerName(info.getActorPath().getManager());
			req.setOperation(info.getActorPath().getOperation());
			for (int i = 1; i < argv.length; i++) {
				TclObject tclObject = argv[i];
				Object obj = null;
				try {
					obj = ReflectObject.get(interp, tclObject);
				} catch (Exception e) {
					obj = tclObject.toString();
				}
				String name = paramNames.get(i-1);
				if (null != obj) {
				    req.put(name, obj);
				}
			}
			for (ScriptParams param : routingParams) {
                String key = param.getName();
                if (StringUtils.isNotBlank(param.getRoutingId()))
                    key = param.getRoutingId();
                req.getContext().put(key, req.get(key));
            }
			try {
				Response response = null;
				if (null != info.getAsync() && info.getAsync().booleanValue())
				    response = AkkaRequestRouter.sendRequestAsync(req, info.getActorPath());
				else
				    response = AkkaRequestRouter.sendRequest(req, info.getActorPath());
				interp.setResult(ReflectObject.newInstance(interp, Response.class, response));
			} catch (MiddlewareException e) {
				Response response = ERROR(e.getErrCode(), e.getMessage(), e.getResponseCode());
				interp.setResult(ReflectObject.newInstance(interp, Response.class, response));
			}
		} else {
			Response response = ERROR(ExecutionErrorCodes.ERR_EXEC_INVALID_REQUEST.name(),
					"Request to the command is invalid", ResponseCode.CLIENT_ERROR);
			interp.setResult(ReflectObject.newInstance(interp, Response.class, response));
		}
	}

	private Response ERROR(String errorCode, String errorMessage, ResponseCode responseCode) {
		Response response = new Response();
		response.setParams(getErrorStatus(errorCode, errorMessage));
		response.setResponseCode(responseCode);
		return response;
	}

	private ResponseParams getErrorStatus(String errorCode, String errorMessage) {
		ResponseParams params = new ResponseParams();
		params.setErr(errorCode);
		params.setStatus(StatusType.failed.name());
		params.setErrmsg(errorMessage);
		return params;
	}

}
