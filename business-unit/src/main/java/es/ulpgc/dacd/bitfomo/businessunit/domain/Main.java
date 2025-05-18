package es.ulpgc.dacd.bitfomo.businessunit.domain;

import es.ulpgc.dacd.bitfomo.businessunit.adapters.ActiveMQListener;
import es.ulpgc.dacd.bitfomo.businessunit.adapters.EventFileReader;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.CLIUserInterface;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.Deduplicator;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.SentimentAnalyzer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java -jar business-unit.jar <datamart.csv>");
            System.exit(1);
        }
        String csvPath = args[0];

        // Inicializar componentes
        Deduplicator deduplicator = new Deduplicator();
        SentimentAnalyzer sentimentAnalyzer = new SentimentAnalyzer();
        DatamartEnsembler ensembler = new DatamartEnsembler(csvPath, sentimentAnalyzer, deduplicator);

        // Cargar datos históricos
        EventFileReader reader = new EventFileReader(ensembler);
        reader.loadHistoricalEvents("src/main/eventstore/RedditPost/reddit-feeder");
        reader.loadHistoricalEvents("src/main/eventstore/CryptoPrice/binance-feeder");

        // Iniciar listener de ActiveMQ
        ActiveMQListener listener = new ActiveMQListener(ensembler);
        try {
            listener.start();
        } catch (Exception e) {
            System.err.println("Error starting ActiveMQ listener: " + e.getMessage());
            System.exit(1);
        }

        // Scheduler para actualizar datamart cada 5 minutos
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                ensembler.assembleDatamart();
            } catch (Exception e) {
                System.err.println("Error assembling datamart: " + e.getMessage());
            }
        }, 0, 1, TimeUnit.MINUTES);

        // Iniciar CLI
        CLIUserInterface cli = new CLIUserInterface(csvPath);
        Thread cliThread = new Thread(cli::start);
        cliThread.setDaemon(true);
        cliThread.start();

        // Mantener el programa corriendo
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            scheduler.shutdown();
            Thread.currentThread().interrupt();
        }
    }
}