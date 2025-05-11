package com.bitfomo.adapters;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DATABASE_URL = "jdbc:sqlite:binance.db";

    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             Statement stmt = conn.createStatement()) {
            String sqlBinance = "CREATE TABLE IF NOT EXISTS BinanceEvents (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "Kline_Open_Time DATE," +
                    "open_price REAL," +
                    "high_price REAL," +
                    "low_price REAL," +
                    "close_price REAL," +
                    "volume REAL," +
                    "Kline_Close_Time DATE," +
                    "quote_asset_volume REAL," +
                    "number_of_trades INTEGER" +
                    ");";
            stmt.execute(sqlBinance);
            System.out.println("Database successfully initialized.");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public static String getDatabaseUrl() {
        return DATABASE_URL;
    }
}
