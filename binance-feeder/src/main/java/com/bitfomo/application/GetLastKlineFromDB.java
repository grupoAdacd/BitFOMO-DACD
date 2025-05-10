package com.bitfomo.application;

import com.bitfomo.adapters.DatabaseManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GetLastKlineFromDB {
    public long getLastKline() {
        long lastTime = 0;
        try (Connection conn = DriverManager.getConnection(DatabaseManager.getDatabaseUrl());
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
