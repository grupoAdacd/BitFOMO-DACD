package es.ulpgc.dacd.bitfomo.businessunit.infrastructure;

public class RecommendationStats {
    private double totalSentiment;
    private double totalPriceDifference;
    private int count;

    public void addEntry(double sentiment, double openPrice, double closePrice) {
        totalSentiment += sentiment;
        totalPriceDifference += (closePrice - openPrice);
        count++;
    }

    public double getTotalSentiment() {
        return totalSentiment;
    }

    public double getTotalPriceDifference() {
        return totalPriceDifference;
    }

    public int count() {
        return count;
    }
}
