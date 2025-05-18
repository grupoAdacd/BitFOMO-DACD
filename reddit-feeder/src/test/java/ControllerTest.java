import es.ulpgc.dacd.bitfomo.redditfeeder.Controller;
import es.ulpgc.dacd.bitfomo.redditfeeder.domain.RedditPost;
import es.ulpgc.dacd.bitfomo.redditfeeder.infrastructure.ports.EventPublisherPort;
import es.ulpgc.dacd.bitfomo.redditfeeder.infrastructure.ports.ExternalRedditApiPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ControllerTest {
    @Mock
    private ExternalRedditApiPort mockRedditApi;
    @Mock
    private EventPublisherPort mockPublisher;
    private Controller controller;
    private List<String> testSubreddits;
    private int testLimit;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testSubreddits = Arrays.asList("programming", "java");
        testLimit = 5;
        controller = new Controller(mockRedditApi, mockPublisher, testSubreddits, testLimit);
    }

    @Test
    void shouldScheduleTaskOnExecute() throws Exception {
        ScheduledExecutorService scheduler = getSchedulerField();
        ScheduledExecutorService mockScheduler = mock(ScheduledExecutorService.class);
        setSchedulerField(mockScheduler);
        controller.execute();
        verify(mockScheduler).scheduleAtFixedRate(any(Runnable.class), eq(0L), eq(5L), eq(TimeUnit.MINUTES));
        setSchedulerField(scheduler);
    }

    @Test
    void shouldFetchAndPublishPosts() throws Exception {
        List<RedditPost> testPosts = Arrays.asList(
                new RedditPost("id1", Instant.now(), "programming", "user1", "title1", "content1"),
                new RedditPost("id2", Instant.now(), "java", "user2", "title2", "content2")
        );
        when(mockRedditApi.fetchLatestPosts(testSubreddits, testLimit)).thenReturn(testPosts);
        invokePrivateMethod(controller, "fetchPosts");
        verify(mockRedditApi).fetchLatestPosts(testSubreddits, testLimit);
        for (RedditPost post : testPosts) {
            verify(mockPublisher).publish(post);
        }
    }

    @Test
    void shouldHandleExceptionInFetchPosts() throws Exception {
        when(mockRedditApi.fetchLatestPosts(any(), anyInt()))
                .thenThrow(new RuntimeException("Test exception"));
        assertDoesNotThrow(() -> {
            invokePrivateMethod(controller, "fetchPosts");
        });
        verify(mockPublisher, never()).publish(any());
    }

    @Test
    void shouldHandleExceptionInPublishPosts() throws Exception {
        List<RedditPost> testPosts = Arrays.asList(
                new RedditPost("id1", Instant.now(), "programming", "user1", "title1", "content1"),
                new RedditPost("id2", Instant.now(), "java", "user2", "title2", "content2")
        );
        doThrow(new RuntimeException("Test exception")).when(mockPublisher).publish(testPosts.get(1));
        invokePrivateMethod(controller, "publishPosts", new Class<?>[]{List.class}, testPosts);
        verify(mockPublisher).publish(testPosts.get(0));
        verify(mockPublisher).publish(testPosts.get(1));
    }

    private ScheduledExecutorService getSchedulerField() throws Exception {
        java.lang.reflect.Field field = Controller.class.getDeclaredField("scheduler");
        field.setAccessible(true);
        return (ScheduledExecutorService) field.get(controller);
    }

    private void setSchedulerField(ScheduledExecutorService scheduler) throws Exception {
        java.lang.reflect.Field field = Controller.class.getDeclaredField("scheduler");
        field.setAccessible(true);
        field.set(controller, scheduler);
    }

    private void invokePrivateMethod(Object object, String methodName) throws Exception {
        java.lang.reflect.Method method = object.getClass().getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(object);
    }

    private void invokePrivateMethod(Object object, String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        java.lang.reflect.Method method = object.getClass().getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        method.invoke(object, args);
    }
}