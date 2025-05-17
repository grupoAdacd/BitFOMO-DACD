package es.ulpgc.dacd.bitfomo.redditfeeder.infrastructure.adapters;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.Session;
import org.apache.activemq.ActiveMQConnectionFactory;

public class MessageBrokerConnection {
    private final ConnectionFactory connectionFactory;

    public MessageBrokerConnection(String brokerUrl) {
        this.connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
    }

    public Connection createConnection() throws JMSException {
        Connection connection = connectionFactory.createConnection();
        connection.start();
        return connection;
    }

    public Session createSession(Connection connection) throws JMSException {
        return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    public void closeConnection(Connection connection) throws JMSException {
        if (connection != null) {
            connection.close();
        }
    }

    public void closeSession(Session session) throws JMSException {
        if (session != null) {
            session.close();
        }
    }
}