package es.ulpgc.dacd.businessunit.infrastructure;

import es.ulpgc.dacd.businessunit.infrastructure.ports.BusinessUnitServicePort;

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
        System.out.println("Type help to see available commands:");
        String help = scanner.nextLine().trim();
        if (help.equalsIgnoreCase("help")) {
            System.out.println("  sentiment <subreddit> - Shows average sentiment given a subreddit (last hour)");
            System.out.println("  price <date-start> <date-end> - Shows average price of a given interval of time (format=yyyy/MM/dd)");
            System.out.println("  recommend <subreddit> <date-start> <date-end> - Generates a financial recommendation (last hour)");
            System.out.println("  exit - Stops execution");
        }

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit")) {
                break;
            }
            String[] parts = input.split("\\s+");
            if (parts.length < 2) {
                System.out.println("Invalid command.");
                continue;
            }
            try {
                String endTime = Instant.now().toString();
                String startTime = Instant.now().minus(1, ChronoUnit.HOURS).toString();

                switch (parts[0].toLowerCase()) {
                    case "sentiment":
                        String subreddit = parts[1];
                        double averageSentiment = businessUnitService.calculateAverageSentiment(subreddit, startTime, endTime);
                        System.out.printf("Average sentiment of %s (last hour): %.2f%n", subreddit, averageSentiment);
                        break;
                    case "price":
                        String symbol = parts[1];
                        double averagePrice = businessUnitService.getAveragePrice(symbol, startTime, endTime);
                        System.out.printf("Average price of %s (last hour): %.2f%n", symbol, averagePrice);
                        break;
                    case "recommend":
                        if (parts.length < 3) {
                            System.out.println("Invalid command. Use: recommend <subreddit> <symbol>");
                            continue;
                        }
                        subreddit = parts[1];
                        symbol = parts[2];
                        String recommendation = businessUnitService.generateRecommendation(subreddit, symbol, startTime, endTime);
                        System.out.println(recommendation);
                        break;
                    default:
                        System.out.println("Unknown command. Type help to see available commands");
                        String helpUnknownCommand = scanner.nextLine().trim();
                        if (helpUnknownCommand.equalsIgnoreCase("help")){
                            System.out.println("  sentiment <subreddit> - Shows average sentiment given a subreddit (last hour)");
                            System.out.println("  price <date-start> <date-end> - Shows average price of a given interval of time (format=yyyy/MM/dd)");
                            System.out.println("  recommend <subreddit> <date-start> <date-end> - Generates a financial recommendation (last hour)");
                            System.out.println("  exit - Stops execution");
                        }
                }
            } catch (Exception e) {
                System.out.println("Error processing command: " + e.getMessage());
            }
        }

        scanner.close();
        System.exit(0);
    }
}