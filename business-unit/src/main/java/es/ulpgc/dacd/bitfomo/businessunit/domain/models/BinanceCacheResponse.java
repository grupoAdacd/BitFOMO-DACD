package es.ulpgc.dacd.bitfomo.businessunit.domain.models;

public record BinanceCacheResponse (
        long ts,
        double openPrice,
        double closePrice
){}
