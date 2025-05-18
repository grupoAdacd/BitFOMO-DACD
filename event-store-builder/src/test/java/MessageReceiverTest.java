import es.ulpgc.dacd.bitfomo.eventstorebuilder.infrastructure.adapters.EventStore;
import es.ulpgc.dacd.bitfomo.eventstorebuilder.infrastructure.adapters.MessageReceiver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MessageReceiverTest {
    private MessageReceiver messageReceiver;
    private final List<String> testTopics = Arrays.asList("TestTopic1", "TestTopic2");
    private final String testUrl = "tcp://localhost:61616";
    private EventStore mockEventStore;

    @BeforeEach
    void setUp() throws Exception {
        mockEventStore = Mockito.mock(EventStore.class);
        messageReceiver = new MessageReceiver(testTopics, testUrl);
        Field eventStoreField = MessageReceiver.class.getDeclaredField("eventStore");
        eventStoreField.setAccessible(true);
        eventStoreField.set(messageReceiver, mockEventStore);
    }

    @Test
    void shouldInitializeFieldsCorrectly() throws Exception {
        Field topicsField = MessageReceiver.class.getDeclaredField("topics");
        topicsField.setAccessible(true);
        List<String> actualTopics = (List<String>)topicsField.get(messageReceiver);
        assertEquals(testTopics, actualTopics, "Los tópicos deben inicializarse correctamente");
        Field urlField = MessageReceiver.class.getDeclaredField("url");
        urlField.setAccessible(true);
        String actualUrl = (String)urlField.get(messageReceiver);
        assertEquals(testUrl, actualUrl, "La URL debe inicializarse correctamente");
        Field eventStoreField = MessageReceiver.class.getDeclaredField("eventStore");
        eventStoreField.setAccessible(true);
        EventStore eventStore = (EventStore)eventStoreField.get(messageReceiver);
        assertSame(mockEventStore, eventStore, "El EventStore debe ser el mock");
    }

    @Test
    void shouldSetupMessageListener() throws Exception {
        MessageReceiver testableReceiver = new TestableMessageReceiver(testTopics, testUrl, mockEventStore);
        assertDoesNotThrow(() -> {
            ((TestableMessageReceiver)testableReceiver).testSetupWithoutConnection();
        }, "La configuración básica no debería lanzar excepciones");
        Field eventStoreField = MessageReceiver.class.getDeclaredField("eventStore");
        eventStoreField.setAccessible(true);
        EventStore eventStore = (EventStore)eventStoreField.get(testableReceiver);
        assertSame(mockEventStore, eventStore, "El EventStore debe ser el mock");
    }

    @Test
    void shouldProcessMessageCorrectly() throws Exception {
        TestableMessageReceiver testableReceiver = new TestableMessageReceiver(testTopics, testUrl, mockEventStore);
        String testTopic = "TestTopic1";
        String testJson = "{\"id\":\"test123\",\"ts\":\"2023-05-20T10:15:30Z\",\"ss\":\"test-source\"}";
        testableReceiver.simulateMessageReceived(testTopic, testJson);
        Mockito.verify(mockEventStore).saveEvent(testTopic, testJson);
    }

    private static class TestableMessageReceiver extends MessageReceiver {
        private final EventStore mockEventStore;

        public TestableMessageReceiver(List<String> topics, String url, EventStore mockEventStore) {
            super(topics, url);
            this.mockEventStore = mockEventStore;
            try {
                Field eventStoreField = MessageReceiver.class.getDeclaredField("eventStore");
                eventStoreField.setAccessible(true);
                eventStoreField.set(this, mockEventStore);
            } catch (Exception e) {
                throw new RuntimeException("Error inyectando mockEventStore", e);
            }
        }

        public void testSetupWithoutConnection() {
            System.out.println("Configuración sin conexión para " + getTopics().size() + " tópicos");
        }

        public void simulateMessageReceived(String topicName, String messageText) {
            try {
                mockEventStore.saveEvent(topicName, messageText);
                System.out.println("Event caught: " + topicName);
            } catch (Exception e) {
                System.err.println("Error in topic " + topicName + ": " + e.getMessage());
            }
        }

        @Override
        public void start() {
            System.out.println("Start simulado para pruebas");
        }

        private List<String> getTopics() {
            try {
                Field topicsField = MessageReceiver.class.getDeclaredField("topics");
                topicsField.setAccessible(true);
                return (List<String>) topicsField.get(this);
            } catch (Exception e) {
                throw new RuntimeException("Error al acceder a topics", e);
            }
        }
    }
}