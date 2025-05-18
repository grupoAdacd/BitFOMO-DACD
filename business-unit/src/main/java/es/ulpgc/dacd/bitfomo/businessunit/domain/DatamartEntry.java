package es.ulpgc.dacd.bitfomo.businessunit.domain;

public record DatamartEntry(long ts, double sentiment, double openPrice, double closePrice) {}