package es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure.adapters;

import es.ulpgc.dacd.bitfomo.binancefeeder.domain.Candlestick;
import es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure.ports.CandlestickSerializer;
import es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure.ports.EventPublisherPort;
import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

public class ActiveMQCandlestickPublisher implements EventPublisherPort {
    private final ConnectionFactory connectionFactory;
    private final String topicName;
    private final CandlestickSerializer serializer;

    public ActiveMQCandlestickPublisher(String brokerUrl, String topicName, CandlestickSerializer serializer) {
        this.connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        this.topicName = topicName;
        this.serializer = serializer;
    }

    @Override
    public void publish(Candlestick candlestick) {
        Connection connection = createConnection();
        Session session = createSession(connection);
        MessageProducer producer = createProducer(session);
        sendMessage(candlestick, session, producer);
        closeResources(session, connection);
    }

    private Connection createConnection() {
        try {
            Connection connection = connectionFactory.createConnection();
            connection.start();
            return connection;
        } catch (JMSException e) {
            throw new RuntimeException("Error creando conexión: " + e.getMessage());
        }
    }

    private Session createSession(Connection connection) {
        try {
            return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException e) {
            throw new RuntimeException("Error creando sesión: " + e.getMessage());
        }
    }

    private MessageProducer createProducer(Session session) {
        try {
            Topic topic = session.createTopic(topicName);
            return session.createProducer(topic);
        } catch (JMSException e) {
            throw new RuntimeException("Error creando productor: " + e.getMessage());
        }
    }

    private void sendMessage(Candlestick candlestick, Session session, MessageProducer producer) {
        try {
            String json = serializer.serializeCandlestick(candlestick);
            TextMessage message = session.createTextMessage(json);
            producer.send(message);
            System.out.println("Evento enviado a: " + topicName);
        } catch (JMSException e) {
            System.err.println("Error enviando mensaje: " + e.getMessage());
        }
    }

    private void closeResources(Session session, Connection connection) {
        try {
            if (session != null) session.close();
            if (connection != null) connection.close();
        } catch (JMSException e) {
            System.err.println("Error cerrando recursos: " + e.getMessage());
        }
    }
}