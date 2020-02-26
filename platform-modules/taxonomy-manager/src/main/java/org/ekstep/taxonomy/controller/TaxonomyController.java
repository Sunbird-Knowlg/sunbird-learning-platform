package org.ekstep.taxonomy.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.ekstep.common.Platform;
import org.ekstep.common.controller.BaseController;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.graph.common.enums.GraphEngineParams;
import org.ekstep.graph.dac.model.SearchCriteria;
import org.ekstep.graph.enums.ImportType;
import org.ekstep.graph.importer.OutputStreamValue;
import org.ekstep.taxonomy.enums.TaxonomyAPIParams;
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
@RequestMapping("/taxonomy")
public class TaxonomyController extends BaseController {

	@Autowired
	private ITaxonomyManager taxonomyManager;

	@PostConstruct
	public void postConstruct() {
		if (Platform.config.hasPath("platform.services")) {
			List<String> services = Platform.config.getStringList("platform.services");
			for (String service : services) {
				String key = service + ".graph_ids";
				if (Platform.config.hasPath(key)) {
					TelemetryManager.info("Loading definition nodes to in-memory cache for service: " + service);
					List<String> graphIds = Platform.config.getStringList(key);
					for (String graphId : graphIds) {
						TelemetryManager.info("Loading definition nodes to in-memory cache for graph: " + graphId);
						taxonomyManager.findAllDefinitions(graphId);
					}
				} else {
					TelemetryManager.warn("config is not available to load definitions for service: " + service);
				}
			}
			TelemetryManager.info("Loading definition nodes to in-memory cache is complete.");
		}
	}

	@RequestMapping(value = "/{graphId:.+}/{objectType:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> findAllByObjectType(@PathVariable(value = "graphId") String graphId,
			@PathVariable(value = "objectType") String objectType, @RequestHeader(value = "user-id") String userId) {
		String apiId = "ekstep.taxonomy.objecttype.list";
		try {
			Response response = taxonomyManager.findAllByObjectType(graphId, objectType);
			TelemetryManager.log("FindAll | Response: ", response.getResult());
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("FindAll | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	@RequestMapping(value = "/{id:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> create(@PathVariable(value = "id") String id,
			@RequestParam("file") MultipartFile file, @RequestHeader(value = "user-id") String userId,
			HttpServletResponse resp) {
		String apiId = "ekstep.taxonomy.import";
		TelemetryManager.log("Create | Id: " + id + " | File: " + file + " | user-id: " + userId);
		InputStream stream = null;
		try {
			if (null != file)
				stream = file.getInputStream();
			Response response = taxonomyManager.create(id, stream);
			TelemetryManager.log("Create | Response: ", response.getResult());
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Create | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		} finally {
			if (null != stream)
				try {
					stream.close();
				} catch (IOException e) {
					TelemetryManager.error("Error1 While Closing the Stream: " + e.getMessage(), e);
				}
		}
	}

	@RequestMapping(value = "/{id:.+}/export", method = RequestMethod.POST)
	@ResponseBody
	public void export(@PathVariable(value = "id") String id, @RequestBody Map<String, Object> map,
			@RequestHeader(value = "user-id") String userId, HttpServletResponse resp) {
		String format = ImportType.CSV.name();
		String apiId = "ekstep.taxonomy.export";
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
			TelemetryManager.log("Export | Response: ", response.getResult());
		} catch (Exception e) {
			TelemetryManager.error("Create | Exception: " + e.getMessage(), e);
		}
	}

	@RequestMapping(value = "/{id:.+}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<Response> delete(@PathVariable(value = "id") String id,
			@RequestHeader(value = "user-id") String userId) {
		TelemetryManager.log("Delete | Id: " + id + " | user-id: " + userId);
		String apiId = "ekstep.taxonomy.delete";
		try {
			Response response = taxonomyManager.delete(id);
			TelemetryManager.log("Delete | Response: ", response.getResult());
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Delete | Exception: " + e.getMessage(), e);
			e.printStackTrace();
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	@RequestMapping(value = "/{id:.+}/definition", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> createDefinition(@PathVariable(value = "id") String id, @RequestBody String json,
			@RequestHeader(value = "user-id") String userId) {
		String apiId = "ekstep.definition.create";
		TelemetryManager.log("Create Definition | Id: " + id + " | user-id: " + userId);
		try {
			Response response = taxonomyManager.updateDefinition(id, json);
			TelemetryManager.log("Create Definition | Response: ", response.getResult());
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Create Definition | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	@RequestMapping(value = "/{id:.+}/definition/{defId:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> findDefinition(@PathVariable(value = "id") String id,
			@PathVariable(value = "defId") String objectType, @RequestHeader(value = "user-id") String userId) {
		String apiId = "ekstep.definition.find";
		TelemetryManager.log("Find Definition | Id: " + id + " | Object Type: " + objectType + " | user-id: " + userId);
		try {
			Response response = taxonomyManager.findDefinition(id, objectType);
			TelemetryManager.log("Find Definition | Response: ", response.getResult());
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Find Definition | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	@RequestMapping(value = "/{id:.+}/definition", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> findAllDefinitions(@PathVariable(value = "id") String id,
			@RequestHeader(value = "user-id") String userId) {
		String apiId = "ekstep.definition.list";
		TelemetryManager.log("Find All Definitions | Id: " + id + " | user-id: " + userId);
		try {
			Response response = taxonomyManager.findAllDefinitions(id);
			TelemetryManager.log("Find All Definitions | Response: ", response.getResult());
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Find All Definitions | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	@RequestMapping(value = "/{id:.+}/definition/{defId:.+}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<Response> deleteDefinition(@PathVariable(value = "id") String id,
			@PathVariable(value = "defId") String objectType, @RequestHeader(value = "user-id") String userId) {
		String apiId = "ekstep.definition.delete";
		TelemetryManager
				.log("Delete Definition | Id: " + id + " | Object Type: " + objectType + " | user-id: " + userId);
		try {
			Response response = taxonomyManager.deleteDefinition(id, objectType);
			TelemetryManager.log("Delete Definition | Response: " + response);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			TelemetryManager.error("Delete Definition | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/{id:.+}/index", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> createIndex(@PathVariable(value = "id") String id,
			@RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
		String apiId = "ekstep.index.create";
		Request request = getRequest(map);
		TelemetryManager.log("Create Index | Id: " + id + " | Request: " + request + " | user-id: " + userId);
		try {
			List<String> keys = (List<String>) request.get(TaxonomyAPIParams.property_keys.name());
			Boolean unique = (Boolean) request.get(TaxonomyAPIParams.unique_constraint.name());
			Response response = taxonomyManager.createIndex(id, keys, unique);
			TelemetryManager.log("Create Index | Response: ", response.getResult());
			return getResponseEntity(response, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		} catch (Exception e) {
			TelemetryManager.error("Create Index | Exception: " + e.getMessage(), e);
			return getExceptionResponseEntity(e, apiId,
					(null != request.getParams()) ? request.getParams().getMsgid() : null);
		}
	}

}
