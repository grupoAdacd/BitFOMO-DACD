package com.bitfomo.adapters;



import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HttpClient implements HttpClientProvider {
    private String baseUrl;
    private String addition;

    public HttpClient(String baseUrl, String addition) {
        this.baseUrl = baseUrl;
        this.addition = addition;
    }

    @Override
    public java.net.http.HttpClient createClient() {
        return java.net.http.HttpClient.newHttpClient();
    }

    @Override
    public HttpRequest createRequest() {
        EndpointResolver apiUrlBuilder = new EndpointResolver(baseUrl, addition);
        return HttpRequest.newBuilder()
                .uri(URI.create(apiUrlBuilder.createApiUrl())).timeout(Duration.ofSeconds(10)).build();
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
