package es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure;

import es.ulpgc.dacd.bitfomo.binancefeeder.domain.Candlestick;
import es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure.adapters.BinanceHttpClient;
import es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure.ports.CandlestickFetcher;
import es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure.ports.CandlestickParser;
import es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure.ports.EndpointProvider;
import es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure.ports.HttpClientProvider;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class BinanceCandlestickFetcher implements CandlestickFetcher {
    private final HttpClientProvider httpClient;
    private final EndpointProvider endpointProvider;
    private final CandlestickParser parser;
    private final LastKlineTimeManager timeManager;
    private final long intervalMillis;
    private long startDateTime;
    private long endDateTime;

    public BinanceCandlestickFetcher(HttpClientProvider httpClient, EndpointProvider endpointProvider,
                                     CandlestickParser parser, LastKlineTimeManager timeManager,
                                     long intervalMillis, long defaultInitialRangeMillis) {
        this.httpClient = httpClient;
        this.endpointProvider = endpointProvider;
        this.parser = parser;
        this.timeManager = timeManager;
        this.intervalMillis = intervalMillis;
        this.startDateTime = 0;
        this.endDateTime = 0;
    }

    @Override
    public List<List<Candlestick>> fetchCandlesticks(long startTime, long endTime) {
        long currentTime = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
        currentTime = (currentTime / (60 * 1000L)) * (60 * 1000L);
        this.endDateTime = (endTime == 0) ? currentTime : endTime;

        long lastKlineTime = timeManager.loadLastKlineTime();
        if (lastKlineTime == -1) {
            this.startDateTime = endDateTime - 3600000;
        } else {
            this.startDateTime = lastKlineTime + 1;
        }

        if (startTime != 0) {
            this.startDateTime = startTime;
        }

        if (this.startDateTime >= this.endDateTime) {
            this.startDateTime = this.endDateTime - 3600000;
        }

        List<List<Candlestick>> fullResponse = new ArrayList<>();
        fetchCandlesticksIteratively(fullResponse);
        return fullResponse;
    }

    public long getInitialStartDateTime() {
        if (startDateTime == 0) {
            long currentTime = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
            currentTime = (currentTime / (60 * 1000L)) * (60 * 1000L);
            long lastKlineTime = timeManager.loadLastKlineTime();
            if (lastKlineTime == -1) {
                return currentTime - 3600000;
            } else {
                return lastKlineTime + 1;
            }
        }
        return startDateTime;
    }

    private void fetchCandlesticksIteratively(List<List<Candlestick>> fullResponse) {
        long currentStartTime = startDateTime;
        while (currentStartTime < endDateTime) {
            List<Candlestick> candlesticks = fetchSingleBatch(currentStartTime, endDateTime);
            if (!candlesticks.isEmpty()) {
                fullResponse.add(candlesticks);
                updateStartDateTime(candlesticks);
                currentStartTime = startDateTime;
            } else {
                break;
            }
        }
    }

    private List<Candlestick> fetchSingleBatch(long startTime, long endTime) {
        if (httpClient instanceof BinanceHttpClient) {
            ((BinanceHttpClient) httpClient).setTimeRange(startTime, endTime);
        }
        String json = fetchJsonData();
        if (json == null || json.isEmpty()) return new ArrayList<>();
        List<Candlestick> candlesticks = parser.parseCandlesticks(json);
        return candlesticks.isEmpty() ? new ArrayList<>() : candlesticks;
    }

    private String fetchJsonData() {
        try {
            return httpClient.sendRequest().body();
        } catch (Exception e) {
            return null;
        }
    }

    private void updateStartDateTime(List<Candlestick> candlesticks) {
        if (!candlesticks.isEmpty()) {
            Candlestick lastCandlestick = candlesticks.get(candlesticks.size() - 1);
            startDateTime = lastCandlestick.klineCloseTime() + 1;
            timeManager.saveLastKlineTime(lastCandlestick.klineCloseTime());
        }
    }
}