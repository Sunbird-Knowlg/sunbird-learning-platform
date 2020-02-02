package org.ekstep.content.operation.finalizer;

import java.util.HashMap;
import java.util.Map;

import org.ekstep.common.dto.Response;
import org.ekstep.common.dto.ResponseParams;
import org.ekstep.common.mgr.ConvertToGraphNode;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.engine.common.GraphEngineTestSetup;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.learning.util.ControllerUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ReviewFinalizer.class})
@PowerMockIgnore({ "javax.management.*", "sun.security.ssl.*", "javax.net.ssl.*", "javax.crypto.*" })
public class ReviewFinalizerTest extends GraphEngineTestSetup{

	ObjectMapper mapper = new ObjectMapper();

	@BeforeClass
	public static void create() throws Exception {
		loadDefinition("definitions/content_definition.json", "definitions/content_image_definition.json");
	}

	@AfterClass
	public static void destroy() {}
	
	
	/*@Test
	public void validateResourceTest1() {
		
		
		String result = "{hierarchy={identifier=do_11294852598716006416, children=null}}";
		ControllerUtil controllerUtil = PowerMockito.spy(new ControllerUtil());
		ReviewFinalizer reviewFinalizer = PowerMockito.spy(new ReviewFinalizer(controllerUtil));
		Response response = new Response();
		ResponseParams params = new ResponseParams();
		params.setStatus("failed");
		response.setParams(params);
		PowerMockito.doReturn(response).when(reviewFinalizer.getCollectionHierarchy("do_11294852598716006416.img"));
		
		params.setStatus("failed");
		response.setParams(params);
		PowerMockito.doReturn(response).when(reviewFinalizer.getCollectionHierarchy("do_11294852598716006416"));
		
	}*/
}
