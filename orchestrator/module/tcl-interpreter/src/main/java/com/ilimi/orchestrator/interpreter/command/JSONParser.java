package com.ilimi.orchestrator.interpreter.command;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import tcl.lang.Command;
import tcl.lang.Interp;
import tcl.lang.TclException;
import tcl.lang.TclNumArgsException;
import tcl.lang.TclObject;
import tcl.pkg.java.ReflectObject;

import com.ilimi.orchestrator.interpreter.ICommand;

public class JSONParser implements ICommand, Command {

	ObjectMapper mapper = new ObjectMapper();

	@Override
	public void cmdProc(Interp interp, TclObject[] argv) throws TclException {
		Map<String, String> map = new HashMap<String, String>();
		if (argv.length == 2) {
			try {
				map = mapper.readValue(argv[1].toString(), new TypeReference<HashMap<String, String>>() {
				});
			} catch (Exception e) {
				throw new TclException(interp, "Unable to parse the json string");
			}
			interp.setResult(ReflectObject.newInstance(interp, Map.class, map));
		} else {
			throw new TclNumArgsException(interp, 1, argv, "Invalid arguments to json_parser command");
		}
	}

	@Override
	public String getCommandName() {
		return "json_parser";
	}

}
