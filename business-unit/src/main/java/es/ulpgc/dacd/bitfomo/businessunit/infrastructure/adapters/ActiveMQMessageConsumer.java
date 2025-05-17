package es.ulpgc.dacd.bitfomo.businessunit.infrastructure.adapters;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.ports.MessageConsumerPort;
import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.json.JSONObject;
import java.util.*;
import java.util.concurrent.CountDownLatch;


public class ActiveMQMessageConsumer implements MessageConsumerPort {
    private final List<String> topics;
    private final String url;

    public ActiveMQMessageConsumer(List<String> topics, String url) {
        this.topics = topics;
        this.url = url;
    }

    @Override
    public List<Map<String, JSONObject>> startConsuming() {
        List<Map<String, JSONObject>> fullResponse = new ArrayList<>();
        Connection connection = null;
        Session session = null;
        try {
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
            connection = connectionFactory.createConnection();
            connection.setClientID("BusinessUnit-" + System.currentTimeMillis());
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            CountDownLatch latch = new CountDownLatch(topics.size());
            for (String topicName : topics) {
                try {
                    Topic topic = session.createTopic(topicName);
                    MessageConsumer consumer = session.createConsumer(topic);
                    consumer.setMessageListener(message -> {
                        try {
                            if (message instanceof TextMessage textMessage) {
                                String responseString = textMessage.getText();
                                JSONObject jsonObject = new JSONObject(responseString);
                                Map<String, JSONObject> eachResponse = new HashMap<>();
                                eachResponse.put(topicName, jsonObject);
                                fullResponse.add(eachResponse);
                            }
                        } catch (Exception e) {
                            System.err.println("Error processing message from topic " + topicName + ": " + e.getMessage());
                        } finally {
                            latch.countDown();
                        }
                    });
                } catch (JMSException e) {
                    System.err.println("Error setting up consumer for topic " + topicName + ": " + e.getMessage());
                    latch.countDown();
                }
            }
        } catch (JMSException e) {
            System.err.println("Error connecting to ActiveMQ: " + e.getMessage());
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (JMSException e) {
                System.err.println("Error closing ActiveMQ connection: " + e.getMessage());
            }
        }
        return fullResponse;
    }
}