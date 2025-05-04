package com.bitfomo.common.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DATABASE_URL = "jdbc:sqlite:src/main/database/events.db";

    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             Statement stmt = conn.createStatement()) {

            String sqlReddit = "CREATE TABLE IF NOT EXISTS RedditEvents (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "subreddit TEXT NOT NULL," +
                    "author TEXT NOT NULL," +
                    "title TEXT NOT NULL," +
                    "post_text TEXT," +
                    "post_link TEXT," +
                    "number_of_comments INTEGER," +
                    "number_of_subscribers INTEGER," +
                    "sentiment TEXT," +
                    "created_date DATE" +
                    ");";

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

            stmt.execute(sqlReddit);
            stmt.execute(sqlBinance);

            System.out.println("Base de datos y tablas inicializadas correctamente.");

        } catch (SQLException e) {
            System.err.println("Error al inicializar la base de datos: " + e.getMessage());
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
