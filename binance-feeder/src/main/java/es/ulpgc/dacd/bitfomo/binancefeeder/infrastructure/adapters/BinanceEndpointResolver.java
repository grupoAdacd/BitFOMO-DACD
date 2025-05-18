package es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure.adapters;

import es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure.ports.EndpointProvider;

public class BinanceEndpointResolver implements EndpointProvider {
    private static final String BASE_URL = "https://api.binance.com/api/v3/uiKlines?symbol=";
    private final String symbol;
    private final String interval;
    private final int limit;
    private long startTime;
    private long endTime;

    public BinanceEndpointResolver(String symbol, String interval, int limit) {
        this.symbol = symbol;
        this.interval = interval;
        this.limit = limit;
        this.startTime = 0;
        this.endTime = 0;
    }

    public void setTimeRange(long startTime, long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public String createApiUrl() {
        String url = BASE_URL + symbol + "&interval=" + interval + "&limit=" + limit +
                "&startTime=" + startTime + "&endTime=" + endTime;
        return url;
    }
}