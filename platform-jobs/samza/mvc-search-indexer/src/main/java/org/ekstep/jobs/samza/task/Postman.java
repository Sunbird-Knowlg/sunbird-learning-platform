package org.ekstep.jobs.samza.task;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Postman {
    private static HttpClient client;

    public static HttpClient getHttpClient() {
        if (client == null) {
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(300 * 1000).setSocketTimeout(300 * 1000).build();
            client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
        }
        return client;
    }

    public static String getContent(String api , String identifier)throws Exception {
        HttpGet get = new HttpGet(api+identifier);
        String strResponse = null;
        // add request header
        get.setHeader("Content-Type", "application/json; charset=utf-8");
        HttpResponse response = getHttpClient().execute(get);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        String line;

        while ((line = reader.readLine()) != null) {
            strResponse = line;
        }
        reader.close();

        if(strResponse==null || strResponse.isEmpty())
            strResponse = response.getStatusLine().getReasonPhrase();
        return strResponse;
    }
}
