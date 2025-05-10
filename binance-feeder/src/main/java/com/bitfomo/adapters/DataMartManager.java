package com.bitfomo.adapters;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DataMartManager {

    private static final String DATABASE_URL = "jdbc:sqlite:src/main/data_mart/mart.db";

    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             Statement stmt = conn.createStatement()) {
            String financialTable = "CREATE TABLE IF NOT EXISTS financialRawData (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "date DATE," +
                    "response TEXT" +
                    ");";
            String postTable = "CREATE TABLE IF NOT EXISTS postRawData (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "date DATE," +
                    "response TEXT" +
                    ");";
            String mergedDataTable = "CREATE TABLE IF NOT EXISTS MergedData (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "Kline_Open_Time DATE," +
                    "open_price REAL," +
                    "high_price REAL," +
                    "low_price REAL," +
                    "close_price REAL," +
                    "volume REAL," +
                    "Kline_Close_Time DATE," +
                    "quote_asset_volume REAL," +
                    "number_of_trades INTEGER," +
                    "post_id TEXT PRIMARY KEY,"+
                    "timestamp TEXT NOT NULL,"+
                    "subreddit TEXT NOT NULL,"+
                    "author TEXT NOT NULL,"+
                    "title TEXT NOT NULL,"+
                    "body TEXT," +
                    "sentiment REAL," +
                    ");";
            stmt.execute(financialTable);
            stmt.execute(postTable);
            stmt.execute(mergedDataTable);
            System.out.println("Database and tables successfully initialized.");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public static String getDatabaseUrl() {
        return DATABASE_URL;
    }

    public static void main(String[] args) {
        initializeDatabase();
    }
}
