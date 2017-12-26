package org.ekstep.orchestrator.interpreter.command;

import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import org.ekstep.orchestrator.interpreter.ICommand;

import tcl.lang.Command;
import tcl.lang.Interp;
import tcl.lang.TclException;
import tcl.lang.TclNumArgsException;
import tcl.lang.TclObject;
import tcl.pkg.java.ReflectObject;


public class convertJSONStringToMap extends BaseSystemCommand implements ICommand, Command {

	private ObjectMapper mapper = new ObjectMapper();
	
	@Override
	public String getCommandName() {
		return "convert_jsonstring_to_map";
	}

	@Override
	public void cmdProc(Interp interp, TclObject[] argv) throws TclException {
        if (argv.length == 2) {
            TclObject tclObject1 = argv[1];
            if (null == tclObject1 ) {
                throw new TclException(interp, "Null arguments to " + getCommandName());
            } else {
                String jsonString = tclObject1.toString();
        		Map<String, Object> resultMap;
				try {
					resultMap = mapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {
					});
				} catch (Exception e) {
	                throw new TclException(interp, "Unable to convert string to Map :" + e.getMessage());
				}

				TclObject tclResp = ReflectObject.newInstance(interp, resultMap.getClass(), resultMap);
				interp.setResult(tclResp);
            }

        }else{
            throw new TclNumArgsException(interp, 1, argv, "Invalid arguments to convert_jsonstring_to_map command");
        }
	}

}
