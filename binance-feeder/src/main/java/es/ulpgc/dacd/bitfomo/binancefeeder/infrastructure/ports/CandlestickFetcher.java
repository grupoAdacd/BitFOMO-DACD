package es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure.ports;

import es.ulpgc.dacd.bitfomo.binancefeeder.domain.Candlestick;
import java.util.List;

public interface CandlestickFetcher {
    List<List<Candlestick>> fetchCandlesticks(long startTime, long endTime);
}