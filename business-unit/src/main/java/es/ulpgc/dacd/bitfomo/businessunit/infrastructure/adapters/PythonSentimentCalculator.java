package es.ulpgc.dacd.bitfomo.businessunit.infrastructure.adapters;

import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.ports.SentimentCalculator;

import java.io.*;

public class PythonSentimentCalculator implements SentimentCalculator {
    private static final String PYTHON_COMMAND = "python";
    private static final String SCRIPT_PATH = "src/main/resources/sentiment_service.py";

    public double calculateSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
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
            return null;
        }
    }

    private String getScriptPath() {
        String projectDir = System.getProperty("user.dir");
        return projectDir + File.separator + SCRIPT_PATH.replace("/", File.separator);
    }

    private void writeInput(Process process, String text) {
        try (OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream())) {
            writer.write(text);
            writer.flush();
        } catch (IOException e) {
        }
    }

    private double readOutput(Process process) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line = reader.readLine();
            process.waitFor();
            double result = line != null ? Double.parseDouble(line.trim()) : 0.0;
            return result;
        } catch (Exception e) {
            return 0.0;
        }
    }
}