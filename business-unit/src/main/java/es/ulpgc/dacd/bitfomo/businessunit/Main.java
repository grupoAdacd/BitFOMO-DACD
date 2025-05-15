
package es.ulpgc.dacd.bitfomo.businessunit;

import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.DatamartStorer;

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
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(storer::store, 0, 1, TimeUnit.MINUTES);
        System.out.println("Business Unit running. Data will be stored every 1 minute to: " + csvPath);
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.err.println("Main thread interrupted: " + e.getMessage());
            scheduler.shutdown();
        }
    }
}