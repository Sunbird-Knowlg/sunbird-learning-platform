package com.ilimi.taxonomy.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

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

import com.ilimi.common.controller.BaseController;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogManager;
import com.ilimi.graph.common.enums.GraphEngineParams;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.enums.ImportType;
import com.ilimi.graph.importer.OutputStreamValue;
import com.ilimi.taxonomy.enums.TaxonomyAPIParams;
import com.ilimi.taxonomy.mgr.ITaxonomyManager;

@Controller
@RequestMapping("/v3/system")
public class GraphV3Controller extends BaseController {

	private static ILogger LOGGER = PlatformLogManager.getLogger();

	@Autowired
	private ITaxonomyManager taxonomyManager;

	@RequestMapping(value = "/import/{id:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> create(@PathVariable(value = "id") String id,
			@RequestParam("file") MultipartFile file, @RequestHeader(value = "user-id") String userId,
			HttpServletResponse resp) {
		String apiId = "ekstep.graph.import";
		LOGGER.log("Create | Id: " + id + " | File: " + file + " | user-id: " + userId);
		InputStream stream = null;
		try {
			if (null != file)
				stream = file.getInputStream();
			Response response = taxonomyManager.create(id, stream);
			LOGGER.log("Create | Response: " , response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.log("Create | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		} finally {
			if (null != stream)
				try {
					stream.close();
				} catch (IOException e) {
					LOGGER.log("Error1 While Closing the Stream.", e.getMessage(), e);
				}
		}
	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/export/{id:.+}", method = RequestMethod.POST)
	@ResponseBody
	public void export(@PathVariable(value = "id") String id, @RequestBody Map<String, Object> map,
			@RequestHeader(value = "user-id") String userId, HttpServletResponse resp) {
		String format = ImportType.CSV.name();
		String apiId = "ekstep.graph.export";
		LOGGER.log("Export | Id: " + id + " | Format: " + format + " | user-id: " + userId);
		try {
			Request req = getRequest(map);
			try {
				SearchCriteria criteria = mapper.convertValue(req.get(TaxonomyAPIParams.search_criteria.name()),
						SearchCriteria.class);
				req.put(TaxonomyAPIParams.search_criteria.name(), criteria);
			} catch (Exception e) {
			}
			req.put(GraphEngineParams.format.name(), format);
			Response response = taxonomyManager.export(id, req);
			if (!checkError(response)) {
				OutputStreamValue graphOutputStream = (OutputStreamValue) response
						.get(GraphEngineParams.output_stream.name());
				try (OutputStream os = graphOutputStream.getOutputStream();
						ByteArrayOutputStream bos = (ByteArrayOutputStream) os) {
					byte[] bytes = bos.toByteArray();
					resp.setContentType("text/csv");
					resp.setHeader("Content-Disposition", "attachment; filename=graph.csv");
					resp.getOutputStream().write(bytes);
					resp.getOutputStream().close();
				}
			}
			LOGGER.log("Export | Response: " , response);
		} catch (Exception e) {
			LOGGER.log("Create | Exception: " , e.getMessage(), e);
		}
	}

	@RequestMapping(value = "/definitions/update/{id:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> updateDefinition(@PathVariable(value = "id") String id, @RequestBody String json,
			@RequestHeader(value = "user-id") String userId) {
		String apiId = "ekstep.definition.update";
		LOGGER.log("update Definition | Id: " + id + " | user-id: " + userId);
		try {
			Response response = taxonomyManager.updateDefinition(id, json);
			LOGGER.log("update Definition | Response: " , response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.log("update Definition | Exception: " , e.getMessage(), e);
			e.printStackTrace();
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	@RequestMapping(value = "/definitions/read/{id:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> findDefinition(@PathVariable(value = "id") String objectType,
			 @RequestParam(value = "graphId", required = true, defaultValue = "domain") String graphId,
			 @RequestHeader(value = "user-id") String userId) {
		String apiId = "ekstep.definition.read";
		LOGGER.log("Find Definition | Id: " + graphId + " | Object Type: " + objectType + " | user-id: " + userId);
		try {
			Response response = taxonomyManager.findDefinition(graphId, objectType);
			LOGGER.log("Find Definition | Response: " , response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.log("Find Definition | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	@RequestMapping(value = "/definitions/list", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> findAllDefinitions(
			@RequestParam(value = "graphId", required = true, defaultValue = "domain") String graphId,
			@RequestHeader(value = "user-id") String userId) {
		String apiId = "ekstep.definition.list";
		LOGGER.log("Find All Definitions | Id: " + graphId + " | user-id: " + userId);
		try {
			Response response = taxonomyManager.findAllDefinitions(graphId);
			LOGGER.log("Find All Definitions | Response: " , response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.log("Find All Definitions | Exception: " , e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
}
