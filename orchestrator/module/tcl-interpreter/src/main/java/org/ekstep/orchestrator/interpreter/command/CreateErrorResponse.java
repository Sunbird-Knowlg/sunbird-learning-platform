package org.ekstep.orchestrator.interpreter.command;

import java.util.Map;

import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ResponseCode;

import org.ekstep.orchestrator.interpreter.ICommand;

import tcl.lang.Command;
import tcl.lang.Interp;
import tcl.lang.TclException;
import tcl.lang.TclNumArgsException;
import tcl.lang.TclObject;
import tcl.pkg.java.ReflectObject;

public class CreateErrorResponse extends BaseSystemCommand implements ICommand, Command {

    @Override
    public String getCommandName() {
        return "create_error_response";
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void cmdProc(Interp interp, TclObject[] argv) throws TclException {
        if (argv.length == 2) {
            try {
                TclObject tclObject = argv[1];
                if (null == tclObject) {
                    throw new TclException(interp, "Null arguments to " + getCommandName());
                } else {
                    Object obj = ReflectObject.get(interp, tclObject);
                    Map<String, Object> map = (Map<String, Object>) obj;
                    String errorCode = (String) map.get("code");
                    String message = (String) map.get("message");
                    Integer code = (Integer) map.get("responseCode");
                    Map<String, Object> result = (Map<String, Object>) map.get("result");
                    Response response = ERROR(errorCode, message, ResponseCode.getResponseCode(code));
                    if (null != result && !result.isEmpty())
                        response.getResult().putAll(map);
                    TclObject tclResp = ReflectObject.newInstance(interp, response.getClass(), response);
                    interp.setResult(tclResp);
                }
            } catch (Exception e) {
                throw new TclException(interp, "Unable to read response: " + e.getMessage());
            }
        } else {
            throw new TclNumArgsException(interp, 1, argv, "Invalid arguments to get_resp_value command");
        }
    }
}
