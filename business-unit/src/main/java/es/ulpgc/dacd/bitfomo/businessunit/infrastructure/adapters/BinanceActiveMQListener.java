package es.ulpgc.dacd.bitfomo.businessunit.infrastructure.adapters;

import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.ports.EventProcessor;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.ports.MessageListener;
import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

public class BinanceActiveMQListener implements MessageListener {
    private final String url;
    private final String topicName;

    public BinanceActiveMQListener(String url, String topicName) {
        this.url = url;
        this.topicName = topicName;
    }

    public void startListening(EventProcessor processor) {
        Connection connection = createConnection();
        Session session = createSession(connection);
        MessageConsumer consumer = createConsumer(session);
        setMessageListener(consumer, processor);
    }

    private Connection createConnection() {
        try {
            Connection conn = new ActiveMQConnectionFactory(url).createConnection();
            conn.setClientID("BinanceListener");
            conn.start();
            return conn;
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

    private MessageConsumer createConsumer(Session session) {
        try {
            Topic topic = session.createTopic(topicName);
            return session.createConsumer(topic);
        } catch (JMSException e) {
            throw new RuntimeException("Error creando consumidor: " + e.getMessage());
        }
    }

    private void setMessageListener(MessageConsumer consumer, EventProcessor processor) {
        try {
            consumer.setMessageListener(message -> processMessage(message, processor));
        } catch (JMSException e) {
            System.err.println("Error configurando listener: " + e.getMessage());
        }
    }

    private void processMessage(Message message, EventProcessor processor) {
        if (message instanceof TextMessage) {
            try {
                String text = ((TextMessage) message).getText();
                processor.processBinanceEvent(text);
            } catch (JMSException e) {
                System.err.println("Error procesando mensaje de Binance: " + e.getMessage());
            }
        }
    }
}