package es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure.ports;

import es.ulpgc.dacd.bitfomo.binancefeeder.domain.Candlestick;

public interface CandlestickSerializer {
    String serializeCandlestick(Candlestick candlestick);
}