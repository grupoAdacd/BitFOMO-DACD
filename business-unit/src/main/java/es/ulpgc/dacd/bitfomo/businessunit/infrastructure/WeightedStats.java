package es.ulpgc.dacd.bitfomo.businessunit.infrastructure;

public class WeightedStats {
    private double weightedSentiment;
    private double weightedPriceDiff;
    private double totalWeight;

    public void addEntry(double sentiment, double priceDiff, double weight) {
        weightedSentiment += sentiment * weight;
        weightedPriceDiff += priceDiff * weight;
        totalWeight += weight;
    }

    public double getWeightedSentiment() {
        return totalWeight == 0 ? 0.0 : weightedSentiment / totalWeight;
    }

    public double getWeightedPriceDiff() {
        return totalWeight == 0 ? 0.0 : weightedPriceDiff / totalWeight;
    }
}