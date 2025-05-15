package es.ulpgc.dacd.bitfomo.binancefeeder.adapters;

public class CandlestickDBPersistence {
    private long lastKlineIntroduced;

    public long getLastKlineIntroduced() {
        return lastKlineIntroduced;
    }

    public void setLastKlineIntroduced(long lastKlineIntroduced) {
        this.lastKlineIntroduced = lastKlineIntroduced;
    }
}
