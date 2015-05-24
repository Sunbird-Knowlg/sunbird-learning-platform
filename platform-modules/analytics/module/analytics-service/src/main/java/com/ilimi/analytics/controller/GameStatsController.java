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

import com.ilimi.analytics.manager.IGameStatsManager;
import com.ilimi.common.dto.Response;

@Controller
@RequestMapping("/game")
public class GameStatsController extends BaseController {
	
	private static Logger LOGGER = LogManager.getLogger(GameStatsController.class.getName());
    
    @Autowired
    private IGameStatsManager gameStatsMgr;
    
    @RequestMapping(value = "/stats", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> search(@RequestBody Map<String, Object> map) {
        String apiId = "game.stats";
        Map<String, Object> searchParams = getSearchParams(map);
        LOGGER.info("Search UserStats | Search Params: " + searchParams);
        try {
            @SuppressWarnings("unchecked")
			Response response = gameStatsMgr.getGameStats((List<String>)searchParams.get("games"));
            LOGGER.info("Search UserStats | Response: " + response);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            LOGGER.error("List Games | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

}
