import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class IntentService extends CordovaPlugin {

	public static final String TAG = "Intent Service Plugin";

	public IntentService() {

    }

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Log.v(TAG, "Init IntentService");
    }

    public boolean execute(final String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Log.v(TAG, "PlatformService received:" + action);
        if(action.equals("sendResult")) {
        	if (args.length() != 2) {
                callbackContext.error("Invalid arguments to sendResult method");
            	return false;
			}
            sendResult(args.getString(0), args.getString(1), callbackContext);
        }
        return true;
    }

    private void sendResult(String param, String value, CallbackContext callbackContext) {
    	CordovaActivity activity = (CordovaActivity) this.cordova.getActivity();
    	if (null != activity) {
    		try {
    			Intent resultIntent = new Intent(Intent.ACTION_MAIN); 
    			resultIntent.putExtra(param, value); 
    			activity.setResult(Activity.RESULT_OK, resultIntent); 
    			activity.finish();
    			callbackContext.success("success");
    		} catch (Exception e) {
    			callbackContext.error("Error: " + e.getMessage());
    		}
    	} else {
			callbackContext.error("activity not found");
    	}
    	
    }

}