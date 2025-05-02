package com.bitfomo.common.http;

import com.bitfomo.common.api.ApiUrlBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class DefaultHttpService implements DefaultHttpServiceProvider{
    private String baseUrl;
    private String addition;

    public DefaultHttpService(String baseUrl, String addition) {
        this.baseUrl = baseUrl;
        this.addition = addition;
    }

    @Override
    public HttpClient createClient() {
        return HttpClient.newHttpClient();
    }

    @Override
    public HttpRequest createRequest() {
        ApiUrlBuilder apiUrlBuilder = new ApiUrlBuilder(baseUrl, addition);
        return HttpRequest.newBuilder().uri(URI.create(apiUrlBuilder.createApiUrl()))
                .timeout(Duration.ofSeconds(10)).build();
    }

    @Override
    public HttpResponse<String> sendRequest() {
        try {
            return createClient().send(createRequest(), HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
