package com.ilimi.orchestrator.interpreter.command;

import com.ilimi.common.dto.Response;
import com.ilimi.common.logger.LogHelper;
import com.ilimi.orchestrator.interpreter.ICommand;

import tcl.lang.Command;
import tcl.lang.Interp;
import tcl.lang.TclException;
import tcl.lang.TclNumArgsException;
import tcl.lang.TclObject;
import tcl.pkg.java.ReflectObject;

public class GetResponseValue implements ICommand, Command {
	
	private static LogHelper LOGGER = LogHelper.getInstance(GetResponseValue.class.getName());

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
				TclObject tclResp = ReflectObject.newInstance(interp, result.getClass(), result);
				interp.setResult(tclResp);
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new TclException(interp, "Unable to read response: " + e.getMessage());
			}
		} else {
			throw new TclNumArgsException(interp, 1, argv, "Invalid arguments to get_resp_value command");
		}
	}

}
