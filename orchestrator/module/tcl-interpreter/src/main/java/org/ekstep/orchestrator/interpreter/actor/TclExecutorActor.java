package org.ekstep.orchestrator.interpreter.actor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.common.dto.ExecutionContext;
import org.ekstep.common.dto.HeaderParam;
import org.ekstep.common.dto.Response;
import org.ekstep.common.dto.ResponseParams;
import org.ekstep.common.dto.ResponseParams.StatusType;
import org.ekstep.common.exception.MiddlewareException;
import org.ekstep.common.exception.ResponseCode;
import org.ekstep.graph.common.enums.GraphHeaderParams;
import org.ekstep.orchestrator.dac.model.OrchestratorScript;
import org.ekstep.orchestrator.dac.model.ScriptParams;
import org.ekstep.orchestrator.dac.model.ScriptTypes;
import org.ekstep.orchestrator.interpreter.ICommand;
import org.ekstep.orchestrator.interpreter.OrchestratorRequest;
import org.ekstep.orchestrator.interpreter.command.AkkaCommand;
import org.ekstep.orchestrator.interpreter.command.ScriptCommand;
import org.ekstep.orchestrator.interpreter.exception.ExecutionErrorCodes;
import org.ekstep.telemetry.logger.TelemetryManager;
import org.springframework.web.multipart.MultipartFile;

import akka.actor.UntypedActor;
import tcl.lang.Command;
import tcl.lang.Interp;
import tcl.lang.TCL;
import tcl.lang.TclException;
import tcl.lang.TclObject;
import tcl.pkg.java.ReflectObject;

public class TclExecutorActor extends UntypedActor {

	private Interp interpreter;
	private ObjectMapper mapper = new ObjectMapper();

	private static final Logger perfLogger = LogManager.getLogger("PerformanceTestLogger");

	public TclExecutorActor(List<OrchestratorScript> commands) {
		init(commands);
	}

