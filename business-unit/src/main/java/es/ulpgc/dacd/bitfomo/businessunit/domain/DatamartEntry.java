package es.ulpgc.dacd.bitfomo.businessunit.domain;

import java.time.Instant;

public record DatamartEntry(Instant ts, double sentiment, double openPrice, double closePrice) {}