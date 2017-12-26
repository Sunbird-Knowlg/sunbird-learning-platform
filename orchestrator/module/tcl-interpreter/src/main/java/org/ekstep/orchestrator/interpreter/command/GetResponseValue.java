package org.ekstep.orchestrator.interpreter.command;

import org.ekstep.common.dto.Response;
import org.ekstep.common.logger.PlatformLogger;

import org.ekstep.orchestrator.interpreter.ICommand;

import tcl.lang.Command;
import tcl.lang.Interp;
import tcl.lang.TclException;
import tcl.lang.TclNumArgsException;
import tcl.lang.TclObject;
import tcl.pkg.java.ReflectObject;

public class GetResponseValue implements ICommand, Command {
	
	

	@Override
	public String getCommandName() {
		return "get_resp_value";
	}

	@Override
	public void cmdProc(Interp interp, TclObject[] argv) throws TclException {
		if (argv.length == 3) {
			try {
				TclObject tclObject = argv[1];
				Object obj = ReflectObject.get(interp, tclObject);
				Response response = (Response) obj;
				String param = argv[2].toString();
				Object result = response.get(param);
				if (null != result) {
					TclObject tclResp = ReflectObject.newInstance(interp, result.getClass(), result);
					interp.setResult(tclResp);
				} else {
					TclObject tclResp = ReflectObject.newInstance(interp, Object.class, null);
					interp.setResult(tclResp);
				}
			} catch (Exception e) {
				PlatformLogger.log("Exception", e.getMessage(), e);
				throw new TclException(interp, "Unable to read response: " + e.getMessage());
			}
		} else {
			throw new TclNumArgsException(interp, 1, argv, "Invalid arguments to get_resp_value command");
		}
	}

}
