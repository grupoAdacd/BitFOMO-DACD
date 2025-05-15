package es.ulpgc.dacd.bitfomo.businessunit.domain;


import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.ports.CacheEnsemblerPort;

public class CacheServiceEnsembler implements CacheEnsemblerPort {
    private BinanceCacheService binanceCacheService = new BinanceCacheService();
    private RedditCacheService redditCacheService = new RedditCacheService();
    @Override
    public void assembly() {
        binanceCacheService.insertFromEventstore("binance-feeder");
        redditCacheService.insertFromEventstore("reddit-feeder");
        binanceCacheService.insertFromConsumer("tcp://localhost:61616");
        redditCacheService.insertFromConsumer("tcp://localhost:61616");
    }
}
