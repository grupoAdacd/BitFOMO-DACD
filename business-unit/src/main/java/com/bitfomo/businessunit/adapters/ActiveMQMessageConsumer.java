package com.bitfomo.businessunit.adapters;

import com.bitfomo.businessunit.domain.DatamartPort;
import com.bitfomo.businessunit.domain.MessageConsumerPort;
//import com.bitfomo.domain.CryptoPrice;
import com.bitfomo.domain.RedditPost;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.util.List;

//TODO APPLY SRP
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
                            if (topicName.equals("RedditPost")) {
                                RedditPost post = mapper.readValue(json, RedditPost.class);
                                datamartPort.storeRedditPost(post);
                                System.out.println("Post recibido y almacenado: " + post);
                            }
//                            else if (topicName.equals("CryptoPrice")) {
//                                CryptoPrice price = mapper.readValue(json, CryptoPrice.class);
//                                datamartPort.storeCryptoPrice(price);
//                                System.out.println("Precio recibido y almacenado: " + price);
//                            }
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