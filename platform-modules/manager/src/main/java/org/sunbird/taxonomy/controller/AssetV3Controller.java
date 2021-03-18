package org.sunbird.taxonomy.controller;

import org.sunbird.common.controller.BaseController;
import org.sunbird.common.dto.Request;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ResponseCode;
import org.sunbird.taxonomy.enums.AssetParams;
import org.sunbird.taxonomy.mgr.IAssetManager;
import org.sunbird.telemetry.logger.TelemetryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/asset/v3")
public class AssetV3Controller extends BaseController {

    @Autowired
    private IAssetManager assetManager;

    private Map<String, Object> getAsset(Request request) throws Exception {
        Map<String, Object> requestMap = null;
        requestMap = request.getRequest();
        if(!requestMap.isEmpty()) {
            Map<String, Object> asset = (Map<String, Object>) requestMap.get(AssetParams.asset.name());
            if(null != asset && !asset.isEmpty()) return asset;
            else throw new ClientException(ResponseCode.CLIENT_ERROR.name(), "Invalid Asset");
        } else throw new ClientException(ResponseCode.CLIENT_ERROR.name(), "InvalidRequest.");
    }

    @RequestMapping(value = "/validate", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> licenseValidate(@RequestBody Map<String, Object> requestMap,
    		@RequestParam(value = "field", required = true) String field) {
        String apiId = "asset.url.validate";
        TelemetryManager.log("Validating URL against : " + field);
        Response response;
        try {
            Request request = getRequest(requestMap);
            response = assetManager.urlValidate(getAsset(request), field);
            return getResponseEntity(response, apiId, null);
        } catch(Exception e) {
            TelemetryManager.error("Exception occured while Licesence Validation: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }

    /**
     * This method returns the metadata of the Url.
     *
     * @param requestMap Request containing the url in metadata.
     *
     * @return ResponseEntity containing the metadata for the Url.
     */
    @RequestMapping(value = "/metadata/read", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Response> metadataRead(@RequestBody Map<String, Object> requestMap) {
        String apiId = "ekstep.asset.meatadata.read";
        TelemetryManager.log("Reading Url Metadata");
        Response response;
        try {
            Request request = getRequest(requestMap);
            response = assetManager.metadataRead(getAsset(request));
            return getResponseEntity(response, apiId, null);
        } catch(Exception e) {
            TelemetryManager.error("Exception occured while reading Url Metadata: " + e.getMessage(), e);
            return getExceptionResponseEntity(e, apiId, null);
        }
    }
}
