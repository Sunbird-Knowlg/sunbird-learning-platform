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

public class DownloaderService extends CordovaPlugin {
    public static final String TAG = "Platform Service Plugin";
    /**
     * Constructor.
     */
    public DownloaderService() {
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
        Log.v(TAG, "Init DownloaderService");
    }
    public boolean execute(final String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Log.v(TAG, "DownloaderService received:" + action);
        if(action.equals("showToast")) {
            showToast(args.getString(0), Toast.LENGTH_SHORT);
            callbackContext.success(args.getString(0));
        } else if(action.equals("process")) {
            String message = "downloading and extracting zip file.";
            showToast(message, Toast.LENGTH_SHORT);
            callbackContext.success(new JSONObject().put("status", "ready").put("baseDir", args.getString(0)).put("error", ""));
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
}
