package com.bitfomo;

import com.bitfomo.businessunit.adapters.ActiveMQMessageConsumer;
import com.bitfomo.businessunit.adapters.CsvDatamart;
import com.bitfomo.businessunit.application.BusinessUnitService;
import com.bitfomo.businessunit.domain.BusinessUnitServicePort;
import com.bitfomo.businessunit.domain.DatamartPort;
import com.bitfomo.businessunit.domain.MessageConsumerPort;
import com.bitfomo.businessunit.ui.CliUserInterface;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java -jar business-unit.jar <brokerUrl> <csvPath>");
            System.exit(1);
        }

        String brokerUrl = args[0];
        String csvPath = args[1];

        DatamartPort datamartPort = new CsvDatamart(csvPath);

        BusinessUnitServicePort businessUnitService = new BusinessUnitService(datamartPort);

        MessageConsumerPort messageConsumer = new ActiveMQMessageConsumer(
                List.of("RedditPost", "CryptoPrice"),
                brokerUrl,
                datamartPort
        );

        new Thread(messageConsumer::startConsuming).start();

        CliUserInterface cli = new CliUserInterface(businessUnitService);
        cli.start();
    }
}