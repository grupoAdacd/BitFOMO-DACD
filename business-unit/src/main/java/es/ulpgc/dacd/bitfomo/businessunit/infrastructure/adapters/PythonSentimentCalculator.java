package es.ulpgc.dacd.bitfomo.businessunit.infrastructure.adapters;

import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.ports.SentimentCalculator;

import java.io.*;

public class PythonSentimentCalculator implements SentimentCalculator {
    private static final String PYTHON_COMMAND = "python";
    private static final String SCRIPT_PATH = "src/main/resources/sentiment_service.py";

    public double calculateSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            System.out.println("Texto vacío, retornando 0.0");
            return 0.0;
        }
        Process process = startProcess();
        writeInput(process, text);
        return readOutput(process);
    }

    private Process startProcess() {
        try {
            ProcessBuilder pb = new ProcessBuilder(PYTHON_COMMAND, getScriptPath());
            pb.redirectErrorStream(true);
            return pb.start();
        } catch (IOException e) {
            System.err.println("Error iniciando script de Python: " + e.getMessage());
            return null;
        }
    }

    private String getScriptPath() {
        String projectDir = System.getProperty("user.dir");
        return projectDir + File.separator + SCRIPT_PATH.replace("/", File.separator);
    }

    private void writeInput(Process process, String text) {
        try (OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream())) {
            System.out.println("Enviando texto al script: " + text);
            writer.write(text);
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error escribiendo entrada al script: " + e.getMessage());
        }
    }

    private double readOutput(Process process) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line = reader.readLine();
            process.waitFor();
            double result = line != null ? Double.parseDouble(line.trim()) : 0.0;
            System.out.println("Resultado del script: " + result);
            return result;
        } catch (Exception e) {
            System.err.println("Error leyendo salida del script: " + e.getMessage());
            return 0.0;
        }
    }
}