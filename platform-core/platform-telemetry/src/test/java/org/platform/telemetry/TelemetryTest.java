package org.platform.telemetry;

import java.util.HashMap;
import java.util.Map;

import org.platform.telemetry.dto.Actor;
import org.platform.telemetry.dto.Context;
import org.platform.telemetry.dto.Producer;
import org.platform.telemetry.dto.Telemetry;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TelemetryTest {

	public static void main(String[] args) throws Exception {
		
		Actor actor = new Actor("Learning-Platform", "1.0");
		Context context = new Context("in.ekstep", "local", new Producer("org.ekstep.content.platform", "1.0"));
		Map<String, Object> edata = new HashMap<String, Object>();
		Telemetry tel = new Telemetry("LOG", actor, context, edata);
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(tel);
		System.out.println("Telemetry: "+ json);
	}
	
}
