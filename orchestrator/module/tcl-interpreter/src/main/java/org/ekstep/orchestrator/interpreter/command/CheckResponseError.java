package org.ekstep.orchestrator.interpreter.command;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Response;
import org.ekstep.common.dto.ResponseParams.StatusType;

import org.ekstep.orchestrator.interpreter.ICommand;

import tcl.lang.Command;
import tcl.lang.Interp;
import tcl.lang.TclException;
import tcl.lang.TclNumArgsException;
import tcl.lang.TclObject;
import tcl.pkg.java.ReflectObject;

public class CheckResponseError implements ICommand, Command {

	@Override
	public void cmdProc(Interp interp, TclObject[] argv) throws TclException {
		if (argv.length == 2) {
			try {
				TclObject tclObject = argv[1];
				Object obj = ReflectObject.get(interp, tclObject);
				Response response = (Response) obj;
				boolean error = false;
				if (!StringUtils.equalsIgnoreCase(StatusType.successful.name(), response.getParams().getStatus())) {
					error = true;
				}
				interp.setResult(error);
			} catch (Exception e) {
				throw new TclException(interp, "Unable to read response: " + e.getMessage());
			}
		} else {
			throw new TclNumArgsException(interp, 1, argv, "Invalid arguments to check_response_error command");
		}
	}

	@Override
	public String getCommandName() {
		return "check_response_error";
	}

}
