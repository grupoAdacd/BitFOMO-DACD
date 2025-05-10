package com.bitfomo.adapters;

import com.bitfomo.domain.CandlestickData;

import java.sql.Connection;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class CandlestickDBPersistence {
    private long lastKlineIntroduced;
    public void insertEvent(CandlestickData binanceKline) {
        try (Connection conn = DriverManager.getConnection(DatabaseManager.getDatabaseUrl())) {
            String sql = "INSERT INTO BinanceEvents (Kline_Open_Time, open_price, high_price, low_price, close_price, volume, Kline_Close_Time, quote_asset_volume, number_of_trades) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String KlineOpenTime = sdf.format(new Date(binanceKline.getKlineOpenTime()));
                String KlineCloseTime = sdf.format(new Date(binanceKline.getKlineCloseTime()));
                pstmt.setString(1, KlineOpenTime);
                pstmt.setDouble(2, Double.parseDouble(binanceKline.getOpenPrice()));
                pstmt.setDouble(3, Double.parseDouble(binanceKline.getHighPrice()));
                pstmt.setDouble(4, Double.parseDouble(binanceKline.getLowPrice()));
                pstmt.setDouble(5, Double.parseDouble(binanceKline.getClosePrice()));
                pstmt.setDouble(6, Double.parseDouble(binanceKline.getVolume()));
                pstmt.setString(7, KlineCloseTime);
                pstmt.setDouble(8, Double.parseDouble(binanceKline.getQuoteAssetVolume()));
                pstmt.setInt(9, binanceKline.getNumberOfTrades());
                pstmt.executeUpdate();
                setLastKlineIntroduced(binanceKline.getKlineCloseTime());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long getLastKlineIntroduced() {
        return lastKlineIntroduced;
    }

    public void setLastKlineIntroduced(long lastKlineIntroduced) {
        this.lastKlineIntroduced = lastKlineIntroduced;
    }
}
