package es.ulpgc.dacd.bitfomo.businessunit2.adapters;

import es.ulpgc.dacd.bitfomo.businessunit2.domain.DatamartEnsembler;
import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.json.JSONObject;

public class ActiveMQListener {
    private final DatamartEnsembler ensembler;
    private final String url = "tcp://localhost:61616";

    public ActiveMQListener(DatamartEnsembler ensembler) {
        this.ensembler = ensembler;
    }

    public void start() throws JMSException {
        Connection connection = new ActiveMQConnectionFactory(url).createConnection();
        connection.setClientID("BusinessUnitListener");
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Suscripción a RedditPost
        Topic redditTopic = session.createTopic("RedditPost");
        MessageConsumer redditConsumer = session.createConsumer(redditTopic);
        redditConsumer.setMessageListener(message -> {
            if (message instanceof TextMessage) {
                try {
                    String text = ((TextMessage) message).getText();
                    JSONObject json = new JSONObject(text);
                    ensembler.processRedditMessage(json);
                } catch (JMSException e) {
                    System.err.println("Error processing RedditPost: " + e.getMessage());
                }
            }
        });

        // Suscripción a CryptoPrice
        Topic cryptoTopic = session.createTopic("CryptoPrice");
        MessageConsumer cryptoConsumer = session.createConsumer(cryptoTopic);
        cryptoConsumer.setMessageListener(message -> {
            if (message instanceof TextMessage) {
                try {
                    String text = ((TextMessage) message).getText();
                    JSONObject json = new JSONObject(text);
                    ensembler.processBinanceMessage(json);
                } catch (JMSException e) {
                    System.err.println("Error processing CryptoPrice: " + e.getMessage());
                }
            }
        });

        System.out.println("Listening to ActiveMQ topics: RedditPost and CryptoPrice");
    }
}