package es.ulpgc.dacd.eventstorebuilder.infrastructure.adapters;

import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import java.util.List;

public class MessageReceiver {
    private final List<String> topics;
    private final String url;
    private final EventStore eventStore = new EventStore();

    public MessageReceiver(List<String> topics, String url) {
        this.topics = topics;
        this.url = url;
    }

    public void start() throws JMSException {
        Connection connection = new ActiveMQConnectionFactory(url).createConnection();
        connection.setClientID("EventStoreBuilder");
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        for (String topicName : topics) {
            Topic topic = session.createTopic(topicName);
            TopicSubscriber consumer = session.createDurableSubscriber(topic, topicName + "Subscription");

            consumer.setMessageListener(message -> {
                try {
                    if (message instanceof TextMessage textMessage) {
                        eventStore.saveEvent(topicName, textMessage.getText());
                        System.out.println("Event caught: " + topicName);
                    }
                } catch (JMSException e) {
                    System.err.println("Error in topic " + topicName + ": " + e.getMessage());
                }
            });
        }
    }
}
