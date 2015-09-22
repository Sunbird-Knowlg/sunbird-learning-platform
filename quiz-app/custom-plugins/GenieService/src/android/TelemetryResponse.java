import android.text.TextUtils;

import org.apache.cordova.CallbackContext;

import org.ekstep.genieservices.sdks.response.GenieResponse;
import org.ekstep.genieservices.sdks.response.IResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class TelemetryResponse implements IResponseHandler {
    private CallbackContext callbackContext;

    public TelemetryResponse(CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
    }

    @Override
    public void onSuccess(Object o) {
        GenieResponse response = (GenieResponse) o;
        System.out.println("TelemetryResponse success: " + response.getStatus());
        Map<String, String> map = new HashMap<String, String>();
        map.put("status", "success");
        callbackContext.success(new JSONObject(map));
    }

    @Override
    public void onFailure(Object o) {
        GenieResponse response = (GenieResponse) o;
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
