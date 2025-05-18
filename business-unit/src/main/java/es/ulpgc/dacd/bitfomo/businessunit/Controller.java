package es.ulpgc.dacd.bitfomo.businessunit;

import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.CLIUserInterface;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.DatamartBuilder;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.BinanceEventProcessor;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.RedditEventProcessor;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.adapters.BinanceActiveMQListener;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.adapters.RedditActiveMQListener;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.adapters.BinanceFileEventReader;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.adapters.RedditFileEventReader;

import java.util.concurrent.*;

public class Controller {
    private final RedditEventProcessor redditProcessor;
    private final BinanceEventProcessor binanceProcessor;
    private final DatamartBuilder datamartBuilder;
    private final CLIUserInterface cli;
    private final RedditFileEventReader redditReader;
    private final BinanceFileEventReader binanceReader;
    private final RedditActiveMQListener redditListener;
    private final BinanceActiveMQListener binanceListener;

    public Controller(RedditEventProcessor redditProcessor, BinanceEventProcessor binanceProcessor,
                      DatamartBuilder datamartBuilder, CLIUserInterface cli,
                      RedditFileEventReader redditReader, BinanceFileEventReader binanceReader,
                      RedditActiveMQListener redditListener, BinanceActiveMQListener binanceListener) {
        this.redditProcessor = redditProcessor;
        this.binanceProcessor = binanceProcessor;
        this.datamartBuilder = datamartBuilder;
        this.cli = cli;
        this.redditReader = redditReader;
        this.binanceReader = binanceReader;
        this.redditListener = redditListener;
        this.binanceListener = binanceListener;
    }

    public void start(String redditPath, String binancePath) {
        loadHistoricalEvents(redditPath, binancePath);
        startListeners();
        scheduleDatamartBuilding();
        startCLI();
        keepProgramRunning();
    }

    private void loadHistoricalEvents(String redditPath, String binancePath) {
        redditReader.readEvents(redditPath, redditProcessor);
        binanceReader.readEvents(binancePath, binanceProcessor);
    }

    private void startListeners() {
        try {
            redditListener.startListening(redditProcessor);
            binanceListener.startListening(binanceProcessor);
        } catch (Exception e) {
            System.err.println("Error iniciando listeners: " + e.getMessage());
            System.exit(1);
        }
    }

    private void scheduleDatamartBuilding() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                datamartBuilder.buildDatamart();
            } catch (Exception e) {
                System.err.println("Error generando datamart: " + e.getMessage());
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    private void startCLI() {
        Thread cliThread = new Thread(cli::start);
        cliThread.setDaemon(true);
        cliThread.start();
    }

    private void keepProgramRunning() {
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}