package com.ilimi.orchestrator.interpreter.command;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.ilimi.common.dto.TelemetryEvent;
import com.ilimi.orchestrator.interpreter.ICommand;

import tcl.lang.Command;
import tcl.lang.Interp;
import tcl.lang.TclException;
import tcl.lang.TclNumArgsException;
import tcl.lang.TclObject;
import tcl.pkg.java.ReflectObject;

public class LogAsTelemetryEvent extends BaseSystemCommand implements ICommand, Command {

	private static final Logger telemetryEventMessageLogger = LogManager.getLogger("TelemetryEventMessageLogger");
	private static ObjectMapper mapper = new ObjectMapper();
	
    @Override
    public String getCommandName() {
        return "log_as_telemetry_event";
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
                    String contentId = tclObject1.toString();
                    Object obj2 = ReflectObject.get(interp, tclObject2);
                    Map<String, Object> map = (Map<String, Object>) obj2;
                    logAsTelemeryEvent(contentId, map);
                    interp.setResult(true);
                }
            } catch (Exception e) {
                throw new TclException(interp, e.getMessage());
            }
        } else {
            throw new TclNumArgsException(interp, 1, argv, "Invalid arguments to get_resp_value command");
        }
	}


    private void logAsTelemeryEvent(String contentId, Map<String, Object> metadata){

    	TelemetryEvent te=new TelemetryEvent();
    	long unixTime = System.currentTimeMillis() / 1000L;
    	te.setEid("BE_CONTENT_LIFECYCLE");
    	te.setEts(unixTime);
    	te.setVer("2.0");
    	te.setPdata("org.ekstep.content.platform", "", "1.0", "");
    	te.setEdata(contentId, metadata.get("status"), metadata.get("size"), metadata.get("pkgVersion"), metadata.get("concepts"), metadata.get("flags"));
		String jsonMessage ;
		try{
			jsonMessage= mapper.writeValueAsString(te);
			if (StringUtils.isNotBlank(jsonMessage))
				telemetryEventMessageLogger.info(jsonMessage);
		}catch(Exception e){
		
		}
    }
}
