package com.ilimi.orchestrator.interpreter.command;

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

public class ConvertToGraphNode extends BaseSystemCommand implements ICommand, Command {
    

    @Override
    public String getCommandName() {
        return "convert_to_graph_node";
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
                    Map<String, Object> map = (Map<String, Object>) obj1;
                    Object obj2 = ReflectObject.get(interp, tclObject2);
                    DefinitionDTO def = (DefinitionDTO) obj2;
                    Node graphNode = null;
                    if (argv.length == 4) {
                    	TclObject tclObject3 = argv[3];
                        if (null != tclObject3) {
                        	Object obj3 = ReflectObject.get(interp, tclObject3);
                        	graphNode = (Node) obj3;
                        }
                    }
                    Node node = com.ilimi.common.mgr.ConvertToGraphNode.convertToGraphNode(map, def, graphNode);
                    TclObject tclResp = ReflectObject.newInstance(interp, node.getClass(), node);
                    interp.setResult(tclResp);
                }

            } catch (Exception e) {
                throw new TclException(interp, e.getMessage());
            }
        } else {
            throw new TclNumArgsException(interp, 1, argv, "Invalid arguments to get_resp_value command");
        }
    }

}