	public TclExecutorActor() {
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof OrchestratorRequest) {
			Response response = null;
			try {
				OrchestratorRequest request = (OrchestratorRequest) message;
				if (null != request && StringUtils.isNotBlank(request.getRequestId()))
					ExecutionContext.getCurrent().getGlobalContext().put(HeaderParam.REQUEST_ID.getParamName(),
							request.getRequestId());
				if (null != request && StringUtils.isNotBlank(request.getConsumerId()))
					ExecutionContext.getCurrent().getGlobalContext().put(HeaderParam.CONSUMER_ID.getParamName(),
							request.getConsumerId());
				if (null != request && StringUtils.isNotBlank(request.getChannelId()))
					ExecutionContext.getCurrent().getGlobalContext().put(HeaderParam.CHANNEL_ID.getParamName(),
							request.getChannelId());
				if (null != request && StringUtils.isNotBlank(request.getAppId()))
					ExecutionContext.getCurrent().getGlobalContext().put(HeaderParam.APP_ID.getParamName(),
							request.getAppId());
				if (StringUtils.equalsIgnoreCase(OrchestratorRequest.ACTION_TYPES.INIT.name(), request.getAction())) {
					init(request.getScripts());
					response = OK();
				} else if (StringUtils.equalsIgnoreCase(OrchestratorRequest.ACTION_TYPES.EXECUTE.name(),
						request.getAction())) {
					long startTime = System.currentTimeMillis();
					perfLogger.info(request.getContext().get(GraphHeaderParams.scenario_name.name()) + ","
							+ request.getRequestId() + ",TclExecutorActor," + request.getScript().getName()
							+ ",STARTTIME," + startTime);
					Object result = execute(request.getScript(), request.getParams());
					long endTime = System.currentTimeMillis();
					long exeTime = endTime - startTime;
					perfLogger.info(request.getContext().get(GraphHeaderParams.scenario_name.name()) + ","
							+ request.getRequestId() + ",TclExecutorActor," + request.getScript().getName()
							+ ",ENDTIME," + endTime);
					perfLogger.info(request.getContext().get(GraphHeaderParams.scenario_name.name()) + ","
							+ request.getRequestId() + ",TclExecutorActor," + request.getScript().getName()
							+ ",successful," + exeTime);
					if (result instanceof Response)
						response = (Response) result;
					else
						response = OK("result", result);
					getContext().dispatcher();
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
		if (null != scripts && !scripts.isEmpty()) {
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
							TelemetryManager.error("Error initialising command: " + script.getName(), e);
						}
					} else {
						interpreter.createCommand(script.getName(), new AkkaCommand(script));
					}
				} else if (StringUtils.equalsIgnoreCase(ScriptTypes.SCRIPT.name(), script.getType())) {
					interpreter.createCommand(script.getName(), new ScriptCommand(script));
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private boolean isListClass(Class cls) {
		if (null != cls) {
			Class[] arr = cls.getInterfaces();
			if (null != arr && arr.length > 0) {
				for (Class c : arr) {
					if (StringUtils.equals("java.util.List", c.getName()))
						return true;
				}
			}
		}
		return false;
	}

	@SuppressWarnings("rawtypes")
	private boolean checkDataType(Class cls, String name) {
		if (null != cls) {
			if (StringUtils.equals(cls.getName(), name))
				return true;
		}
		return false;
	}

	private Object execute(OrchestratorScript script, Map<String, Object> params) {
		try {
			if (StringUtils.equalsIgnoreCase(ScriptTypes.SCRIPT.name(), script.getType())) {
				List<ScriptParams> scriptParams = script.getParameters();
				if (null != scriptParams && !scriptParams.isEmpty()) {
					for (ScriptParams scriptParam : scriptParams) {
						String name = scriptParam.getName();
						Object paramObj = params.get(name);
						if (null != paramObj) {
							if (StringUtils.isNotBlank(scriptParam.getDatatype())) {
								TclObject obj = getTclObject(scriptParam.getDatatype(), paramObj);
								interpreter.setVar(name, obj, TCL.NAMESPACE_ONLY);
							} else {
								interpreter.setVar(name, paramObj.toString(), TCL.NAMESPACE_ONLY);
							}
						} else {
							TclObject obj = ReflectObject.newInstance(interpreter, Object.class, paramObj);
							interpreter.setVar(name, obj, TCL.NAMESPACE_ONLY);
						}
					}
				}
				interpreter.eval(script.getBody());
			} else {
				Command cmd = interpreter.getCommand(script.getName());
				List<TclObject> tclParamsList = new ArrayList<TclObject>();
				TclObject commandName = ReflectObject.newInstance(interpreter, String.class, script.getName());
				tclParamsList.add(commandName);

				TclObject[] tclParamsArray = new TclObject[script.getParameters().size() + 1];
				tclParamsArray[0] = commandName;

				List<ScriptParams> scriptParams = script.getParameters();
				for (ScriptParams scriptParam : scriptParams) {
					Object paramObj = params.get(scriptParam.getName());
					if (StringUtils.isNotBlank(scriptParam.getDatatype())) {
						TclObject tclObj = getTclObject(scriptParam.getDatatype(), paramObj);
						tclParamsArray[scriptParam.getIndex() + 1] = tclObj;
					} else {
						TclObject tclObj = ReflectObject.newInstance(interpreter, Object.class, paramObj);
						tclParamsArray[scriptParam.getIndex() + 1] = tclObj;
					}
				}
				cmd.cmdProc(interpreter, tclParamsArray);
			}
			TclObject tclObject = interpreter.getResult();
			if (null != tclObject.getInternalRep() && tclObject.getInternalRep() instanceof ReflectObject)
				return ReflectObject.get(interpreter, tclObject);
			else
				return tclObject.toString();
		} catch (TclException ex) {
			ex.printStackTrace();
			int code = ex.getCompletionCode();
			String msg = "";
			switch (code) {
			case TCL.ERROR:
				TelemetryManager.warn("tcl interpretation error" + interpreter.getResult().toString());
				msg = interpreter.getResult().toString();
				if (StringUtils.contains(msg, "tcl.lang.TclException") || StringUtils.contains(msg, "java.")) {
					msg = "| Invalid request format |";
				}
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private TclObject getTclObject(String datatype, Object paramObj) throws Exception {
		TclObject obj = null;
		Class cls = Class.forName(datatype);
		try {
			String objectStr = mapper.writeValueAsString(paramObj);
			Object javaObj = mapper.readValue(objectStr, cls);
			obj = ReflectObject.newInstance(interpreter, cls, javaObj);
		} catch (Exception e) {
			if (isListClass(cls)) {
				List list = new ArrayList();
				list.add(paramObj);
				obj = ReflectObject.newInstance(interpreter, list.getClass(), list);
			} else if (checkDataType(cls, InputStream.class.getName())) {
				if (paramObj instanceof MultipartFile) {
					MultipartFile mpf = (MultipartFile) paramObj;
					try (InputStream is = mpf.getInputStream()) {
						obj = ReflectObject.newInstance(interpreter, is.getClass(), is);
					}
				} else {
					obj = ReflectObject.newInstance(interpreter, Object.class, paramObj);
				}
			} else {
				obj = ReflectObject.newInstance(interpreter, Object.class, paramObj);
			}
		}
		return obj;
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
