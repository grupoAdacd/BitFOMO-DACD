package es.ulpgc.dacd.bitfomo.businessunit.infrastructure;

import java.util.Scanner;

public class CLIUserInterface {
    private final String csvPath;
    private final RecommendationCalculator calculator;

    public CLIUserInterface(String csvPath, RecommendationCalculator calculator) {
        this.csvPath = csvPath;
        this.calculator = calculator;
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = readInput(scanner);
            if (input.equalsIgnoreCase("exit")) {
                System.out.println("¡Adiós!");
                break;
            }
            processCommand(input);
        }
    }

    private String readInput(Scanner scanner) {
        System.out.println("Escribe 'help' para ver los comandos o 'exit' para salir:");
        System.out.print("> ");
        return scanner.nextLine().trim();
    }

    private void processCommand(String input) {
        if (input.equalsIgnoreCase("help")) {
            showHelp();
        } else if (input.equalsIgnoreCase("recommend")) {
            showRecommendation();
        } else {
            System.out.println("Comando inválido. Escribe 'help' para ver los comandos.");
        }
    }

    private void showHelp() {
        System.out.println("Comandos disponibles:");
        System.out.println("  recommend     - Muestra una recomendación basada en la media de las últimas 48 horas");
        System.out.println("  help          - Muestra este mensaje de ayuda");
        System.out.println("  exit          - Sale de la aplicación");
    }

    private void showRecommendation() {
        String recommendation = calculator.generateRecommendation(csvPath);
        System.out.println(recommendation);
    }
}