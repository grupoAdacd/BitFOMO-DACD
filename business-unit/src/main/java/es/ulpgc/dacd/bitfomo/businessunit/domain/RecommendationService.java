package es.ulpgc.dacd.bitfomo.businessunit.domain;

public class RecommendationService {

    public String getRecommendation(double avgSentiment, double avgPriceDifference) {
        boolean isPriceUp = avgPriceDifference > 0;

        // Sentimiento neutro
        if (avgSentiment > -0.1 && avgSentiment < 0.1) {
            return "Mantener";
        }
        // Sentimiento positivo
        else if (avgSentiment >= 0.1) {
            return "Comprar";
        }
        // Sentimiento negativo
        else {
            if (isPriceUp) {
                return "Mantener";
            } else {
                return "Vender";
            }
        }
    }
}