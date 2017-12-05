package com.ilimi.orchestrator.interpreter.command;

import org.ekstep.graph.dac.model.Node;

import com.ilimi.orchestrator.interpreter.ICommand;

import tcl.lang.Command;
import tcl.lang.Interp;
import tcl.lang.TclException;
import tcl.lang.TclNumArgsException;
import tcl.lang.TclObject;
import tcl.pkg.java.ReflectObject;

//this command class is deprecated, no more in use
public class LogTranslationLifecycleEvent extends BaseSystemCommand implements ICommand, Command {
	
    @Override
    public String getCommandName() {
        return "log_translation_lifecycle_event";
    }
    
    @SuppressWarnings("unchecked")
	@Override
	public void cmdProc(Interp interp, TclObject[] argv) throws TclException {
        if (argv.length == 3) {
            try {
                TclObject tclObject1 = argv[1];
                TclObject tclObject2 = argv[2];
                if (null == tclObject1 || null == tclObject2) {
                    throw new TclException(interp, "Null arguments to " + getCommandName());
                } else {
                    String wordId = tclObject1.toString();
                    Object obj2 = ReflectObject.get(interp, tclObject2);
                    Node node = (Node) obj2;
//                    LogWordEventUtil.logWordLifecycleEvent(wordId, node.getMetadata());
                    interp.setResult(true);
                }
            } catch (Exception e) {
                throw new TclException(interp, e.getMessage());
            }
        } else {
            throw new TclNumArgsException(interp, 1, argv, "Invalid arguments to get_resp_value command");
        }
	}

}