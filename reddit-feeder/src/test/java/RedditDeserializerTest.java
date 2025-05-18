import es.ulpgc.dacd.bitfomo.redditfeeder.domain.RedditPost;
import es.ulpgc.dacd.bitfomo.redditfeeder.infrastructure.adapters.RedditDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class RedditDeserializerTest {
    private RedditDeserializer deserializer;
    @BeforeEach
    void setUp() {
        deserializer = new RedditDeserializer();
    }

    @Test
    void shouldParseValidJsonData() {
        String jsonData = "{\n" +
                "  \"data\": {\n" +
                "    \"children\": [\n" +
                "      {\n" +
                "        \"data\": {\n" +
                "          \"id\": \"test123\",\n" +
                "          \"created_utc\": 1621500000,\n" +
                "          \"subreddit\": \"programming\",\n" +
                "          \"author\": \"testuser\",\n" +
                "          \"title\": \"Test Title\",\n" +
                "          \"selftext\": \"Test Content\"\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"data\": {\n" +
                "          \"id\": \"test456\",\n" +
                "          \"created_utc\": 1621600000,\n" +
                "          \"subreddit\": \"java\",\n" +
                "          \"author\": \"javauser\",\n" +
                "          \"title\": \"Java Title\"\n" +
                "        }\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}";
        List<RedditPost> posts = deserializer.parsePosts(jsonData);
        assertEquals(2, posts.size());
        RedditPost post1 = posts.get(0);
        assertEquals("test123", post1.id());
        assertEquals(Instant.ofEpochSecond(1621500000), post1.ts());
        assertEquals("programming", post1.subreddit());
        assertEquals("testuser", post1.author());
        assertEquals("Test Title", post1.title());
        assertEquals("Test Content", post1.selftext());
        RedditPost post2 = posts.get(1);
        assertEquals("test456", post2.id());
        assertEquals(Instant.ofEpochSecond(1621600000), post2.ts());
        assertEquals("java", post2.subreddit());
        assertEquals("javauser", post2.author());
        assertEquals("Java Title", post2.title());
        assertEquals("", post2.selftext());
    }

    @Test
    void shouldHandleEmptyChildren() {
        String jsonData = "{\n" +
                "  \"data\": {\n" +
                "    \"children\": []\n" +
                "  }\n" +
                "}";
        List<RedditPost> posts = deserializer.parsePosts(jsonData);
        assertTrue(posts.isEmpty());
    }

    @Test
    void shouldThrowExceptionForInvalidJson() {
        String invalidJson = "{ invalid json }";
        assertThrows(org.json.JSONException.class, () -> {
            deserializer.parsePosts(invalidJson);
        });
    }
}