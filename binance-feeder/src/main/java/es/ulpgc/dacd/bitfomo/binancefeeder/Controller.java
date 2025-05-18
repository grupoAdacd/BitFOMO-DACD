package es.ulpgc.dacd.bitfomo.binancefeeder;

import es.ulpgc.dacd.bitfomo.binancefeeder.domain.Candlestick;
import es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure.BinanceCandlestickFetcher;
import es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure.ports.CandlestickFetcher;
import es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure.ports.EventPublisherPort;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Controller {
    private final CandlestickFetcher fetcher;
    private final EventPublisherPort publisher;
    private final long fetchPeriodSeconds;
    private long lastFetchTime;

    public Controller(CandlestickFetcher fetcher, EventPublisherPort publisher, long fetchPeriodSeconds) {
        this.fetcher = fetcher;
        this.publisher = publisher;
        this.fetchPeriodSeconds = fetchPeriodSeconds;
        this.lastFetchTime = fetcher instanceof BinanceCandlestickFetcher ?
                ((BinanceCandlestickFetcher) fetcher).getInitialStartDateTime() :
                LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public void start() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduleFetchTask(scheduler);
        keepProgramRunning(scheduler);
    }

    private void scheduleFetchTask(ScheduledExecutorService scheduler) {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("Ejecutando fetch programado a las: " + Instant.now());
                fetchAndPublishCandlesticks();
            } catch (Exception e) {
                System.err.println("Error en tarea programada: " + e.getMessage());
            }
        }, 0, fetchPeriodSeconds, TimeUnit.SECONDS);
    }

    private void fetchAndPublishCandlesticks() {
        long currentTime = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
        List<List<Candlestick>> candlestickBatches = fetcher.fetchCandlesticks(lastFetchTime, currentTime);
        for (List<Candlestick> batch : candlestickBatches) {
            System.out.println("Obteniendo lote de candlesticks...");
            publishCandlesticks(batch);
        }
        lastFetchTime = currentTime;
    }

    private void publishCandlesticks(List<Candlestick> batch) {
        for (Candlestick candlestick : batch) {
            try {
                publisher.publish(candlestick);
                System.out.println("Publicando evento...");
            } catch (Exception e) {
                System.err.println("Error publicando evento: " + e.getMessage());
            }
        }
    }

    private void keepProgramRunning(ScheduledExecutorService scheduler) {
        try {
            System.out.println("Binance Feeder ejecutándose...");
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            System.out.println("Apagando...");
            scheduler.shutdown();
            Thread.currentThread().interrupt();
        }
    }
}