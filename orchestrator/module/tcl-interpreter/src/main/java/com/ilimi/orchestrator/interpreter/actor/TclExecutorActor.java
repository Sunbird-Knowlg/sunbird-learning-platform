package com.ilimi.orchestrator.interpreter.actor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.web.multipart.MultipartFile;

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
import com.ilimi.orchestrator.interpreter.command.ScriptCommand;
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
    private ObjectMapper mapper = new ObjectMapper();

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
                    if (result instanceof Response)
                        response = (Response) result;
                    else
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
                            throw new MiddlewareException(ExecutionErrorCodes.ERR_INIT_INVALID_COMMAND.name(),
                                    e.getMessage(), e);
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Object execute(OrchestratorScript script, Map<String, Object> params) {
        try {
            List<ScriptParams> scriptParams = script.getParameters();
            if (null != scriptParams && !scriptParams.isEmpty()) {
                for (ScriptParams scriptParam : scriptParams) {
                    String name = scriptParam.getName();
                    Object paramObj = params.get(name);
                    if (null != paramObj) {
                        if (StringUtils.isNotBlank(scriptParam.getDatatype())) {
                            Class cls = Class.forName(scriptParam.getDatatype());
                            try {
                                String objectStr = mapper.writeValueAsString(paramObj);
                                Object javaObj = mapper.readValue(objectStr, cls);
                                TclObject obj = ReflectObject.newInstance(interpreter, cls, javaObj);
                                interpreter.setVar(name, obj, TCL.NAMESPACE_ONLY);
                            } catch (Exception e) {
                                if (isListClass(cls)) {
                                    List list = new ArrayList();
                                    list.add(paramObj);
                                    TclObject obj = ReflectObject.newInstance(interpreter, list.getClass(), list);
                                    interpreter.setVar(name, obj, TCL.NAMESPACE_ONLY);
                                } else if (checkDataType(cls, InputStream.class.getName())) {
                                    if (paramObj instanceof MultipartFile) {
                                        MultipartFile mpf = (MultipartFile) paramObj;
                                        InputStream is = mpf.getInputStream();
                                        TclObject obj = ReflectObject.newInstance(interpreter, is.getClass(), is);
                                        interpreter.setVar(name, obj, TCL.NAMESPACE_ONLY);
                                    } else {
                                        TclObject obj = ReflectObject.newInstance(interpreter, Object.class, paramObj);
                                        interpreter.setVar(name, obj, TCL.NAMESPACE_ONLY);
                                    }
                                } else {
                                    TclObject obj = ReflectObject.newInstance(interpreter, Object.class, paramObj);
                                    interpreter.setVar(name, obj, TCL.NAMESPACE_ONLY);
                                }
                            }
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
