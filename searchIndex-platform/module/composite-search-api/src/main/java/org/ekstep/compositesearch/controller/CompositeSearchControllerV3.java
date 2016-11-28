package org.ekstep.compositesearch.controller;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ilimi.common.dto.Response;

/**
 * The Class CompositeSearchControllerV3, entry point for v3 composite search
 *
 * @author karthik
 */
@Controller
@RequestMapping("v3/public/search")
public class CompositeSearchControllerV3 extends BaseCompositeSearchController {

	/** The composite search controller. */
	@Autowired
	private CompositeSearchController compositeSearchController;

	/**
	 * Search.
	 *
	 * @param map
	 *            the map
	 * @param userId
	 *            the user id
	 * @param resp
	 *            the resp
	 * @return the response entity
	 */
	@RequestMapping(value = "", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> search(@RequestBody Map<String, Object> map,
			@RequestHeader(value = "user-id") String userId, HttpServletResponse resp) {
		return compositeSearchController.search(map, userId, resp);
	}

	/**
	 * Count.
	 *
	 * @param map
	 *            the map
	 * @param userId
	 *            the user id
	 * @param resp
	 *            the resp
	 * @return the response entity
	 */
	@RequestMapping(value = "/count", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> count(@RequestBody Map<String, Object> map,
			@RequestHeader(value = "user-id") String userId, HttpServletResponse resp) {
		return compositeSearchController.count(map, userId, resp);
	}

}
