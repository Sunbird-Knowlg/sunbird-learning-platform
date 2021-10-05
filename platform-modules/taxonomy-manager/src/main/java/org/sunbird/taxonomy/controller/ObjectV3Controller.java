package org.sunbird.taxonomy.controller;


import org.sunbird.common.controller.BaseController;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.taxonomy.mgr.IObjectManager;
import org.sunbird.telemetry.logger.TelemetryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/object")
public class ObjectV3Controller extends BaseController {

    @Autowired
    IObjectManager manager;

    @RequestMapping(value = "/{objectType:.+}/v3/create", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> create(@PathVariable(value = "objectType") String objectType,
                                           @RequestBody Map<String, Object> requestMap) {

        String apiId = "ekstep.learning." + objectType +".create";
        TelemetryManager.log("Executing " + objectType + " Create API (Java Version) (API Version V3).", requestMap);
        Request request = getRequest(requestMap);
        try {
            Map<String, Object> map = (Map<String, Object>) request.get(objectType);
            Response response = manager.create(objectType, map);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            TelemetryManager.error("Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }

    }


    @RequestMapping(value = "/{objectType:.+}/v3/update/{id:.+}", method = RequestMethod.PATCH)
    @ResponseBody
    public ResponseEntity<Response> update(@PathVariable(value = "objectType") String objectType,
                                           @RequestBody Map<String, Object> requestMap, @PathVariable(value = "id") String id) {

        String apiId = "ekstep.learning." + objectType +".update";
        TelemetryManager.log("Executing " + objectType + " Update API (Java Version) (API Version V3).", requestMap);
        Request request = getRequest(requestMap);
        try {
            Map<String, Object> map = (Map<String, Object>) request.get(objectType);
            Response response = manager.update(objectType, id, map);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            TelemetryManager.error("Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }

    }


    @RequestMapping(value = "/{objectType:.+}/v3/read/{id:.+}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Response> read(@PathVariable(value = "objectType") String objectType, @PathVariable(value = "id") String id) {

        String apiId = "ekstep.learning." + objectType +".update";
        try {
            Response response = manager.read(objectType, id);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            TelemetryManager.error("Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }

    }

    @RequestMapping(value = "/{objectType:.+}/v3/delete/{id:.+}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Response> delete(@PathVariable(value = "objectType") String objectType, @PathVariable(value = "id") String id) {

        String apiId = "ekstep.learning." + objectType +".update";
        try {
            Response response = manager.delete(objectType, id);
            return getResponseEntity(response, apiId, null);
        } catch (Exception e) {
            TelemetryManager.error("Exception: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }

    }
}
