package com.ilimi.orchestrator.interpreter.command;

import java.util.HashMap;
import java.util.List;
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

public class GetWeightagesMap implements ICommand, Command {

	ObjectMapper mapper = new ObjectMapper();

	@Override
	public void cmdProc(Interp interp, TclObject[] argv) throws TclException {
		Map<String, Double> weightagesMap = new HashMap<String, Double>();
		if (argv.length == 2) {
			try {
				TclObject tclObject = argv[1];
				Object obj = ReflectObject.get(interp, tclObject);
				String weightagesJson = (String) obj;
				if (weightagesJson != null && !weightagesJson.isEmpty()) {
					Map<String, Object> weightagesRequestMap = mapper.readValue(weightagesJson,
							new TypeReference<Map<String, Object>>() {
							});

					for (Map.Entry<String, Object> entry : weightagesRequestMap.entrySet()) {
						Double weightage = Double.parseDouble(entry.getKey());
						if (entry.getValue() instanceof List) {
							List<String> fields = (List<String>) entry.getValue();
							for (String field : fields) {
								weightagesMap.put(field, weightage);
							}
						} else {
							String field = (String) entry.getValue();
							weightagesMap.put(field, weightage);
						}
					}
				}
			} catch (Exception e) {
				throw new TclException(interp, "Unable to parse the json string");
			}
			interp.setResult(ReflectObject.newInstance(interp, weightagesMap.getClass(), weightagesMap));
		} else {
			throw new TclNumArgsException(interp, 1, argv, "Invalid arguments to json_parser command");
		}
	}

	@Override
	public String getCommandName() {
		return "get_weightages_map";
	}

}
