package com.ilimi.orchestrator.interpreter.command;

import java.util.Map;
import java.util.Map.Entry;

import org.ekstep.common.dto.Property;

import com.ilimi.orchestrator.interpreter.ICommand;

import tcl.lang.Command;
import tcl.lang.Interp;
import tcl.lang.TclException;
import tcl.lang.TclNumArgsException;
import tcl.lang.TclObject;
import tcl.pkg.java.ReflectObject;

public class CreateSearchProperty implements ICommand, Command {

    @SuppressWarnings({ "unchecked" })
    @Override
    public void cmdProc(Interp interp, TclObject[] argv) throws TclException {
        if (argv.length == 2) {
            try {
                TclObject tclObject = argv[1];
                Object obj = ReflectObject.get(interp, tclObject);
                Map<String, Object> map = (Map<String, Object>) obj;

                Property searchProperty = new Property();
                if (null != map && !map.isEmpty()) {
                    for (Entry<String, Object> entry : map.entrySet()) {
                    	searchProperty = new Property(entry.getKey(),entry.getValue());
                    	break;
                    }
                }
                TclObject tclResp = ReflectObject.newInstance(interp, searchProperty.getClass(), searchProperty);
                interp.setResult(tclResp);
            } catch (Exception e) {
                throw new TclException(interp, "Unable to read response: " + e.getMessage());
            }
        } else {
            throw new TclNumArgsException(interp, 1, argv, "Invalid arguments to check_response_error command");
        }
    }

    @Override
    public String getCommandName() {
        return "create_search_property";
    }
}
