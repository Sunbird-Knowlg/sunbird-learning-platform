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

// TODO: Auto-generated Javadoc
/**
 * The Class MetricsControllerV3, entry point for v3 metric API
 *
 * @author karthik
 */
@Controller
@RequestMapping("v3/public/metrics")
public class MetricsControllerV3 extends BaseCompositeSearchController {

	/** The metrics controller. */
	@Autowired
	private MetricsController metricsController;

	/**
	 * Metrics.
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
	public ResponseEntity<Response> metrics(@RequestBody Map<String, Object> map,
			@RequestHeader(value = "user-id") String userId, HttpServletResponse resp) {
		return metricsController.metrics(map, userId, resp);
	}
}
