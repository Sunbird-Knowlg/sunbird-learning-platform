package org.ekstep.language.controller;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.mgr.ICompositeSearchManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ilimi.common.controller.BaseController;
import com.ilimi.common.dto.Response;

/**
 * The Class LanguageDataSyncController provides operations that can sync data
 * between the Graph DB and ElasticSearch
 * 
 * @author Rayulu
 */
@Controller
@RequestMapping("v2/composite-search")
public class LanguageDataSyncController extends BaseController {

	/** The logger. */
	private static Logger LOGGER = LogManager.getLogger(LanguageDataSyncController.class.getName());

	/** The composite search manager. */
	@Autowired
	private ICompositeSearchManager compositeSearchManager;

	/**
	 * Sync all objects of a type from a given graph into ElasticSearch
	 *
	 * @param graphId
	 *            the graph id
	 * @param objectType
	 *            the object type
	 * @param start
	 *            the start
	 * @param total
	 *            the total
	 * @param map
	 *            the map
	 * @return the response entity
	 */
	@RequestMapping(value = "/sync/{id:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> sync(@PathVariable(value = "id") String graphId,
			@RequestParam(name = "objectType", required = false) String objectType,
			@RequestParam(name = "start", required = false) Integer start,
			@RequestParam(name = "total", required = false) Integer total, @RequestBody Map<String, Object> map) {
		String apiId = "composite-search.sync";
		LOGGER.info(apiId + " | Graph : " + graphId + " | ObjectType: " + objectType);
		try {
			Response response = compositeSearchManager.sync(graphId, objectType, start, total);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.error("Error: " + apiId, e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * Sync objects using the identifiers and graph into ElasticSearch.
	 *
	 * @param graphId
	 *            the graph id
	 * @param identifiers
	 *            the identifiers
	 * @param map
	 *            the map
	 * @return the response entity
	 */
	@RequestMapping(value = "/sync/object/{graphId:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> syncObject(@PathVariable(value = "graphId") String graphId,
			@RequestParam(value = "identifiers", required = true) String[] identifiers,
			@RequestBody Map<String, Object> map) {
		String apiId = "composite-search.sync-object";
		LOGGER.info(apiId + " | Graph : " + graphId + " | Identifier: " + identifiers);
		try {
			Response response = compositeSearchManager.syncObject(graphId, identifiers);
			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			LOGGER.error("Error: " + apiId, e);
			return getExceptionResponseEntity(e, apiId, null);
		}
	}
}
