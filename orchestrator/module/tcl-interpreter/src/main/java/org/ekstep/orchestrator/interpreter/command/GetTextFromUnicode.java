package org.ekstep.orchestrator.interpreter.command;

import java.util.List;

import org.ekstep.language.measures.meta.SyllableMap;

import org.ekstep.orchestrator.interpreter.ICommand;

import tcl.lang.Command;
import tcl.lang.Interp;
import tcl.lang.TclException;
import tcl.lang.TclNumArgsException;
import tcl.lang.TclObject;
import tcl.pkg.java.ReflectObject;

public class GetTextFromUnicode implements ICommand, Command {
	
    static {
        SyllableMap.loadSyllables("te");
        SyllableMap.loadSyllables("hi");
        SyllableMap.loadSyllables("ka");
    }

    @SuppressWarnings("unchecked")
	@Override
    public void cmdProc(Interp interp, TclObject[] argv) throws TclException {
        if (argv.length == 3) {
            try {
            	TclObject tclObject = argv[1];
                Object obj = ReflectObject.get(interp, tclObject);
                String language = (String) obj;
            	
                tclObject = argv[2];
                obj = ReflectObject.get(interp, tclObject);
                List<String> unicodes = (List<String>) obj;

                String text = "";
        		for(String unicode: unicodes){
        			if(!SyllableMap.getDefaultVowel(language).equalsIgnoreCase(unicode)){
        			    int hexVal = Integer.parseInt(unicode, 16);
        			    text += (char)hexVal;
        			}
        		}
                
                TclObject tclResp = ReflectObject.newInstance(interp, text.getClass(), text);
                interp.setResult(tclResp);
            } catch (Exception e) {
                throw new TclException(interp, "Unable to read response: " + e.getMessage());
            }
        } else {
            throw new TclNumArgsException(interp, 1, argv, "Invalid arguments to get_syllables_word command");
        }
    }

	@Override
    public String getCommandName() {
        return "get_text_from_unicode";
    }
}
