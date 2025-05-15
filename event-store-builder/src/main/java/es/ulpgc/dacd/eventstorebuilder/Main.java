package es.ulpgc.dacd.eventstorebuilder;

import es.ulpgc.dacd.eventstorebuilder.insfrastructure.adapters.MessageReceiver;
import jakarta.jms.JMSException;

import java.util.List;

public class Main {
    public static void main(String[] args) throws JMSException {
        if (args.length < 1) {
            System.err.println("Usage: java -jar event-store-builder.jar <brokerUrl>");
            System.exit(1);
        }

        String brokerUrl = args[0];

        List<String> topics = List.of("RedditPost");

        MessageReceiver receiver = new MessageReceiver(topics, brokerUrl);

        receiver.start();
    }
}
