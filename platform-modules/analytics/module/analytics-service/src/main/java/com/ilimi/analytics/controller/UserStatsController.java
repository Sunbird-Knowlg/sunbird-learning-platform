package com.ilimi.analytics.controller;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ilimi.analytics.manager.IUserStatsManager;
import com.ilimi.common.dto.Response;

@Controller
@RequestMapping("/user")
public class UserStatsController extends BaseController {
	
	private static Logger LOGGER = LogManager.getLogger(UserStatsController.class.getName());
    
    @Autowired
    private IUserStatsManager userStatsMgr;
    
    @RequestMapping(value = "/stats", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> search(@RequestBody Map<String, Object> map) {
        String apiId = "userStats.search";
        Map<String, Object> searchParams = getSearchParams(map);
        LOGGER.info("Search GameStats | Search Params: " + searchParams);
        try {
            @SuppressWarnings("unchecked")
			Response response = userStatsMgr.searchUserStats((List<String>)searchParams.get("uids"));
            LOGGER.info("Search GameStats | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("List GameStats | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

}
