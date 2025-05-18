package es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure.ports;

import es.ulpgc.dacd.bitfomo.binancefeeder.domain.Candlestick;
import java.util.List;

public interface CandlestickParser {
    List<Candlestick> parseCandlesticks(String json);
}