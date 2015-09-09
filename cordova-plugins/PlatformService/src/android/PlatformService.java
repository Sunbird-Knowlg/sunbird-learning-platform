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
        contentMap.put("stories", "{\"id\":\"ekstep.lp.game.list\",\"ver\":\"1.0\",\"ts\":\"2015-08-03T18:22:03ZZ\",\"params\":{\"resmsgid\":\"bac97dd6-1aeb-4b2c-8202-f8cb3f1b879b\",\"msgid\":null,\"err\":null,\"status\":\"successful\",\"errmsg\":null},\"result\":{\"games\":[{\"id\":\"story.annual.haircut.day\",\"name\":\"Annual Haircut Day\",\"description\":\"Sringeri Srinivas Annual Haircut Day\",\"appIcon\":\"assets/haircut.png\",\"launchPath\":\"stories/haircut_story\",\"loadingMessage\":\"Every great journey starts with a great story...\"}],\"ttl\":24}}");
        contentMap.put("worksheets", "{\"id\":\"ekstep.lp.game.list\",\"ver\":\"1.0\",\"ts\":\"2015-08-03T18:22:03ZZ\",\"params\":{\"resmsgid\":\"bac97dd6-1aeb-4b2c-8202-f8cb3f1b879b\",\"msgid\":null,\"err\":null,\"status\":\"successful\",\"errmsg\":null},\"result\":{\"games\":[{\"id\":\"worksheet.addition.through.objects\",\"name\":\"Addition through Objects\",\"description\":\"Addition through Objects\",\"appIcon\":\"assets/noah.png\",\"launchPath\":\"worksheets/addition_by_grouping\",\"loadingMessage\":\"Loading Addition through Objects assets...\"}],\"ttl\":24}}");
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
            String message = "Fetching content list from server...";
            // showToast(message, Toast.LENGTH_SHORT);
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
        JSONObject obj = new JSONObject(contentMap);
        return obj;
    }
}
