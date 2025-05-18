import es.ulpgc.dacd.bitfomo.redditfeeder.infrastructure.adapters.MessageBrokerConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;

class MessageBrokerConnectionTest {
    private MessageBrokerConnection brokerConnection;
    private final String testBrokerUrl = "tcp://localhost:61616";

    @BeforeEach
    void setUp() {
        brokerConnection = new MessageBrokerConnection(testBrokerUrl);
    }

    @Test
    void shouldCreateConnectionFactory() throws Exception {
        Field factoryField = MessageBrokerConnection.class.getDeclaredField("connectionFactory");
        factoryField.setAccessible(true);
        Object factory = factoryField.get(brokerConnection);
        assertTrue(factory instanceof ActiveMQConnectionFactory);
        ActiveMQConnectionFactory activeMQFactory = (ActiveMQConnectionFactory) factory;
        assertEquals(testBrokerUrl, activeMQFactory.getBrokerURL());
    }

    @Test
    void shouldHandleNullSessionAndConnectionSafely() {
        assertDoesNotThrow(() -> brokerConnection.closeSession(null));
        assertDoesNotThrow(() -> brokerConnection.closeConnection(null));
    }
}