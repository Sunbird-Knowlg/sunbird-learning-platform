package com.ilimi.orchestrator.interpreter.command;

import org.ekstep.common.dto.Response;

import com.ilimi.orchestrator.interpreter.ICommand;

import tcl.lang.Command;
import tcl.lang.Interp;
import tcl.lang.TclException;
import tcl.lang.TclNumArgsException;
import tcl.lang.TclObject;
import tcl.pkg.java.ReflectObject;

public class MergeResponse implements ICommand, Command {

	@Override
	public void cmdProc(Interp interp, TclObject[] argv) throws TclException {
		if (argv.length >= 3) {
			try {
				TclObject tclObject = argv[1];
				Object obj = ReflectObject.get(interp, tclObject);
				Response result = (Response) obj;
				for (int i = 2; i < argv.length; i++) {
					tclObject = argv[i];
					Object respObj = ReflectObject.get(interp, tclObject);
					Response response = (Response) respObj;
					if (null != response.getResult() && !response.getResult().isEmpty()) {
						result.getResult().putAll(response.getResult());
					}
				}
				TclObject tclResp = ReflectObject.newInstance(interp, result.getClass(), result);
				interp.setResult(tclResp);
			} catch (Exception e) {
				throw new TclException(interp, "Unable to read response: " + e.getMessage());
			}
		} else {
			throw new TclNumArgsException(interp, 1, argv, "Invalid arguments to merge_response command");
		}

	}

	@Override
	public String getCommandName() {
		return "merge_response";
	}

}
