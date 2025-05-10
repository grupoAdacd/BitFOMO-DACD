package com.bitfomo;


import com.bitfomo.adapters.broker.ActiveMQEventPublisher;
import com.bitfomo.adapters.broker.EventStore;
import com.bitfomo.adapters.client.ExchangeApiClient;
import com.bitfomo.application.usecase.GetLastKlineFromDB;
import com.bitfomo.domain.model.CandlestickData;
import com.bitfomo.adapters.persistence.manager.DatabaseManager;
import com.bitfomo.adapters.persistence.CandlestickDBPersistence;
import com.bitfomo.transformer.CandleStickSerializer;

import java.util.Date;
import java.util.ArrayList;

public class Application {
    public static void main(String[] args) {
        DatabaseManager.initializeDatabase();
        GetLastKlineFromDB getFromDB = new GetLastKlineFromDB();
        long lastKlineTime = getFromDB.getLastKline();
        System.out.println("Last Date inserted in DB: " + (lastKlineTime > 0 ? new Date(lastKlineTime) : "Theres no date registers"));
        ExchangeApiClient binanceApi = new ExchangeApiClient();
        if (lastKlineTime > 0) {
            binanceApi.setStartDateTime(lastKlineTime + 1);
        }
        CandlestickDBPersistence inserter = new CandlestickDBPersistence();
        CandleStickSerializer serializer = new CandleStickSerializer();
        ActiveMQEventPublisher publisher = new ActiveMQEventPublisher("tcp://localhost:61616", "CryptoPrice", serializer);
        for (ArrayList<CandlestickData> KlinesList: binanceApi.obtainFullResponse()) {
            System.out.println("Obtaining Arrays of Klines...");
            for (CandlestickData kline: KlinesList) {
                try {
                    inserter.insertEvent(kline);
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
        System.out.println("Binance Application running...");
    }
}