package org.ekstep.jobs.samza.util;

import com.google.gson.Gson;
import okhttp3.*;

import java.io.IOException;

public class DaggitServiceClient {

    private static JobLogger LOGGER = new JobLogger(DaggitServiceClient.class);
    private final String apiEndPoint;
    private final String experimentName;
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    public DaggitServiceClient(String apiEndPoint, String experimentName) {
        this.apiEndPoint = apiEndPoint;
        this.experimentName = experimentName;
    }

    public DaggitAPIResponse submit(String contentId)  throws IOException {
        // call daggit submit API
        String body = new Gson().toJson(new DaggitAPIRequest(contentId, experimentName).toMap());
        Request request = new Request.Builder()
                .url(apiEndPoint)
                .post(RequestBody.create(JSON_MEDIA_TYPE, body))
                .build();
        LOGGER.info("Request URI: " + request + "Request Body: " + body);
        Response response = new OkHttpClient().newCall(request).execute();
        String responseBody = response.body().string();
        LOGGER.info("Response Body: " + responseBody);
        return new Gson().fromJson(responseBody, DaggitAPIResponse.class);
    }
}
