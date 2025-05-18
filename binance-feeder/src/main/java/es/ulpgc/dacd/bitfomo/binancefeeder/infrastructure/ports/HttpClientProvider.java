package es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure.ports;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public interface HttpClientProvider {
    java.net.http.HttpClient createClient();
    HttpRequest createRequest();
    HttpResponse<String> sendRequest();
}