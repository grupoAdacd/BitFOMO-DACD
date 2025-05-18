package es.ulpgc.dacd.bitfomo.businessunit.domain;

public record Entry(long ts, double sentiment, double priceDiff) {}