package es.ulpgc.dacd.bitfomo.businessunit.infrastructure.adapters;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.ports.MessageConsumerPort;
import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.json.JSONObject;

import java.util.*;


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
        try {
            Connection connection = new ActiveMQConnectionFactory(url).createConnection();
            connection.setClientID("BusinessUnit");
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            for (String topicName : topics) {
                Map<String, JSONObject> eachResponse = new HashMap<>();
                Topic topic = session.createTopic(topicName);
                TopicSubscriber consumer = session.createDurableSubscriber(topic, topicName + "Subscription");
                consumer.setMessageListener(message -> {
                    try {
                        if (message instanceof TextMessage textMessage) {
                            String responseString = textMessage.getText();
                            JSONObject jsonObject = new JSONObject(responseString);
                            eachResponse.put(topicName, jsonObject);
                            fullResponse.add(eachResponse);
                        }
                    } catch (Exception e) {
                        System.err.println("Error procesando mensaje: " + e.getMessage());
                    }
                });
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            return fullResponse;
        } catch (JMSException e){
            System.err.println("Error connecting: "+ e.getMessage());
        }
        return fullResponse;
    }
}