package es.ulpgc.dacd.bitfomo.redditfeeder.infrastructure.adapters;

import es.ulpgc.dacd.bitfomo.redditfeeder.domain.RedditPost;
import es.ulpgc.dacd.bitfomo.redditfeeder.infrastructure.ports.EventPublisherPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.jms.*;

public class MessagePublisher implements EventPublisherPort {
    private final MessageBrokerConnection brokerConnection;
    private final String topicName;
    private final ObjectMapper mapper;

    public MessagePublisher(String brokerUrl, String topicName) {
        this.brokerConnection = new MessageBrokerConnection(brokerUrl);
        this.topicName = topicName;
        this.mapper = configureObjectMapper();
    }

    @Override
    public void publish(RedditPost post) {
        Connection connection = null;
        Session session = null;
        try {
            connection = brokerConnection.createConnection();
            session = brokerConnection.createSession(connection);
            publishMessage(post, session);
        } catch (JMSException | JsonProcessingException e) {
            throw new RuntimeException("Error publishing RedditPost to ActiveMQ", e);
        } finally {
            closeResources(connection, session);
        }
    }

    private ObjectMapper configureObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule module = new JavaTimeModule();
        mapper.registerModule(module);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS);
        return mapper;
    }

    private void publishMessage(RedditPost post, Session session) throws JMSException, JsonProcessingException {
        Topic topic = session.createTopic(topicName);
        MessageProducer producer = session.createProducer(topic);
        String json = mapper.writeValueAsString(post);
        TextMessage message = session.createTextMessage(json);
        producer.send(message);
        System.out.println("Event sent to: " + topicName);
    }

    private void closeResources(Connection connection, Session session) {
        try {
            brokerConnection.closeSession(session);
            brokerConnection.closeConnection(connection);
        } catch (JMSException e) {
            System.err.println("Error closing ActiveMQ resources: " + e.getMessage());
        }
    }
}