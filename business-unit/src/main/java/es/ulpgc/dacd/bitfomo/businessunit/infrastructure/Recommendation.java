package es.ulpgc.dacd.bitfomo.businessunit.infrastructure;

public record Recommendation (
     long ts,
     double openPrice,
     double closePrice,
     double sentiment
    ){}