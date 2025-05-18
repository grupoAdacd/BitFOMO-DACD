package es.ulpgc.dacd.bitfomo.businessunit.infrastructure;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class SentimentAnalyzer {
    private static final String PYTHON_COMMAND = "python"; // O "python3"
    private static final String SCRIPT_PATH = "src/main/resources/sentiment_service.py";

    public double analyzeSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }
        try {
            String projectDir = System.getProperty("user.dir");
            String scriptPath = projectDir + File.separator + SCRIPT_PATH.replace("/", File.separator);
            ProcessBuilder pb = new ProcessBuilder(PYTHON_COMMAND, scriptPath);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            try (OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream())) {
                writer.write(text);
                writer.flush();
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                if (line != null) {
                    return Double.parseDouble(line.trim());
                }
            }
            process.waitFor();
            return 0.0;
        } catch (Exception e) {
            System.err.println("Error analyzing sentiment: " + e.getMessage());
            return 0.0;
        }
    }
}