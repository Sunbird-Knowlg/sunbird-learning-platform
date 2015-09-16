import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RESTUtil {

    private static String host = "http://lp-sandbox.ekstep.org:8080/taxonomy-service";

    public static String post(String api, String request) {
        String result = "";
        try {
            URL url = getURL(api);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(2000);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("user-id", "ilimi");

            OutputStream os = conn.getOutputStream();
            os.write(request.getBytes());
            os.flush();

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                String line;
                StringBuilder sb = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                result = sb.toString();
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private static URL getURL(String api) throws Exception {
        return new URL(host + api);
    }
}