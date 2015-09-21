import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class RESTUtil {

    private static String host = "http://lp-sandbox.ekstep.org:8080/taxonomy-service";

    public static Map<String, Object> post(String api, String request) {
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            URL url = getURL(api);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("user-id", "ilimi");

            OutputStream os = conn.getOutputStream();
            os.write(request.getBytes());
            os.flush();
            result.put("code", conn.getResponseCode());
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                String line;
                StringBuilder sb = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                result.put("status", "success");
                result.put("msg", "all ok");
                result.put("data", sb.toString());
            } else if(conn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                result.put("status", "error");
                result.put("msg", "HTTP_NOT_FOUND");
                result.put("errorCode", "NO_CONNECTION_ERROR");
            }
            return result;
        } catch (UnknownHostException e) {
            result.put("status", "error");
            result.put("msg", "UnknownHostException");
            result.put("errorCode", "NO_CONNECTION_ERROR");
        }
        catch (Exception e) {
            result.put("status", "error");
            result.put("msg", "Exception");
            result.put("errorCode", "INTERNAL_ERROR");
        }
        return result;
    }

    private static URL getURL(String api) throws Exception {
        return new URL(host + api);
    }
}