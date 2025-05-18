import es.ulpgc.dacd.bitfomo.eventstorebuilder.infrastructure.Deduplicator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DeduplicatorTest {
    private static class TestableDeduplicator extends Deduplicator {
        private final Set<String> testPostIds = new HashSet<>();

        public TestableDeduplicator() {
        }
        public void addTestPostIds(String... ids) {
            for (String id : ids) {
                testPostIds.add(id);
            }
        }
        @Override
        public boolean isDuplicateRedditPost(String postId) {
            return testPostIds.contains(postId);
        }

        @Override
        public void addRedditPostId(String postId) {
            testPostIds.add(postId);
        }
    }

    private TestableDeduplicator deduplicator;

    @BeforeEach
    void setUp() {
        deduplicator = new TestableDeduplicator();
        deduplicator.addTestPostIds("test123", "test456");
    }

    @Test
    void shouldDetectDuplicatePost() {
        assertTrue(deduplicator.isDuplicateRedditPost("test123"), "Debería detectar el post existente como duplicado");
        assertTrue(deduplicator.isDuplicateRedditPost("test456"), "Debería detectar el post existente como duplicado");
        assertFalse(deduplicator.isDuplicateRedditPost("newpost789"), "No debería detectar un post nuevo como duplicado");
    }

    @Test
    void shouldAddNewPostId() {
        String newPostId = "newpost789";
        assertFalse(deduplicator.isDuplicateRedditPost(newPostId), "Inicialmente no es duplicado");
        deduplicator.addRedditPostId(newPostId);
        assertTrue(deduplicator.isDuplicateRedditPost(newPostId), "Después de añadirlo debe ser detectado como duplicado");
    }

    @Test
    void shouldLoadIdsFromSetDirectly() {
        TestableDeduplicator testDeduplicator = new TestableDeduplicator();
        assertFalse(testDeduplicator.isDuplicateRedditPost("anyId"), "Set vacío inicialmente");
        testDeduplicator.addTestPostIds("id1", "id2");
        assertTrue(testDeduplicator.isDuplicateRedditPost("id1"), "Debe contener id1 después de añadirlo");
        assertTrue(testDeduplicator.isDuplicateRedditPost("id2"), "Debe contener id2 después de añadirlo");
    }
}