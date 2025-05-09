package com.bitfomo.adapters.persistence;

import com.bitfomo.adapters.JdbcPostRepository;
import com.bitfomo.domain.RedditPost;
import com.bitfomo.domain.PostRepositoryPort;
import org.junit.jupiter.api.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JdbcPostRepositoryTest {

    private static String jdbcUrl;
    private PostRepositoryPort repository;

    @BeforeAll
    static void setupAll() throws Exception {
        Path tempFile = Files.createTempFile("test-reddit", ".db");
        jdbcUrl = "jdbc:sqlite:" + tempFile.toAbsolutePath();
    }

    @BeforeEach
    void setUp() {
        repository = new JdbcPostRepository(jdbcUrl);
    }

    @Test
    @DisplayName("saveAll debe insertar múltiples posts sin error y sin duplicados")
    void saveAllInsertsPostsAndIgnoresDuplicates() throws Exception {
        RedditPost p1 = new RedditPost("id1", Instant.now(), "TestSub", "alice", "Hello", "Body", null);
        RedditPost p2 = new RedditPost("id2", Instant.now(), "TestSub", "bob",   "Hi",    "Body", 0.5);
        List<RedditPost> posts = List.of(p1, p2, p1);  // p1 repetido para probar IGNORE

        repository.saveAll(posts);

        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             Statement  stmt = conn.createStatement();
             ResultSet  rs   = stmt.executeQuery("SELECT id, author, sentiment FROM reddit_post")) {

            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
                String id = rs.getString("id");
                if (id.equals("id1")) {
                    assertEquals("alice", rs.getString("author"));
                    assertTrue(rs.getObject("sentiment") == null);
                } else if (id.equals("id2")) {
                    assertEquals("bob", rs.getString("author"));
                    assertEquals(0.5, rs.getDouble("sentiment"));
                } else {
                    fail("Encontrado un id inesperado: " + id);
                }
            }
            assertEquals(2, rowCount);
        }
    }
}
