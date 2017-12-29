package org.ekstep.orchestrator.interpreter.command;

import org.ekstep.orchestrator.interpreter.ICommand;
import org.ekstep.telemetry.logger.Level;
import org.ekstep.telemetry.logger.PlatformLogger;

import tcl.lang.Command;
import tcl.lang.Interp;
import tcl.lang.TclException;
import tcl.lang.TclNumArgsException;
import tcl.lang.TclObject;

public class Logs extends BaseSystemCommand implements ICommand, Command {

	@Override
	public void cmdProc(Interp interp, TclObject[] argv) throws TclException {
        if (argv.length == 2) {
            try {
                TclObject tclObject1 = argv[1];
                if (null == tclObject1) {
                    throw new TclException(interp, "Null arguments to " + getCommandName());
                } else {
                    String logMessage = tclObject1.toString();
                    PlatformLogger.log("TclLogMessage", logMessage, Level.ERROR.name());
                    interp.setResult(true);
                }
            } catch (Exception e) {
                throw new TclException(interp, e.getMessage());
            }
        } else {
            throw new TclNumArgsException(interp, 1, argv, "Invalid arguments to get_resp_value command");
        }
	}

	@Override
	public String getCommandName() {
        return "logs";
    }

}
