package com.ilimi.orchestrator.interpreter.command;

import java.util.Map;

import com.ilimi.common.dto.Response;
import com.ilimi.orchestrator.interpreter.ICommand;

import tcl.lang.Command;
import tcl.lang.Interp;
import tcl.lang.TclException;
import tcl.lang.TclNumArgsException;
import tcl.lang.TclObject;
import tcl.pkg.java.ReflectObject;

public class CreateResponse extends BaseSystemCommand implements ICommand, Command {

    @Override
    public String getCommandName() {
        return "create_response";
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
                    Response response = OK();
                    Object obj = ReflectObject.get(interp, tclObject);
                    Map<String, Object> map = (Map<String, Object>) obj;
                    if(map.containsKey("node_id")){
                     String identifier = (String)map.get("node_id");
           			 String new_identifier = identifier.replace(".img", "");
                     map.replace("node_id", identifier, new_identifier);
                    }
                    if (null != map && !map.isEmpty())
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
