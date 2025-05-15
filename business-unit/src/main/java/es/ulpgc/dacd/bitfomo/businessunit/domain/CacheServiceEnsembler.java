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
        for (String redditTimestamp: redditCache.keySet()){
            RedditCacheResponse redditCacheResponse = redditCache.get(redditTimestamp);
            for (String binanceTimestamp: binanceCache.keySet()){
                BinanceCacheResponse binanceCacheResponse = binanceCache.get(binanceTimestamp);
                if (isValid(binanceCacheResponse.ts(), redditCacheResponse.ts())){
                    result.add(new Recommendation(
                            redditCacheResponse.ts(),
                            binanceCacheResponse.openPrice(),
                            binanceCacheResponse.closePrice(),
                            redditCacheResponse.sentiment()));
                }
            }
        }
        return result;
    }

    public boolean isValid(long binanceTimestamp, long redditTimestamp){
        final long MAX_DIFFERENCE_MS = TimeUnit.SECONDS.toMillis(300); // 5 minutes in milliseconds
        long difference = Math.abs(redditTimestamp - binanceTimestamp);
        return difference <= MAX_DIFFERENCE_MS;
    }
}