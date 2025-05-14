package com.bitfomo;


import com.bitfomo.adapters.ActiveMQEventPublisher;
import com.bitfomo.adapters.ExchangeApiClient;
import com.bitfomo.application.GetLastKlineFromDB;
import com.bitfomo.domain.CandlestickData;
import com.bitfomo.adapters.DatabaseManager;
import com.bitfomo.adapters.CandlestickDBPersistence;
import com.bitfomo.transformer.CandlestickSerializer;

import java.util.Date;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Application {
    private static final int DELAY_SECONDS = 0;
    private static final int PERIOD_MINUTES = 5;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java -jar binance-feeder.jar <brokerUrl> <queueName>");
            System.exit(1);
        }
        String brokerUrl = args[0];
        String topic   = args[1];

        DatabaseManager.initializeDatabase();
        GetLastKlineFromDB getFromDB = new GetLastKlineFromDB();
        long lastKlineTime = getFromDB.getLastKline();
        System.out.println("Last Date inserted in DB: " + (lastKlineTime > 0 ? new Date(lastKlineTime) : "Theres no date registers"));
        ExchangeApiClient binanceApi = new ExchangeApiClient();
        if (lastKlineTime > 0) {
            binanceApi.setStartDateTime(lastKlineTime + 1);
        }
        CandlestickDBPersistence inserter = new CandlestickDBPersistence();
        CandlestickSerializer serializer = new CandlestickSerializer();
        ActiveMQEventPublisher publisher = new ActiveMQEventPublisher(brokerUrl, topic, serializer);
        for (ArrayList<CandlestickData> KlinesList: binanceApi.obtainFullResponse()) {
            System.out.println("Obtaining Arrays of Klines...");
            for (CandlestickData kline: KlinesList) {
                ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
                scheduler.scheduleAtFixedRate(() -> {
                    try {
                        inserter.insertEvent(kline);
                        System.out.println("Inserting data in database..." +
                                new Date(kline.getKlineOpenTime()) + " - " +
                                new Date(kline.getKlineCloseTime()));
                        publisher.publish(kline);
                        System.out.println("Publishing events...");
                    } catch (Exception e) {
                        System.err.println("Error Publishing events..." + e);
                    }
                }, DELAY_SECONDS, PERIOD_MINUTES, TimeUnit.MINUTES);
            }
        }
        System.out.println("Binance Application running...");
    }
}