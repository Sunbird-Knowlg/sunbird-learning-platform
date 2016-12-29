package org.ekstep.language.controller;

import java.util.Map;

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

import com.ilimi.common.controller.BaseController;
import com.ilimi.common.dto.Response;

/**
 * The Class SearchController, is entry point for search operation
 *
 * @author karthik
 */
@Controller
@RequestMapping("v3/search")
public class SearchControllerV3 extends BaseController {

	/** The search controller. */
	@Autowired
	private SearchControllerV2 searchController;

	/**
	 * List the words based on input query.
	 *
	 * @param languageId            the language id
	 * @param map            the map
	 * @param userId            the user id
	 * @return the response entity
	 */
	@RequestMapping(value = "/find/", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> search(@RequestParam(name = "languageId", required = true) String languageId,
			@RequestBody Map<String, Object> map, @RequestHeader(value = "user-id") String userId) {
		return searchController.search(languageId, map, userId, API_VERSION_3);
	}
}
