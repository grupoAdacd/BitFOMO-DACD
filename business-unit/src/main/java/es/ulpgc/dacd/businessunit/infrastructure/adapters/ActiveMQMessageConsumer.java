package es.ulpgc.dacd.businessunit.infrastructure.adapters;

import es.ulpgc.dacd.businessunit.infrastructure.ports.DatamartPort;
import es.ulpgc.dacd.businessunit.infrastructure.ports.MessageConsumerPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.util.List;
import java.util.Map;

public class ActiveMQMessageConsumer implements MessageConsumerPort {
    private final List<String> topics;
    private final String url;
    private final DatamartPort datamartPort;
    private final ObjectMapper mapper;

    public ActiveMQMessageConsumer(List<String> topics, String url, DatamartPort datamartPort) {
        this.topics = topics;
        this.url = url;
        this.datamartPort = datamartPort;
        this.mapper = new ObjectMapper();
    }

    @Override
    public void startConsuming() {
        try {
            Connection connection = new ActiveMQConnectionFactory(url).createConnection();
            connection.setClientID("BusinessUnit");
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            for (String topicName : topics) {
                Topic topic = session.createTopic(topicName);
                TopicSubscriber consumer = session.createDurableSubscriber(topic, topicName + "Subscription");

                consumer.setMessageListener(message -> {
                    try {
                        if (message instanceof TextMessage textMessage) {
                            String json = textMessage.getText();
                            Map<String, Object> eventData = mapper.readValue(json, Map.class);
                            if (topicName.equals("RedditPost")) {
                                datamartPort.storeRedditPost(eventData);
                                System.out.println("Post recibido y almacenado: " + eventData);
                            } else if (topicName.equals("CryptoPrice")) {
                                datamartPort.storeCryptoPrice(eventData);
                                System.out.println("Precio recibido y almacenado: " + eventData);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error procesando mensaje: " + e.getMessage());
                    }
                });
            }
        } catch (JMSException e) {
            throw new RuntimeException("Error al iniciar el consumidor de mensajes: " + e.getMessage());
        }
    }
}