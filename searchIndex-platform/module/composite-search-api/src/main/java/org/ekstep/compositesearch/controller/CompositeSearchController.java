package org.ekstep.compositesearch.controller;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.ekstep.compositesearch.mgr.ICompositeSearchManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;

@Controller
@RequestMapping("v2/search")
public class CompositeSearchController extends BaseCompositeSearchController {
	
	//TODO: Initialize the LOGGER Put the Logg message in each section and catch block 
	
	@Autowired
	private ICompositeSearchManager compositeSearchManager;
	
	@RequestMapping(value = "/sync/{id:.+}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> create(@PathVariable(value = "id") String graphId,
    		@RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId,
            HttpServletResponse resp) {
        String apiId = "composite-search.sync";
        try {
        	Request request = getRequest(map);
            Response response = compositeSearchManager.sync(graphId, request);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            return getExceptionResponseEntity(e, apiId, null);
        }
    }
	
	@RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> search(@PathVariable(value = "id") String graphId,
    		@RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId,
            HttpServletResponse resp) {
        String apiId = "composite-search.sync";
        try {
        	Request request = getRequest(map);
            Response response = compositeSearchManager.sync(graphId, request);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

}
