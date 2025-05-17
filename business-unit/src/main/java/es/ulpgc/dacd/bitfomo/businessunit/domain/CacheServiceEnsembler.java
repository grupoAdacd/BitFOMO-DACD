package es.ulpgc.dacd.bitfomo.businessunit.domain;

import es.ulpgc.dacd.bitfomo.businessunit.domain.models.BinanceCacheResponse;
import es.ulpgc.dacd.bitfomo.businessunit.domain.models.RedditCacheResponse;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.Recommendation;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.ports.CacheEnsemblerPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CacheServiceEnsembler implements CacheEnsemblerPort {
    private final BinanceCacheService binanceCacheService;
    private final RedditCacheService redditCacheService;

    public CacheServiceEnsembler(BinanceCacheService binanceCacheService, RedditCacheService redditCacheService) {
        this.binanceCacheService = binanceCacheService;
        this.redditCacheService = redditCacheService;
    }

    @Override
    public List<Recommendation> assembly() {
        List<Recommendation> result = new ArrayList<>();
        binanceCacheService.insert("CryptoPrice/binance-feeder");
        redditCacheService.insert("RedditPost/reddit-feeder");
        Map<String, RedditCacheResponse> redditCache = redditCacheService.getPostCache();
        Map<String, BinanceCacheResponse> binanceCache = binanceCacheService.getFinancialCache();
        if (!redditCache.isEmpty()) {
            RedditCacheResponse sample = redditCache.values().iterator().next();
        }
        if (!binanceCache.isEmpty()) {
            BinanceCacheResponse sample = binanceCache.values().iterator().next();
        }
        for (RedditCacheResponse redditPost : redditCache.values()) {
            BinanceCacheResponse closestKline = findClosestKline(redditPost.ts(), binanceCache);
            if (closestKline != null) {
                result.add(new Recommendation(
                        redditPost.ts(),
                        closestKline.openPrice(),
                        closestKline.closePrice(),
                        redditPost.sentiment()));
            } else {
                System.out.println("No matching Binance data found for Reddit timestamp: " + redditPost.ts());
            }
        }
        return result;
    }

    private BinanceCacheResponse findClosestKline(long redditTimestamp, Map<String, BinanceCacheResponse> binanceCache) {
        if (binanceCache.isEmpty()) {
            return null;
        }
        BinanceCacheResponse closestKline = null;
        long minDifference = Long.MAX_VALUE;
        for (BinanceCacheResponse kline : binanceCache.values()) {
            long timeDifference = Math.abs(redditTimestamp - kline.ts());
            if (timeDifference < minDifference && timeDifference <= TimeUnit.HOURS.toMillis(24)) {
                minDifference = timeDifference;
                closestKline = kline;
            }
        }
        if (closestKline == null && !binanceCache.isEmpty()) {
            closestKline = binanceCache.values().iterator().next();
        }
        return closestKline;
    }
}