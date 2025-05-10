package com.bitfomo.businessunit.ui;

import com.bitfomo.businessunit.domain.BusinessUnitServicePort;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

public class CliUserInterface {
    private final BusinessUnitServicePort businessUnitService;

    public CliUserInterface(BusinessUnitServicePort businessUnitService) {
        this.businessUnitService = businessUnitService;
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Business Unit CLI. Comandos disponibles:");
        System.out.println("  sentiment <subreddit> - Muestra el sentimiento promedio del subreddit (última hora)");
        System.out.println("  price <symbol> - Muestra el precio promedio del símbolo (última hora)");
        System.out.println("  recommend <subreddit> <symbol> - Genera una recomendación (última hora)");
        System.out.println("  exit - Salir");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit")) {
                break;
            }

            String[] parts = input.split("\\s+");
            if (parts.length < 2) {
                System.out.println("Comando inválido. Usa: sentiment <subreddit>, price <symbol>, o recommend <subreddit> <symbol>");
                continue;
            }

            try {
                // Usa el último intervalo de 1 hora por defecto
                String endTime = Instant.now().toString();
                String startTime = Instant.now().minus(1, ChronoUnit.HOURS).toString();

                switch (parts[0].toLowerCase()) {
                    case "sentiment":
                        String subreddit = parts[1];
                        double averageSentiment = businessUnitService.calculateAverageSentiment(subreddit, startTime, endTime);
                        System.out.printf("Sentimiento promedio de %s (última hora): %.2f%n", subreddit, averageSentiment);
                        break;
                    case "price":
                        String symbol = parts[1];
                        double averagePrice = businessUnitService.getAveragePrice(symbol, startTime, endTime);
                        System.out.printf("Precio promedio de %s (última hora): %.2f%n", symbol, averagePrice);
                        break;
                    case "recommend":
                        if (parts.length < 3) {
                            System.out.println("Comando inválido. Usa: recommend <subreddit> <symbol>");
                            continue;
                        }
                        subreddit = parts[1];
                        symbol = parts[2];
                        String recommendation = businessUnitService.generateRecommendation(subreddit, symbol, startTime, endTime);
                        System.out.println(recommendation);
                        break;
                    default:
                        System.out.println("Comando no reconocido. Comandos disponibles: sentiment, price, recommend, exit");
                }
            } catch (Exception e) {
                System.out.println("Error procesando el comando: " + e.getMessage());
            }
        }

        scanner.close();
        System.exit(0);
    }
}