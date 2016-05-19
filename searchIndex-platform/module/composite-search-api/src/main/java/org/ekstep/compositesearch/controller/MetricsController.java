package org.ekstep.compositesearch.controller;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.ekstep.compositesearch.mgr.ICompositeSearchManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;

@Controller
@RequestMapping("v2/metrics")
public class MetricsController extends BaseCompositeSearchController {
	
	@Autowired
	private ICompositeSearchManager compositeSearchManager;
	
	@RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> metrics(@RequestBody Map<String, Object> map, 
    		@RequestHeader(value = "user-id") String userId,
            HttpServletResponse resp) {
        String apiId = "composite-search.metrics";
        try {
        	Request request = getRequest(map);
            Response response = compositeSearchManager.metrics(request);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

}
