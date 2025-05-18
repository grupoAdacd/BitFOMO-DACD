package es.ulpgc.dacd.bitfomo.businessunit2.infrastructure;

import es.ulpgc.dacd.bitfomo.businessunit2.domain.RecommendationService;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class CLIUserInterface {
    private final String csvPath; // Ruta al archivo CSV del datamart
    private final RecommendationService recommendationService;

    public CLIUserInterface(String csvPath) {
        this.csvPath = csvPath;
        this.recommendationService = new RecommendationService();
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Escribe 'help' para ver los comandos disponibles o 'exit' para salir:");
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit")) {
                System.out.println("¡Adiós!");
                break;
            }
            if (input.equalsIgnoreCase("help")) {
                showHelp();
                continue;
            }
            if (input.equalsIgnoreCase("recommend")) {
                showRecommendation();
            } else {
                System.out.println("Comando inválido. Escribe 'help' para ver los comandos.");
            }
        }
        scanner.close();
    }

    private void showHelp() {
        System.out.println("Comandos disponibles:");
        System.out.println("  recommend     - Muestra una recomendación basada en la media de las últimas 48 horas");
        System.out.println("  help          - Muestra este mensaje de ayuda");
        System.out.println("  exit          - Sale de la aplicación");
    }

    private void showRecommendation() {
        try (BufferedReader reader = new BufferedReader(new FileReader(csvPath))) {
            long currentTime = System.currentTimeMillis();
            long fortyEightHoursAgo = currentTime - TimeUnit.HOURS.toMillis(48);

            double totalSentiment = 0.0;
            double totalPriceDifference = 0.0;
            int count = 0;

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 4) continue;

                long timestamp = Long.parseLong(parts[0]);
                if (timestamp >= fortyEightHoursAgo) {
                    double sentiment = Double.parseDouble(parts[1]);
                    double openPrice = Double.parseDouble(parts[2]);
                    double closePrice = Double.parseDouble(parts[3]);

                    totalSentiment += sentiment;
                    totalPriceDifference += (closePrice - openPrice);
                    count++;
                }
            }

            if (count == 0) {
                System.out.println("No hay datos disponibles para generar una recomendación.");
                return;
            }

            double avgSentiment = totalSentiment / count;
            double avgPriceDifference = totalPriceDifference / count;

            String recommendation = recommendationService.getRecommendation(avgSentiment, avgPriceDifference);
            String explanation = getRecommendationExplanation(avgSentiment, avgPriceDifference, recommendation);

            System.out.println("Recomendación basada en la media de las últimas 48 horas:");
            System.out.println("Sentimiento medio: " + avgSentiment);
            System.out.println("Media de la diferencia de precios (cierre - apertura): " + avgPriceDifference);
            System.out.println("Recomendación: " + recommendation);
            System.out.println("Por qué: " + explanation);
        } catch (IOException e) {
            System.err.println("Error al leer el datamart: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Error en el formato numérico de los datos: " + e.getMessage());
        }
    }

    private String getRecommendationExplanation(double avgSentiment, double avgPriceDifference, String recommendation) {
        boolean isPriceUp = avgPriceDifference > 0;
        String sentimentDescription = avgSentiment >= 0.1 ? "positivo" : (avgSentiment <= -0.1 ? "negativo" : "neutro");
        String priceTrend = isPriceUp ? "subió" : "bajó";

        if (recommendation.equals("Mantener")) {
            if (sentimentDescription.equals("neutro")) {
                return "El sentimiento es neutro y no hay una tendencia clara en el precio.";
            } else {
                return "A pesar del sentimiento " + sentimentDescription + ", la tendencia del precio no es concluyente.";
            }
        } else if (recommendation.equals("Comprar")) {
            return "El sentimiento es positivo y el precio ha subido en promedio.";
        } else if (recommendation.equals("Vender")) {
            return "El sentimiento es negativo y el precio ha bajado en promedio.";
        }
        return "No se pudo determinar la razón.";
    }
}