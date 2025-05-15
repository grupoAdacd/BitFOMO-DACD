package es.ulpgc.dacd.bitfomo.binancefeeder.adapters;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public interface HttpClientProvider {
    HttpClient createClient();
    HttpRequest createRequest();
    HttpResponse<String> sendRequest() throws InterruptedException, IOException;
}