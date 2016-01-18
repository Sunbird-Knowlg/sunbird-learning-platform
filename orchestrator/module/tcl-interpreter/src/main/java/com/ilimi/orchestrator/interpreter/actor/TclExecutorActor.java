package com.ilimi.orchestrator.interpreter.actor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams;
import com.ilimi.common.dto.ResponseParams.StatusType;
import com.ilimi.common.exception.MiddlewareException;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.orchestrator.dac.model.OrchestratorScript;
import com.ilimi.orchestrator.dac.model.ScriptParams;
import com.ilimi.orchestrator.dac.model.ScriptTypes;
import com.ilimi.orchestrator.interpreter.ICommand;
import com.ilimi.orchestrator.interpreter.OrchestratorRequest;
import com.ilimi.orchestrator.interpreter.command.AkkaCommand;
import com.ilimi.orchestrator.interpreter.exception.ExecutionErrorCodes;

import akka.actor.UntypedActor;
import tcl.lang.Command;
import tcl.lang.Interp;
import tcl.lang.TCL;
import tcl.lang.TclException;
import tcl.lang.TclObject;
import tcl.pkg.java.ReflectObject;

public class TclExecutorActor extends UntypedActor {

	private Interp interpreter;

	public TclExecutorActor(List<OrchestratorScript> commands) {
	    init(commands);
	}
	
	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof OrchestratorRequest) {
			Response response = null;
			try {
				OrchestratorRequest request = (OrchestratorRequest) message;
				if (StringUtils.equalsIgnoreCase(OrchestratorRequest.ACTION_TYPES.INIT.name(), request.getAction())) {
					init(request.getScripts());
					response = OK();
				} else if (StringUtils.equalsIgnoreCase(OrchestratorRequest.ACTION_TYPES.EXECUTE.name(),
						request.getAction())) {
					Object result = execute(request.getScript(), request.getParams());
					response = OK("result", result);
				} else {
					response = ERROR(ExecutionErrorCodes.ERR_INVALID_REQUEST.name(), "Invalid Request",
							ResponseCode.CLIENT_ERROR);
				}
			} catch (MiddlewareException me) {
				response = ERROR(me.getErrCode(), me.getMessage(), me.getResponseCode());
			} catch (Exception e) {
				response = ERROR(ExecutionErrorCodes.ERR_SYSTEM_ERROR.name(), e.getMessage(),
						ResponseCode.SERVER_ERROR);
			}
			getSender().tell(response, getSelf());
		} else {
			unhandled(message);
		}
	}

	@SuppressWarnings("rawtypes")
	private void init(List<OrchestratorScript> scripts) {
		interpreter = new Interp();
		System.out.println("loading commands");
		if (null != scripts && !scripts.isEmpty()) {
		    System.out.println("loading commands: " + scripts.size());
			for (OrchestratorScript script : scripts) {
				if (StringUtils.equalsIgnoreCase(ScriptTypes.COMMAND.name(), script.getType())) {
					if (StringUtils.isNotBlank(script.getCmdClass())) {
						try {
							Class cls = Class.forName(script.getCmdClass());
							Object obj = cls.newInstance();
							if (!(obj instanceof ICommand))
								throw new MiddlewareException(ExecutionErrorCodes.ERR_INIT_INVALID_COMMAND.name(),
										"Custom commands must implement ICommand");
							if (!(obj instanceof Command))
								throw new MiddlewareException(ExecutionErrorCodes.ERR_INIT_INVALID_COMMAND.name(),
										"Custom commands must implement tcl.lang.Command");
							Command cmd = (Command) obj;
							interpreter.createCommand(script.getName(), cmd);
						} catch (MiddlewareException e) {
							throw e;
						} catch (Exception e) {
							throw new MiddlewareException(ExecutionErrorCodes.ERR_INIT_INVALID_COMMAND.name(),
									e.getMessage(), e);
						}
					} else {
						interpreter.createCommand(script.getName(), new AkkaCommand(script));
					}
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private Object execute(OrchestratorScript script, List<Object> params) {
		try {
			if (null != params && !params.isEmpty()) {
				Map<Integer, ScriptParams> paramMap = new HashMap<Integer, ScriptParams>();
				if (null != script.getParameters() && !script.getParameters().isEmpty()) {
					for (ScriptParams param : script.getParameters()) {
						paramMap.put(param.getIndex(), param);
					}
				}
				for (int i = 1; i <= params.size(); i++) {
					ScriptParams param = paramMap.get(i-1);
					if (null != param) {
						try {
							if (StringUtils.isNotBlank(param.getDatatype())) {
								Class cls = Class.forName(param.getDatatype());
								TclObject obj = ReflectObject.newInstance(interpreter, cls, params.get(i - 1));
								interpreter.setVar(param.getName(), obj, TCL.NAMESPACE_ONLY);
							} else {
								interpreter.setVar(param.getName(), params.get(i - 1).toString(), TCL.NAMESPACE_ONLY);
							}
						} catch (Exception e) {
							interpreter.setVar(param.getName(), params.get(i - 1).toString(), TCL.NAMESPACE_ONLY);
						}
					}
				}
			}
			interpreter.eval(script.getBody());
			TclObject tclObject = interpreter.getResult();
			if (null != tclObject.getInternalRep() && tclObject.getInternalRep() instanceof ReflectObject)
			    return ReflectObject.get(interpreter, tclObject);
			else
			    return tclObject.toString();
		} catch (TclException ex) {
			int code = ex.getCompletionCode();
			String msg = "";
			switch (code) {
			case TCL.ERROR:
				msg = interpreter.getResult().toString();
				break;
			case TCL.BREAK:
				msg = "invoked \"break\" outside of a loop";
				break;
			case TCL.CONTINUE:
				msg = "invoked \"continue\" outside of a loop";
				break;
			default:
				msg = "command returned bad error code: " + code;
				break;
			}
			throw new MiddlewareException(ExecutionErrorCodes.ERR_SCRIPT_ERROR.name(), msg, ex);
		} catch (Exception e) {
			throw new MiddlewareException(ExecutionErrorCodes.ERR_SYSTEM_ERROR.name(), e.getMessage(), e);
		}
	}

	private Response ERROR(String errorCode, String errorMessage, ResponseCode responseCode) {
		Response response = new Response();
		response.setParams(getErrorStatus(errorCode, errorMessage));
		response.setResponseCode(responseCode);
		return response;
	}

	protected Response OK() {
		Response response = new Response();
		response.setParams(getSucessStatus());
		return response;
	}

	protected Response OK(String responseIdentifier, Object vo) {
		Response response = new Response();
		response.put(responseIdentifier, vo);
		response.setParams(getSucessStatus());
		return response;
	}

	private ResponseParams getErrorStatus(String errorCode, String errorMessage) {
		ResponseParams params = new ResponseParams();
		params.setErr(errorCode);
		params.setStatus(StatusType.failed.name());
		params.setErrmsg(errorMessage);
		return params;
	}

	private ResponseParams getSucessStatus() {
		ResponseParams params = new ResponseParams();
		params.setErr("0");
		params.setStatus(StatusType.successful.name());
		params.setErrmsg("Operation successful");
		return params;
	}

}
