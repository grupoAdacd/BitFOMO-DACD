package es.ulpgc.dacd.bitfomo.businessunit.infrastructure;

import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.ports.BusinessUnitServicePort;

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
        String BITFOMO = """
                                                                                                            .         .                          \s
                                                                     8-888888888888     ,o888888o.           ,8.       ,8.           ,o888888o.    \s
                8-888888888o.                                         8-88888         . 8888     `88.        ,888.     ,888.       . 8888     `88.  \s
                8-8888      8L  8-8888       8-8888                   8-8888         ,8 8888       `8b      .`8888.   .`8888.     ,8 8888       `8b \s
                8-8888     ,88  8-8888       8-8888                  8-8888         88 8888        `8b    ,8.`8888. ,8.`8888.    88 8888        `8b\s
                8-8888.   ,88'           88888-888888888            8-88888888     88 8888    88     88   ,8'8.`8888,8^8.`8888.  88 8888    88    88\s
                8-8888888888    8-8888       8-8888                 8-8888         88 8888   8888    88  ,8' `8.`8888' `8.`8888. 88 8888   8888   88\s
                8-8888    `88.  8-8888       8-8888                  8-8888         88 8888   88    ,8P ,8'   `8.`88'   `8.`8888. 88 8888   88   ,8P\s
                8-8888      88  8-8888       8-8888                   8-8888         `8 8888       ,8P ,8'     `8.`'     `8.`8888.`8 8888       ,8P \s
                8-8888    ,88'  8-8888       8-8888                  8-8888          ` 8888     ,88' ,8'       `8        `8.`8888.` 8888     ,88'  \s
                8-888888888P    8-8888       8-8888                 8-8888             `8888888P'  ,8'         `         `8.`8888.  `8888888P'    \s
                """;
        System.out.print(BITFOMO);
        System.out.println("\nType 'help' to see available commands or 'exit' to quit:");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Goodbye!");
                break;
            }
            if (input.equalsIgnoreCase("help")) {
                showHelp();
                continue;
            }
            String[] parts = input.split("\\s+");
            if (parts.length < 1) {
                System.out.println("Invalid command. Type 'help' for available commands.");
                continue;
            }
            try {
                switch (parts[0].toLowerCase()) {
                    case "recommend":
                        handleRecommendCommand(parts);
                        break;
                    default:
                        System.out.println("Unknown command. Only 'recommend' is available. Type 'help' for details.");
                }
            } catch (Exception e) {
                System.out.println("Error processing command: " + e.getMessage());
            }
        }

        scanner.close();
    }

    private void showHelp() {
        System.out.println("Available commands:");
        System.out.println("  recommend     - Get investment recommendation for BTC");
        System.out.println("                  Compares the open price from 6 hours ago with");
        System.out.println("                  the current close price to recommend whether to invest");
        System.out.println("  help           - Shows this help message");
        System.out.println("  exit           - Exits the application");

    }

    private void handleRecommendCommand(String[] parts) {

        String endTime = Instant.now().toString();
        String startTime = Instant.now().minus(6, ChronoUnit.HOURS).toString();

        System.out.println("Analyzing price movement for BTC...");
        System.out.println("Comparing open price from 6 hours ago with current close price...");
        String recommendation = businessUnitService.generateRecommendation("", startTime, endTime);
        System.out.println(recommendation);
    }
}