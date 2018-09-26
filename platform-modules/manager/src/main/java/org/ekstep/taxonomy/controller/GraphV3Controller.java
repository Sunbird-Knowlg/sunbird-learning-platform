package org.ekstep.taxonomy.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.ekstep.common.controller.BaseController;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.graph.common.enums.GraphEngineParams;
import org.ekstep.graph.dac.model.SearchCriteria;
import org.ekstep.graph.enums.ImportType;
import org.ekstep.graph.importer.OutputStreamValue;
import org.ekstep.taxonomy.enums.TaxonomyAPIParams;
import org.ekstep.taxonomy.mgr.IContentManager;
import org.ekstep.taxonomy.mgr.ITaxonomyManager;
import org.ekstep.telemetry.logger.TelemetryManager;
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
@RequestMapping("/system/v3")
public class GraphV3Controller extends BaseController {

	@Autowired
	private ITaxonomyManager taxonomyManager;
	
	@Autowired
	private IContentManager contentManager;

	@RequestMapping(value = "/import/{id:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> create(@PathVariable(value = "id") String id,
			@RequestParam("file") MultipartFile file, @RequestHeader(value = "user-id") String userId,
			HttpServletResponse resp) {
		String apiId = "ekstep.graph.import";
		TelemetryManager.log("Create | Id: " + id + " | File: " + file + " | user-id: " + userId);
		InputStream stream = null;
		try {
			if (null != file)
				stream = file.getInputStream();
			Response response = taxonomyManager.create(id, stream);
			TelemetryManager.log("Create | Response: " , response.getResult());
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Create | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		} finally {
			if (null != stream)
				try {
					stream.close();
				} catch (IOException e) {
					TelemetryManager.error("Error1 While Closing the Stream:" + e.getMessage(), e);
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
		TelemetryManager.log("Export | Id: " + id + " | Format: " + format + " | user-id: " + userId);
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
			TelemetryManager.log("Export | Response: " , response.getResult());
		} catch (Exception e) {
			TelemetryManager.error("Create | Exception: " + e.getMessage(), e);
		}
	}

	@RequestMapping(value = "/definitions/update/{id:.+}", method = RequestMethod.PATCH)
	@ResponseBody
	public ResponseEntity<Response> updateDefinition(@PathVariable(value = "id") String id, @RequestBody String json,
			@RequestHeader(value = "user-id") String userId) {
		String apiId = "ekstep.definition.update";
		TelemetryManager.log("update Definition | Id: " + id + " | user-id: " + userId);
		try {
			Response response = taxonomyManager.updateDefinition(id, json);
			TelemetryManager.log("update Definition | Response: " , response.getResult());
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("update Definition | Exception: " + e.getMessage(), e);
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
		TelemetryManager.log("Find Definition | Id: " + graphId + " | Object Type: " + objectType + " | user-id: " + userId);
		try {
			Response response = taxonomyManager.findDefinition(graphId, objectType);
			TelemetryManager.log("Find Definition | Response: " , response.getResult());
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Find Definition | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	@RequestMapping(value = "/definitions/list", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> findAllDefinitions(
			@RequestParam(value = "graphId", required = true, defaultValue = "domain") String graphId,
			@RequestHeader(value = "user-id") String userId) {
		String apiId = "ekstep.definition.list";
		TelemetryManager.log("Find All Definitions | Id: " + graphId + " | user-id: " + userId);
		try {
			Response response = taxonomyManager.findAllDefinitions(graphId);
			TelemetryManager.log("Find All Definitions | Response: " , response.getResult());
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Find All Definitions | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/content/update/{id:.+}", method = RequestMethod.PATCH)
	@ResponseBody
	public ResponseEntity<Response> internalObjectUpdate(@PathVariable(value = "id") String contentId,
			@RequestBody Map<String, Object> requestMap) {
		String apiId = "ekstep.learning.system.content.update";
		Request request = getRequest(requestMap);
		try {
			Map<String, Object> map = (Map<String, Object>) request.get("content");
			Response response = contentManager.updateAllContents(contentId, map);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
}
