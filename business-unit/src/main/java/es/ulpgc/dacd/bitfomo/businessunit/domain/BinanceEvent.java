package es.ulpgc.dacd.bitfomo.businessunit.domain;

public record BinanceEvent(long ts, double openPrice, double closePrice) {}

