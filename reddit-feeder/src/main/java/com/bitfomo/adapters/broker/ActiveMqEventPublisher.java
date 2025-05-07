package com.bitfomo.adapters.broker;

import com.bitfomo.domain.port.out.EventPublisherPort;
import com.bitfomo.domain.model.RedditPost;
import com.bitfomo.adapters.serialization.RedditPostSerializer;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import com.fasterxml.jackson.core.JsonProcessingException;

public class ActiveMqEventPublisher implements EventPublisherPort {
    private final ConnectionFactory connectionFactory;
    private final String queueName;
    private final RedditPostSerializer serializer;

    public ActiveMqEventPublisher(String brokerUrl, String queueName) {
        this.connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        this.queueName = queueName;
        this.serializer = new RedditPostSerializer();
    }

    @Override
    public void publish(RedditPost post) {
        Connection connection = null;
        Session session = null;

        try {
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue(queueName);
            MessageProducer producer = session.createProducer(queue);

            String json = serializer.serialize(post);
            TextMessage message = session.createTextMessage(json);

            producer.send(message);

        } catch (JMSException | JsonProcessingException e) {
            throw new RuntimeException("Error publishing RedditPost to ActiveMQ", e);
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
