package es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure.adapters;

import es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure.ports.HttpClientProvider;
import es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure.ports.EndpointProvider;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

public class BinanceHttpClient implements HttpClientProvider {
    private final EndpointProvider endpointProvider;
    private final java.net.http.HttpClient client;
    private long startTime;
    private long endTime;

    public BinanceHttpClient(EndpointProvider endpointProvider) {
        this.endpointProvider = endpointProvider;
        this.client = java.net.http.HttpClient.newHttpClient();
        this.startTime = 0;
        this.endTime = 0;
    }

    public void setTimeRange(long startTime, long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
        if (endpointProvider instanceof BinanceEndpointResolver) {
            ((BinanceEndpointResolver) endpointProvider).setTimeRange(startTime, endTime);
        }
    }

    @Override
    public java.net.http.HttpClient createClient() {
        return client;
    }

    @Override
    public HttpRequest createRequest() {
        String url = endpointProvider.createApiUrl();
        return HttpRequest.newBuilder().uri(URI.create(url)).build();
    }

    @Override
    public HttpResponse<String> sendRequest() {
        try {
            return client.send(createRequest(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException("Error enviando solicitud HTTP: " + e.getMessage());
        }
    }
}