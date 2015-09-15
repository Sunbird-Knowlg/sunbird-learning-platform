import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import android.util.Log;
import android.provider.Settings;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class PlatformService extends CordovaPlugin {
    public static final String TAG = "Platform Service Plugin";
    Map<String, String> contentMap;
    /**
     * Constructor.
     */
    public PlatformService() {
        contentMap = new HashMap<String, String>();
    }
        /**
         * Sets the context of the Command. This can then be used to do things like
         * get file paths associated with the Activity.
         *
         * @param cordova The context of the main Activity.
         * @param webView The CordovaWebView Cordova is running in.
         */
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Log.v(TAG, "Init PlatformService");
    }
    public boolean execute(final String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Log.v(TAG, "PlatformService received:" + action);
        if(action.equals("showToast")) {
            showToast(args.getString(0), Toast.LENGTH_SHORT);
            callbackContext.success(args.getString(0));
        } else if(action.equals("getContentList")) {
            JSONObject contentList = getContentList(args);
            callbackContext.success(contentList);
        }
        return true;
    }

    private void showToast(final String message, final int duration) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                Toast toast = Toast.makeText(cordova.getActivity().getApplicationContext(), message, duration);
                toast.show();
            }
        });
    }

    private JSONObject getContentList(JSONArray types) {
        JSONObject obj = new JSONObject();
        try {
            Map<String, String> result = new HashMap<String, String>();
            if(types != null) {
                for(int i=0;i<types.length();i++) {
                    String type = types.getString(i);
                    result.put(type, getContent(type));
                }
            }
            obj = new JSONObject(result);
            return obj;
        } catch(Exception e) {

        }
        return obj;
    }

    private String getContent(String type) {
        String data = contentMap.get(type);
        if(data == null || data == "") {
            data = RESTUtil.post("/v1/content/list?type="+type, "{  \"request\": {}}");
            contentMap.put(type, data);
        }
        return data;
    }
}
