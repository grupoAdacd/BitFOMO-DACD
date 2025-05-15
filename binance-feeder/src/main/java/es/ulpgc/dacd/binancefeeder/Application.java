package es.ulpgc.dacd.binancefeeder;

import es.ulpgc.dacd.binancefeeder.adapters.ActiveMQEventPublisher;
import es.ulpgc.dacd.binancefeeder.adapters.ExchangeApiClient;
import es.ulpgc.dacd.binancefeeder.adapters.DatabaseManager;
import es.ulpgc.dacd.binancefeeder.adapters.JdbcKlineRepository;
import es.ulpgc.dacd.binancefeeder.domain.Candlestick;
import es.ulpgc.dacd.binancefeeder.transformer.CandlestickSerializer;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Application {
    private static final int INITIAL_DELAY_SECONDS = 0;
    private static final int PERIOD_MINUTES = 60;

    public static void main(String[] args) {
        DatabaseManager.initializeDatabase();
        JdbcKlineRepository klineRepository = new JdbcKlineRepository(DatabaseManager.getDatabaseUrl());
        ExchangeApiClient binanceApi = new ExchangeApiClient();
        CandlestickSerializer serializer = new CandlestickSerializer();
        ActiveMQEventPublisher publisher = new ActiveMQEventPublisher("tcp://localhost:61616", "CryptoPrice", serializer);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("Running scheduled fetch at: " + System.currentTimeMillis());
                long lastKlineTime = klineRepository.getLastKline();
                System.out.println("Last Date inserted in DB: " + (lastKlineTime > 0 ? new Date(lastKlineTime) : "Theres no date registers"));
                if (lastKlineTime > 0) {
                    binanceApi.setStartDateTime(lastKlineTime + 1);
                }
                for (ArrayList<Candlestick> KlinesList : binanceApi.obtainFullResponse()) {
                    System.out.println("Obtaining Arrays of Klines...");
                    for (Candlestick kline : KlinesList) {
                        try {
                            klineRepository.save(kline);
                            System.out.println("Inserting data in database..." +
                                    new Date(kline.getKlineOpenTime()) + " - " +
                                    new Date(kline.getKlineCloseTime()));
                            publisher.publish(kline);
                            System.out.println("Publishing events...");
                        } catch (Exception e) {
                            System.err.println("Error Publishing/Saving events..." + e);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error in scheduled task: " + e.getMessage());
                e.printStackTrace();
            }
        }, INITIAL_DELAY_SECONDS, PERIOD_MINUTES, TimeUnit.MINUTES);
        try {
            System.out.println("Binance Application running...");
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            System.out.println("Shutting down...");
            scheduler.shutdown();
            System.exit(0);
        }
    }
}