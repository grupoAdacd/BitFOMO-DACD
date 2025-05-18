package es.ulpgc.dacd.bitfomo.businessunit.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BinanceEvent(Instant ts, double openPrice, double closePrice) {}