
package es.ulpgc.dacd;

import es.ulpgc.dacd.businessunit.infrastructure.adapters.ActiveMQMessageConsumer;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.CsvDatamart;
import es.ulpgc.dacd.businessunit.infrastructure.adapters.FileEventStoreReader;
import es.ulpgc.dacd.businessunit.infrastructure.BusinessUnitService;
import es.ulpgc.dacd.businessunit.infrastructure.ports.BusinessUnitServicePort;
import es.ulpgc.dacd.businessunit.infrastructure.ports.DatamartPort;
import es.ulpgc.dacd.businessunit.infrastructure.ports.EventStoreReaderPort;
import es.ulpgc.dacd.businessunit.infrastructure.ports.MessageConsumerPort;
import es.ulpgc.dacd.businessunit.infrastructure.AsciiArtBanner;
import es.ulpgc.dacd.businessunit.infrastructure.CliUserInterface;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java -jar business-unit.jar <brokerUrl> <csvPath>");
            System.exit(1);
        }
        String brokerUrl = args[0];
        String csvPath = args[1];
        AsciiArtBanner.printBanner("BitFOMO");
        DatamartPort datamartPort = new CsvDatamart(csvPath);
        EventStoreReaderPort eventStoreReader = new FileEventStoreReader();
        BusinessUnitServicePort businessUnitService = new BusinessUnitService(datamartPort, eventStoreReader);
        MessageConsumerPort messageConsumer = new ActiveMQMessageConsumer(
                List.of("RedditPost", "CryptoPrice"),
                brokerUrl,
                datamartPort
        );

        Thread consumerThread = new Thread(messageConsumer::startConsuming);
        consumerThread.setDaemon(true);
        consumerThread.start();

        System.out.println("Connecting to broker: " + brokerUrl);
        System.out.println("Using Datamart: " + csvPath);
        System.out.println("Initializing user interface...");
        CliUserInterface cli = new CliUserInterface(businessUnitService);
        cli.start();
    }
}