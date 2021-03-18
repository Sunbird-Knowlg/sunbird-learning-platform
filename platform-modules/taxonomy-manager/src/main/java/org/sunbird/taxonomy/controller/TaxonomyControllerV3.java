package org.sunbird.taxonomy.controller;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.sunbird.common.dto.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/v3")
public class TaxonomyControllerV3 {

	@Autowired
	TaxonomyController taxonomyController;
	
	@RequestMapping(value = "/import/{id:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> create(@PathVariable(value = "id") String id,
			@RequestParam("file") MultipartFile file, @RequestHeader(value = "user-id") String userId,
			HttpServletResponse resp) {
		return taxonomyController.create(id, file, userId, resp);
	}

	@RequestMapping(value = "/export/{id:.+}/", method = RequestMethod.POST)
	@ResponseBody
	public void export(@PathVariable(value = "id") String id, @RequestBody Map<String, Object> map,
			@RequestHeader(value = "user-id") String userId, HttpServletResponse resp) {
		 taxonomyController.export(id, map, userId, resp);
	}
	
	@RequestMapping(value = "/definitions/update/{id:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> createDefinition(@PathVariable(value = "id") String id, @RequestBody String json,
			@RequestHeader(value = "user-id") String userId) {
		return taxonomyController.createDefinition(id, json, userId);
	}

	@RequestMapping(value = "/definitions/read/{objectType:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> findDefinition(@RequestParam(name = "graph_id", required = true) String id,
			@PathVariable(value = "objectType") String objectType, @RequestHeader(value = "user-id") String userId) {
		return taxonomyController.findDefinition(id, objectType, userId);
	}

	@RequestMapping(value = "/definitions/list", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> findAllDefinitions(@RequestParam(name = "graph_id", required = true)  String id,
			@RequestHeader(value = "user-id") String userId) {
		return taxonomyController.findAllDefinitions(id, userId);
	}
}
