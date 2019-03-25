package org.ekstep.jobs.samza.util;

import com.google.gson.Gson;
import okhttp3.*;

import java.io.IOException;

public class DaggitServiceClient {

    private final String apiEndPoint;
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    public DaggitServiceClient(String apiEndPoint) {
        this.apiEndPoint = apiEndPoint;
    }

    public DaggitAPIResponse submit(String contentId)  throws IOException {
        // call daggit submit API
        String body = new Gson().toJson(new DaggitAPIRequest(contentId).toMap());
        Request request = new Request.Builder()
                .url(apiEndPoint)
                .post(RequestBody.create(JSON_MEDIA_TYPE, body))
                .build();
        Response response = new OkHttpClient().newCall(request).execute();
        String responseBody = response.body().string();
        return new Gson().fromJson(responseBody, DaggitAPIResponse.class);
    }
}
