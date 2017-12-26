package org.ekstep.orchestrator.interpreter.command;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.ekstep.orchestrator.interpreter.ICommand;

import tcl.lang.Command;
import tcl.lang.Interp;
import tcl.lang.TclException;
import tcl.lang.TclNumArgsException;
import tcl.lang.TclObject;
import tcl.pkg.java.ReflectObject;

public class SortMaps extends BaseSystemCommand implements ICommand, Command {

	private final String ASC_ORDER="ASC";
	private final String DESC_ORDER="DESC";
	
	@Override
	public String getCommandName() {
		return "sort_maps";
	}

	@Override
	public void cmdProc(Interp interp, TclObject[] argv) throws TclException {
        if (argv.length == 4) {
            TclObject tclObject1 = argv[1];
            TclObject tclObject2 = argv[2];
            TclObject tclObject3 = argv[3];
            if (null == tclObject1 || null == tclObject2 || null == tclObject3) {
                throw new TclException(interp, "Null arguments to " + getCommandName());
            } else {
                Object obj1 = ReflectObject.get(interp, tclObject1);
                List<Map<String, Object>> maps = (List<Map<String, Object>>) obj1;
                final String sortField = tclObject2.toString();
                final String sortOrder = tclObject3.toString();
                if (!sortOrder.equalsIgnoreCase(ASC_ORDER) && !sortOrder.equalsIgnoreCase(DESC_ORDER)){
                	throw new TclException(interp, "sortOrder should be either asc/ASC or desc/DESC ");
                }
                Collections.sort(maps, new Comparator<Map<String, Object>>() {
            		@Override
            		public int compare(Map<String, Object> map1, Map<String, Object> map2) {
            			    Comparable c1= (Comparable) map1.get(sortField);
            			    Comparable c2= (Comparable) map2.get(sortField);
            				if(sortOrder.equalsIgnoreCase(ASC_ORDER))
            					return c1.compareTo(c2);
            				else
            					return c2.compareTo(c1);
            		}
            	});
                
				TclObject tclResp = ReflectObject.newInstance(interp, maps.getClass(), maps);
				interp.setResult(tclResp);
            }

        }else{
            throw new TclNumArgsException(interp, 1, argv, "Invalid arguments to sort_maps command");
        }
	}

}
