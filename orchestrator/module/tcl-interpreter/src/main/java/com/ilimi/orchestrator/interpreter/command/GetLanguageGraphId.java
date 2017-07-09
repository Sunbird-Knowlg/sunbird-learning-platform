package com.ilimi.orchestrator.interpreter.command;

import java.lang.Character.UnicodeBlock;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.language.common.LanguageMap;

import tcl.lang.Command;
import tcl.lang.Interp;
import tcl.lang.TclException;
import tcl.lang.TclNumArgsException;
import tcl.lang.TclObject;
import tcl.pkg.java.ReflectObject;

import com.ilimi.orchestrator.interpreter.ICommand;

public class GetLanguageGraphId implements ICommand, Command {

    @Override
    public void cmdProc(Interp interp, TclObject[] argv) throws TclException {
        if (argv.length == 2) {
            try {
                TclObject tclObject = argv[1];
                Object obj = ReflectObject.get(interp, tclObject);
                String unicodeString = (String) obj;
                char unicodeChar = unicodeString.toCharArray()[0];
                //String.format("\\u%04x", (int) unicodeChar);
               // unicodeString = "\\u"+unicodeString;
               // char unicode = (char) Integer.parseInt( unicodeString.substring(2), 16 );
                String language = null;
        		UnicodeBlock charBlock = UnicodeBlock.of(unicodeChar);
        		if (charBlock.equals(Character.UnicodeBlock.DEVANAGARI_EXTENDED) || charBlock.equals(Character.UnicodeBlock.DEVANAGARI)) {
        			language = "HINDI";
        		}
        		else if(charBlock.equals(Character.UnicodeBlock.BASIC_LATIN)){
        			language = "ENGLISH";
        		}
        		else{
        			language = charBlock.toString();
        		}
        		language = StringUtils.capitalize(language.toLowerCase());
        		String graphId = LanguageMap.getLanguageGraph(language);
                TclObject tclResp = ReflectObject.newInstance(interp, graphId.getClass(), graphId);
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
        return "get_language_graph_id";
    }
}
