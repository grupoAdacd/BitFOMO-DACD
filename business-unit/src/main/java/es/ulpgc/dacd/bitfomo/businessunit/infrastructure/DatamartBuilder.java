package es.ulpgc.dacd.bitfomo.businessunit.infrastructure;

import es.ulpgc.dacd.bitfomo.businessunit.domain.BinanceEvent;
import es.ulpgc.dacd.bitfomo.businessunit.domain.DatamartEntry;
import es.ulpgc.dacd.bitfomo.businessunit.domain.RedditEvent;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.ports.DatamartWriter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class DatamartBuilder {
    private final RedditEventProcessor redditProcessor;
    private final BinanceEventProcessor binanceProcessor;
    private final DatamartWriter writer;
    private final long twoDaysMillis = TimeUnit.DAYS.toMillis(2);
    private final Set<Instant> processedRedditTimestamps = new HashSet<>();

    public DatamartBuilder(RedditEventProcessor redditProcessor, BinanceEventProcessor binanceProcessor, DatamartWriter writer) {
        this.redditProcessor = redditProcessor;
        this.binanceProcessor = binanceProcessor;
        this.writer = writer;
    }

    public void buildDatamart() {
        Instant timeThreshold = calculateTimeThreshold();
        processRedditEvents(timeThreshold);
    }

    private Instant calculateTimeThreshold() {
        return Instant.now().minusMillis(twoDaysMillis);
    }

    private void processRedditEvents(Instant timeThreshold) {
        Map<Instant, RedditEvent> redditEvents = redditProcessor.getCache();
        Map<Instant, BinanceEvent> binanceEvents = binanceProcessor.getCache();
        for (RedditEvent redditEvent : redditEvents.values()) {
            processRedditEvent(redditEvent, binanceEvents, timeThreshold);
        }
    }

    private void processRedditEvent(RedditEvent redditEvent, Map<Instant, BinanceEvent> binanceEvents, Instant timeThreshold) {
        if (!redditEvent.ts().isAfter(timeThreshold) || processedRedditTimestamps.contains(redditEvent.ts())) return;
        BinanceEvent closestBinance = findClosestBinance(redditEvent.ts(), binanceEvents);
        if (closestBinance != null) {
            writeEntry(redditEvent, closestBinance);
            processedRedditTimestamps.add(redditEvent.ts());
        }
    }

    private void writeEntry(RedditEvent redditEvent, BinanceEvent closestBinance) {
        DatamartEntry entry = new DatamartEntry(redditEvent.ts(), redditEvent.sentiment(),
                closestBinance.openPrice(), closestBinance.closePrice());
        writer.writeEntry(entry);
    }

    private BinanceEvent findClosestBinance(Instant redditTs, Map<Instant, BinanceEvent> binanceEvents) {
        BinanceEvent closest = null;
        long minDifference = Long.MAX_VALUE;
        long maxRange = TimeUnit.MINUTES.toMillis(5);
        long redditTsMs = redditTs.toEpochMilli();
        for (Map.Entry<Instant, BinanceEvent> entry : binanceEvents.entrySet()) {
            closest = findClosestEntry(entry, redditTsMs, minDifference, maxRange, closest);
        }
        return closest;
    }

    private BinanceEvent findClosestEntry(Map.Entry<Instant, BinanceEvent> entry, long redditTsMs,
                                          long minDifference, long maxRange, BinanceEvent closest) {
        long binanceTsMs = entry.getKey().toEpochMilli();
        long diff = Math.abs(redditTsMs - binanceTsMs);
        if (diff < minDifference && diff <= maxRange) {
            return entry.getValue();
        }
        return closest;
    }
}