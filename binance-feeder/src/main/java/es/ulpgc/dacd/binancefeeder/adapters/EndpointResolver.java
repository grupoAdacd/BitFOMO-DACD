package es.ulpgc.dacd.binancefeeder.adapters;

import es.ulpgc.dacd.binancefeeder.domain.EndpointProvider;

public class EndpointResolver implements EndpointProvider {
    public final String baseUrl;
    public final String specify;

    public EndpointResolver(String BaseUrl, String Specify) {
        this.baseUrl = BaseUrl;
        this.specify = Specify;
    }
    @Override
    public String createApiUrl() {
        return this.baseUrl + this.specify;
    }
}
