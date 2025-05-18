import es.ulpgc.dacd.bitfomo.redditfeeder.domain.RedditPost;
import es.ulpgc.dacd.bitfomo.redditfeeder.infrastructure.adapters.RedditApi;
import es.ulpgc.dacd.bitfomo.redditfeeder.infrastructure.adapters.RedditConnection;
import es.ulpgc.dacd.bitfomo.redditfeeder.infrastructure.adapters.RedditDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedditApiTest {
    @Mock
    private RedditConnection mockConnection;
    @Mock
    private RedditDeserializer mockDeserializer;
    private RedditApi redditApi;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        redditApi = new RedditApi(mockConnection, mockDeserializer);
    }

    @Test
    void shouldFetchPostsFromMultipleSubreddits() {
        List<String> subreddits = Arrays.asList("programming", "java");
        int limit = 10;
        String jsonData1 = "{\"programming\":\"data\"}";
        String jsonData2 = "{\"java\":\"data\"}";
        List<RedditPost> posts1 = Collections.singletonList(
                new RedditPost("id1", Instant.now(), "programming", "user1", "title1", "content1"));
        List<RedditPost> posts2 = Collections.singletonList(
                new RedditPost("id2", Instant.now(), "java", "user2", "title2", "content2"));
        when(mockConnection.fetchJsonData("programming", limit)).thenReturn(jsonData1);
        when(mockConnection.fetchJsonData("java", limit)).thenReturn(jsonData2);
        when(mockDeserializer.parsePosts(jsonData1)).thenReturn(posts1);
        when(mockDeserializer.parsePosts(jsonData2)).thenReturn(posts2);
        List<RedditPost> result = redditApi.fetchLatestPosts(subreddits, limit);
        assertEquals(2, result.size());
        assertTrue(result.containsAll(posts1));
        assertTrue(result.containsAll(posts2));
        verify(mockConnection).fetchJsonData("programming", limit);
        verify(mockConnection).fetchJsonData("java", limit);
        verify(mockDeserializer).parsePosts(jsonData1);
        verify(mockDeserializer).parsePosts(jsonData2);
    }

    @Test
    void shouldReturnEmptyListForEmptySubreddits() {
        List<String> subreddits = Collections.emptyList();
        int limit = 10;
        List<RedditPost> result = redditApi.fetchLatestPosts(subreddits, limit);
        assertTrue(result.isEmpty());
        verify(mockConnection, never()).fetchJsonData(anyString(), anyInt());
        verify(mockDeserializer, never()).parsePosts(anyString());
    }

    @Test
    void shouldPropagateExceptionFromConnection() {
        List<String> subreddits = Collections.singletonList("programming");
        int limit = 10;
        when(mockConnection.fetchJsonData("programming", limit))
                .thenThrow(new RuntimeException("Test exception"));
        Exception exception = assertThrows(RuntimeException.class, () -> {
            redditApi.fetchLatestPosts(subreddits, limit);
        });
        assertTrue(exception.getMessage().contains("Test exception"));
    }
}