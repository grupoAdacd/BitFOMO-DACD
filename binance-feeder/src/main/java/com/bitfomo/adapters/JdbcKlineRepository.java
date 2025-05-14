package com.bitfomo.adapters;

import com.bitfomo.domain.Candlestick;
import com.bitfomo.domain.KlineRepositoryPort;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JdbcKlineRepository implements KlineRepositoryPort {
    private final String databaseUrl;
    private long lastKlineIntroduced;

    public JdbcKlineRepository(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    @Override
    public void save(Candlestick kline) {
        try (Connection conn = DriverManager.getConnection(databaseUrl)) {
            String sql = "INSERT INTO BinanceEvents (Kline_Open_Time, open_price, high_price, low_price, close_price, volume, Kline_Close_Time, quote_asset_volume, number_of_trades) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String klineOpenTime = sdf.format(new Date(kline.getKlineOpenTime()));
                String klineCloseTime = sdf.format(new Date(kline.getKlineCloseTime()));
                pstmt.setString(1, klineOpenTime);
                pstmt.setDouble(2, Double.parseDouble(kline.getOpenPrice()));
                pstmt.setDouble(3, Double.parseDouble(kline.getHighPrice()));
                pstmt.setDouble(4, Double.parseDouble(kline.getLowPrice()));
                pstmt.setDouble(5, Double.parseDouble(kline.getClosePrice()));
                pstmt.setDouble(6, Double.parseDouble(kline.getVolume()));
                pstmt.setString(7, klineCloseTime);
                pstmt.setDouble(8, Double.parseDouble(kline.getQuoteAssetVolume()));
                pstmt.setInt(9, kline.getNumberOfTrades());
                pstmt.executeUpdate();
                lastKlineIntroduced = kline.getKlineCloseTime();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public long getLastKline() {
        long lastTime = 0;
        try (Connection conn = DriverManager.getConnection(databaseUrl)) {
            String sql = "SELECT MAX(Kline_Close_Time) as last_time FROM BinanceEvents";
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getString("last_time") != null) {
                    String dateStr = rs.getString("last_time");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = sdf.parse(dateStr);
                    lastTime = date.getTime();
                }
            }
        } catch (Exception e) {
            System.err.println("Error retrieving last kline time: " + e.getMessage());
        }
        return lastTime;
    }

    public long getLastKlineIntroduced() {
        return lastKlineIntroduced;
    }
}