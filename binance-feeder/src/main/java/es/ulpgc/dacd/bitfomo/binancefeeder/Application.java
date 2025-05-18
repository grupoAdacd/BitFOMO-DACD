package es.ulpgc.dacd.bitfomo.binancefeeder;

import es.ulpgc.dacd.bitfomo.binancefeeder.adapters.ActiveMQEventPublisher;
import es.ulpgc.dacd.bitfomo.binancefeeder.adapters.ExchangeApiClient;
import es.ulpgc.dacd.bitfomo.binancefeeder.domain.Candlestick;
import es.ulpgc.dacd.bitfomo.binancefeeder.transformer.CandlestickSerializer;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Application {
    private static final int INITIAL_DELAY_SECONDS = 0;
    private static final int PERIOD_MINUTES = 5; // Fetch cada 5 minutos

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java -jar binance-feeder.jar <brokerUrl> <queueName>");
            System.exit(1);
        }

        String brokerUrl = args[0];
        String queueName = args[1];

        ExchangeApiClient binanceApi = new ExchangeApiClient();
        CandlestickSerializer serializer = new CandlestickSerializer();
        ActiveMQEventPublisher publisher = new ActiveMQEventPublisher(brokerUrl, queueName, serializer);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("Running scheduled fetch at: " + System.currentTimeMillis());
                for (ArrayList<Candlestick> KlinesList : binanceApi.obtainFullResponse()) {
                    System.out.println("Obtaining Arrays of Klines...");
                    for (Candlestick kline : KlinesList) {
                        try {
                            publisher.publish(kline);
                            System.out.println("Publishing events...");
                        } catch (Exception e) {
                            System.err.println("Error Publishing events..." + e);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error in scheduled task: " + e.getMessage());
                e.printStackTrace();
            }
        }, INITIAL_DELAY_SECONDS, PERIOD_MINUTES, TimeUnit.MINUTES);
        try {
            System.out.println("Binance Application running...");
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            System.out.println("Shutting down...");
            scheduler.shutdown();
            System.exit(0);
        }
    }
}