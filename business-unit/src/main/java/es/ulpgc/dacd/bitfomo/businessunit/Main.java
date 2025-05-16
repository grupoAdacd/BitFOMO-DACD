package es.ulpgc.dacd.bitfomo.businessunit;

import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.adapters.BusinessUnitService;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.CliUserInterface;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.DatamartStorer;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.ports.BusinessUnitServicePort;

import java.io.File;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: java -jar business-unit.jar <csvPath> <topic1> <topic2>");
            System.exit(1);
        }
        String csvPath = args[0];
        String topic1 = args[1];
        String topic2 = args[2];

        DatamartStorer storer = new DatamartStorer(csvPath, topic1, topic2);
        try {
            storer.store();
        } catch (Exception e) {
            System.err.println("Error during initial data storage: " + e.getMessage());
            e.printStackTrace();
        }
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            try {
                storer.store();
            } catch (Exception e) {
                System.err.println("Error during scheduled data storage: " + e.getMessage());
                e.printStackTrace();
            }
        }, 1, 1, TimeUnit.MINUTES);
        File csvFile = new File(csvPath);
        if (!csvFile.exists()) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        BusinessUnitServicePort service = new BusinessUnitService(csvPath);
        CliUserInterface cli = new CliUserInterface(service);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }));
        Thread cliThread = new Thread(() -> {
            try {
                cli.start();
            } catch (Exception e) {
                System.err.println("Error in CLI: " + e.getMessage());
            }
        });
        cliThread.setDaemon(true);
        cliThread.start();
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            scheduler.shutdown();
            Thread.currentThread().interrupt();
        }
    }
}