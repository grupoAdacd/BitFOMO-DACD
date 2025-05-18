package es.ulpgc.dacd.bitfomo.businessunit.domain;

public record RedditEvent(String id, long ts, String text) {}
public record BinanceEvent(long ts, double openPrice, double closePrice) {}
public record DatamartEntry(long ts, double sentiment, double openPrice, double closePrice) {}