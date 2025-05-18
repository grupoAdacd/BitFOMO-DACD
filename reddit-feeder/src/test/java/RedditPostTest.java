import es.ulpgc.dacd.bitfomo.redditfeeder.domain.RedditPost;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class RedditPostTest {
    @Test
    void shouldCreateWithAllParameters() {
        String id = "123abc";
        Instant ts = Instant.now();
        String ss = "custom-source";
        String subreddit = "programming";
        String author = "user1";
        String title = "Test Title";
        String selftext = "Test Content";
        RedditPost post = new RedditPost(id, ts, ss, subreddit, author, title, selftext);
        assertEquals(id, post.id());
        assertEquals(ts, post.ts());
        assertEquals(ss, post.ss());
        assertEquals(subreddit, post.subreddit());
        assertEquals(author, post.author());
        assertEquals(title, post.title());
        assertEquals(selftext, post.selftext());
    }

    @Test
    void shouldUseDefaultSource() {
        String id = "123abc";
        Instant ts = Instant.now();
        String subreddit = "programming";
        String author = "user1";
        String title = "Test Title";
        String selftext = "Test Content";
        RedditPost post = new RedditPost(id, ts, subreddit, author, title, selftext);
        assertEquals(id, post.id());
        assertEquals(ts, post.ts());
        assertEquals("reddit-feeder", post.ss(), "Debe usar 'reddit-feeder' como fuente por defecto");
        assertEquals(subreddit, post.subreddit());
        assertEquals(author, post.author());
        assertEquals(title, post.title());
        assertEquals(selftext, post.selftext());
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        Instant now = Instant.now();
        RedditPost post1 = new RedditPost("123", now, "source", "sub", "author", "title", "text");
        RedditPost post2 = new RedditPost("123", now, "source", "sub", "author", "title", "text");
        RedditPost post3 = new RedditPost("456", now, "source", "sub", "author", "title", "text");
        assertEquals(post1, post2, "Posts con mismos valores deben ser iguales");
        assertNotEquals(post1, post3, "Posts con diferentes valores no deben ser iguales");
        assertEquals(post1.hashCode(), post2.hashCode(), "HashCode debe ser igual para posts iguales");
    }

    @Test
    void shouldImplementToString() {
        Instant now = Instant.parse("2023-05-20T10:15:30.00Z");
        RedditPost post = new RedditPost("123", now, "source", "sub", "author", "title", "text");
        String toString = post.toString();
        assertTrue(toString.contains("123"), "toString debe contener id");
        assertTrue(toString.contains("2023-05-20"), "toString debe contener parte de fecha");
        assertTrue(toString.contains("source"), "toString debe contener source");
        assertTrue(toString.contains("sub"), "toString debe contener subreddit");
        assertTrue(toString.contains("author"), "toString debe contener author");
        assertTrue(toString.contains("title"), "toString debe contener title");
        assertTrue(toString.contains("text"), "toString debe contener selftext");
    }
}