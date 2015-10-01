import android.text.TextUtils;

import org.apache.cordova.CallbackContext;

import org.ekstep.genieservices.sdks.response.GenieResponse;
import org.ekstep.genieservices.sdks.response.IResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class UserProfileResponse implements IResponseHandler {
    private CallbackContext callbackContext;

    public UserProfileResponse(CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
    }

    public void onSuccess(GenieResponse response) {
        // GenieResponse response = (GenieResponse) o;
        System.out.println("UserProfileResponse success: " + response.getStatus());
        System.out.println("UserProfileResponse result: " + response.getResult());
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("status", "success");
        Map<String, Object> resultObj = (Map<String, Object>) response.getResult();
        JSONObject result = new JSONObject(resultObj);
        map.put("data", result);
        callbackContext.success(new JSONObject(map));
    }

    public void onFailure(GenieResponse response) {
        // GenieResponse response = (GenieResponse) o;
        System.out.println("TelemetryResponse error: " + response.getStatus() + " -- " + response.getError());
        List<String> errors = response.getErrorMessages();
        String error = response.getError();
        String errorString = TextUtils.join(",", errors);
        Map<String, String> map = new HashMap<String, String>();
        map.put("status", response.getStatus());
        map.put("error", error);
        map.put("errors", errorString);
        callbackContext.error(new JSONObject(map));
    }
}
