package es.ulpgc.dacd.bitfomo.businessunit2.domain;

public record DatamartEntry(long ts, double sentiment, double openPrice, double closePrice) {}