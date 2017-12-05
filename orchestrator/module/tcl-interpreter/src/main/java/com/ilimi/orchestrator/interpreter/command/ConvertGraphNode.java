package com.ilimi.orchestrator.interpreter.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.model.node.DefinitionDTO;

import com.ilimi.orchestrator.interpreter.ICommand;

import tcl.lang.Command;
import tcl.lang.Interp;
import tcl.lang.TclException;
import tcl.lang.TclNumArgsException;
import tcl.lang.TclObject;
import tcl.pkg.java.ReflectObject;

public class ConvertGraphNode extends BaseSystemCommand implements ICommand, Command {
	
    @Override
    public String getCommandName() {
        return "convert_graph_node";
    }

    @SuppressWarnings("unchecked")
    @Override
    public void cmdProc(Interp interp, TclObject[] argv) throws TclException {
        if (argv.length >= 3) {
            try {
                TclObject tclObject1 = argv[1];
                TclObject tclObject2 = argv[2];
                if (null == tclObject1 || null == tclObject2) {
                    throw new TclException(interp, "Null arguments to " + getCommandName());
                } else {
                    Object obj1 = ReflectObject.get(interp, tclObject1);
                    Node node = (Node) obj1;
                    Object obj2 = ReflectObject.get(interp, tclObject2);
                    DefinitionDTO def = (DefinitionDTO) obj2;
                    List<String> fields = null;
                    if (argv.length > 3) {
                        TclObject tclObject3 = argv[3];
                        Object obj3 = ReflectObject.get(interp, tclObject3);
                        fields = (List<String>) obj3;
                    } else {
                        if (null != def && null != def.getMetadata()) {
                        	try {
                        		String[] arr = (String[]) def.getMetadata().get("fields");
                        		if (null != arr && arr.length > 0) {
                                    fields = new ArrayList<String>();
                                    for (String field : arr)
                                    	fields.add(field);
                                }
                        	} catch (Exception e) {
                        		List<String> arr = (List<String>) def.getMetadata().get("fields");
                        		if (null != arr && arr.size() > 0) {
                                    fields = new ArrayList<String>();
                                    fields.addAll(arr);
                                }
                        	}
                        }
                    }
                    Map<String, Object> map = com.ilimi.common.mgr.ConvertGraphNode.convertGraphNode(node, node.getGraphId(), def, fields);
                    TclObject tclResp = ReflectObject.newInstance(interp, map.getClass(), map);
                    interp.setResult(tclResp);
                }

            } catch (Exception e) {
            	e.printStackTrace();
                throw new TclException(interp, "Unable to read response: " + e.getMessage());
            }
        } else {
            throw new TclNumArgsException(interp, 1, argv, "Invalid arguments to get_resp_value command");
        }
    }

}
