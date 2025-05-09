package com.bitfomo.adapters;

import com.bitfomo.domain.EventPublisherPort;
import com.bitfomo.domain.RedditPost;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.activemq.ActiveMQConnectionFactory;
import jakarta.jms.*;

public class ActiveMqEventPublisher implements EventPublisherPort {
    private final ConnectionFactory connectionFactory;
    private final String topicName;
    private final ObjectMapper mapper;

    public ActiveMqEventPublisher(String brokerUrl, String topicName) {
        this.connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        this.topicName = topicName;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void publish(RedditPost post) {
        Connection connection = null;
        Session session = null;

        try {
            connection = connectionFactory.createConnection();
            connection.start();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(topicName);
            MessageProducer producer = session.createProducer(topic);

            String json = mapper.writeValueAsString(post);
            //System.out.println("JSON enviado: " + json); // Descomentar este log para inspeccionar el JSON

            TextMessage message = session.createTextMessage(json);
            producer.send(message);
            System.out.println("Evento enviado al topic: " + topicName);

        } catch (JMSException | JsonProcessingException e) {
            throw new RuntimeException("Error publicando el RedditPost a ActiveMQ", e);
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (JMSException ignore) {
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException ignore) {
                }
            }
        }
    }
}