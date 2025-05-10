package com.bitfomo.adapters;

import com.bitfomo.domain.CandlestickData;
import com.bitfomo.domain.EventPublisherPort;
import com.bitfomo.transformer.CandleStickSerializer;
import org.apache.activemq.ActiveMQConnectionFactory;
import jakarta.jms.*;

public class ActiveMQEventPublisher implements EventPublisherPort {
    private final ConnectionFactory connectionFactory;
    private final String topicName;
    private final CandleStickSerializer serializer;
    public ActiveMQEventPublisher(String brokerUrl, String topicName, CandleStickSerializer serializer) {
        this.connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        this.topicName = topicName;
        this.serializer = serializer;
    }
    @Override
    public void publish(CandlestickData kline) {
        Connection connection = null;
        Session session = null;
        try {
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(topicName);
            MessageProducer producer = session.createProducer(topic);
            String json = serializer.serialize(kline).toString();
            TextMessage message = session.createTextMessage(json);
            producer.send(message);
            System.out.println("Event sent to: " + topicName);

        } catch (JMSException e) {
            throw new RuntimeException("Error Publishing Event...", e);
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