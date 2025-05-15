package es.ulpgc.dacd.bitfomo.businessunit.infrastructure;

public class Recommendation {
    private long ts;
    private double openPrice;
    private double closePrice;
    private double sentiment;

    public Recommendation(long ts, String openPrice, String closePrice, double sentiment) {
        this.ts = ts;
        this.openPrice = Double.parseDouble(openPrice);
        this.closePrice = Double.parseDouble(closePrice);
        this.sentiment = sentiment;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public double getOpenPrice() {
        return openPrice;
    }

    public void setOpenPrice(double openPrice) {
        this.openPrice = openPrice;
    }

    public double getClosePrice() {
        return closePrice;
    }

    public void setClosePrice(double closePrice) {
        this.closePrice = closePrice;
    }

    public double getSentiment() {
        return sentiment;
    }

    public void setSentiment(double sentiment) {
        this.sentiment = sentiment;
    }
}