package com.bitfomo.application.adapters;

import com.bitfomo.domain.RedditPost;
import com.bitfomo.domain.PostRepositoryPort;

import java.sql.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class JdbcPostRepository implements PostRepositoryPort {

    private static final String DDL = """
        CREATE TABLE IF NOT EXISTS reddit_post (
          id         TEXT PRIMARY KEY,
          timestamp  TEXT    NOT NULL,
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

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("MM/dd/yyyy hh:mm:ss a")
            .withZone(ZoneId.systemDefault());

    private final String jdbcUrl;

    public JdbcPostRepository(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
        initDatabase();
    }

    private void initDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection(jdbcUrl);
                 Statement stmt = conn.createStatement()) {
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
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps   = conn.prepareStatement(DML)) {

            conn.setAutoCommit(false);
            for (RedditPost post : posts) {
                ps.setString(1, post.id());
                String formattedTs = FORMATTER.format(post.timestamp());
                ps.setString(2, formattedTs);
                ps.setString(3, post.subreddit());
                ps.setString(4, post.author());
                ps.setString(5, post.title());
                ps.setString(6, post.selftext());
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
