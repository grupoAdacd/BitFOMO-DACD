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
        // Calculamos el tiempo actual en UTC y lo alineamos al minuto más reciente
        long currentTime = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
        currentTime = (currentTime / (60 * 1000L)) * (60 * 1000L); // Alineamos al minuto completo

        // Calculamos endDateTime como el minuto actual
        this.endDateTime = (endTime == 0) ? currentTime : endTime;

        // Calculamos startDateTime como 5 minutos antes de endDateTime
        if (startTime == 0) {
            long lastKlineTime = timeManager.loadLastKlineTime();
            if (lastKlineTime == -1) {
                startDateTime = endDateTime - (5 * 60 * 1000L); // 5 minutos atrás
            } else {
                startDateTime = lastKlineTime + 1;
            }
        } else {
            startDateTime = startTime;
        }

        // Aseguramos que startDateTime sea menor que endDateTime
        if (startDateTime >= endDateTime) {
            startDateTime = endDateTime - (5 * 60 * 1000L); // Retrocedemos 5 minutos
        }

        List<List<Candlestick>> fullResponse = new ArrayList<>();
        int maxIterations = calculateMaxIterations();
        fetchCandlesticksIteratively(fullResponse, maxIterations);
        return fullResponse;
    }

    public long getInitialStartDateTime() {
        if (startDateTime == 0) {
            long currentTime = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
            currentTime = (currentTime / (60 * 1000L)) * (60 * 1000L); // Alineamos al minuto completo
            long lastKlineTime = timeManager.loadLastKlineTime();
            if (lastKlineTime == -1) {
                return currentTime - (5 * 60 * 1000L); // 5 minutos atrás
            } else {
                return lastKlineTime + 1;
            }
        }
        return startDateTime;
    }

    private int calculateMaxIterations() {
        return (int) ((endDateTime - startDateTime) / intervalMillis);
    }

    private void fetchCandlesticksIteratively(List<List<Candlestick>> fullResponse, int maxIterations) {
        for (int i = 0; i < maxIterations; i++) {
            List<Candlestick> candlesticks = fetchSingleBatch();
            if (candlesticks.isEmpty()) {
                // Si no hay datos, retrocedemos startDateTime y persistimos el cambio
                startDateTime -= (5 * 60 * 1000L); // Retrocedemos 5 minutos
                if (startDateTime < 0) break; // Evitamos timestamps negativos
                timeManager.saveLastKlineTime(startDateTime - 1); // Persistimos el nuevo startDateTime
                continue;
            }
            fullResponse.add(candlesticks);
            updateStartDateTime(candlesticks);
            break; // Salimos después de encontrar el primer lote válido
        }
    }

    private List<Candlestick> fetchSingleBatch() {
        if (httpClient instanceof BinanceHttpClient) {
            ((BinanceHttpClient) httpClient).setTimeRange(startDateTime, endDateTime);
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