package com.bitfomo;


import com.bitfomo.adapters.client.ExchangeApiClient;
import com.bitfomo.domain.model.CandlestickData;
import com.bitfomo.adapters.persistence.manager.DataStoreManager;
import com.bitfomo.adapters.persistence.manager.MarketDataWriter;

import java.sql.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class Application {
    public static void main(String[] args) {
        DataStoreManager.initializeDatabase();

        long lastKlineTime = getLastKlineTimeFromDatabase();
        System.out.println("Last Date inserted in DB: " + (lastKlineTime > 0 ? new Date(lastKlineTime) : "Theres no date registers"));
        ExchangeApiClient binanceApi = new ExchangeApiClient();
        if (lastKlineTime > 0) {
            binanceApi.setStartDateTime(lastKlineTime + 1);
        }
        MarketDataWriter inserter = new MarketDataWriter();
        for (ArrayList<CandlestickData> KlinesList: binanceApi.obtainFullResponse()) {
            System.out.println("Obtaining Arrays of Klines...");
            for (CandlestickData kline: KlinesList) {
                try {
                    inserter.insertEvent(kline);
                    System.out.println("Inserting events on Data Base... " +
                            new Date(kline.getKlineOpenTime()) + " - " +
                            new Date(kline.getKlineCloseTime()));
                } catch (Exception e) {
                    System.err.println("Error inserting..." + e);
                }
            }
        }
        System.out.println("Binance Data Collection Running...");
    }

    private static long getLastKlineTimeFromDatabase() {
        long lastTime = 0;
        try (Connection conn = DriverManager.getConnection(DataStoreManager.getDatabaseUrl());
             Statement stmt = conn.createStatement()) {
            String sql = "SELECT MAX(Kline_Close_Time) as last_time FROM BinanceEvents";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next() && rs.getString("last_time") != null) {
                String dateStr = rs.getString("last_time");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date date = sdf.parse(dateStr);
                lastTime = date.getTime();
            }

        } catch (Exception e) {
            System.err.println("Error when trynna grab LastKlineTime parameter: " + e.getMessage());
        }
        return lastTime;
    }
}