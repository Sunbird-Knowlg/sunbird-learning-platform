package com.ilimi.taxonomy.controller;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ilimi.common.controller.BaseController;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.taxonomy.mgr.IGameManager;

@Controller
@RequestMapping("/v1/game")
public class GameController extends BaseController {

    private static Logger LOGGER = LogManager.getLogger(GameController.class.getName());

    @Autowired
    private IGameManager gameManager;

    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> list(@RequestBody Map<String, Object> map) {
        String apiId = "game.list";
        Request request = getRequestObject(map);
        LOGGER.info("List Games | Request: " + request);
        try {
            Response response = gameManager.listGames(request);
            LOGGER.info("List Games | Response: " + response);
            return getResponseEntity(response, apiId, (null != request.getParams()) ? request.getParams().getMsgid() : null);
        } catch (Exception e) {
            LOGGER.error("List Games | Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, (null != request.getParams()) ? request.getParams().getMsgid() : null);
        }
    }

    @SuppressWarnings("unchecked")
    private Request getRequestObject(Map<String, Object> requestMap) {
        Request request = getRequest(requestMap);
        if (null != requestMap && !requestMap.isEmpty()) {
            Object requestObj = requestMap.get("request");
            if (null != requestObj) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    String strRequest = mapper.writeValueAsString(requestObj);
                    Map<String, Object> map = mapper.readValue(strRequest, Map.class);
                    request.setRequest(map);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return request;
    }
}
