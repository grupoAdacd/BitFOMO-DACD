package com.bitfomo.adapters.persistence;

import com.bitfomo.domain.model.RedditPost;
import com.bitfomo.domain.port.out.PostRepositoryPort;

import java.sql.*;
import java.util.List;

public class JdbcPostRepository implements PostRepositoryPort {

    private static final String DDL = """
        CREATE TABLE IF NOT EXISTS reddit_post (
          id         TEXT PRIMARY KEY,
          timestamp  INTEGER NOT NULL,
          subreddit  TEXT    NOT NULL,
          author     TEXT    NOT NULL,
          title      TEXT    NOT NULL,
          body       TEXT,
          sentiment REAL
        );
        """;

    private static final String DML = """
        INSERT OR IGNORE INTO reddit_post
          (id, timestamp, subreddit, author, title, body, sentiment)
        VALUES (?, ?, ?, ?, ?, ?, ?);
        """;

    private final String jdbcUrl;

    public JdbcPostRepository(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
        initDatabase();
    }

    private void initDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            try (var conn = DriverManager.getConnection(jdbcUrl);
                 var stmt = conn.createStatement()) {
                stmt.execute(DDL);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver SQLite no encontrado", e);
        } catch (SQLException e) {
            throw new RuntimeException("Error inicializando la base de datos", e);
        }
    }


    @Override
    public void saveAll(List<RedditPost> posts) {
        try (var conn = DriverManager.getConnection(jdbcUrl);
             var ps   = conn.prepareStatement(DML)) {

            conn.setAutoCommit(false);
            for (var post : posts) {
                ps.setString(1, post.id());
                ps.setLong(2, post.timestamp().getEpochSecond());
                ps.setString(3, post.subreddit());
                ps.setString(4, post.author());
                ps.setString(5, post.title());
                ps.setString(6, post.body());
                if (post.sentimentScore() != null) {
                    ps.setDouble(7, post.sentimentScore());
                } else {
                    ps.setNull(7, Types.REAL);
                }
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error guardando RedditPost en BD", e);
        }
    }
}
