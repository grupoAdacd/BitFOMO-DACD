package es.ulpgc.dacd.bitfomo.redditfeeder.businessunitstuff;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class SentimentAnalyzer implements SentimentAnalyzerPort {
    private static final String PYTHON_COMMAND = "python";
    private static final String SCRIPT_RELATIVE_PATH = "src/main/resources/sentiment_service.py";

    @Override
    public Double analyzeSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }

        try {
            String projectDir = System.getProperty("user.dir");
            String scriptPath = projectDir + "/" + SCRIPT_RELATIVE_PATH.replace("/", File.separator);

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

            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    System.err.println("Error del script Python: " + errorLine);
                }
            }

            process.waitFor();

            return 0.0;
        } catch (Exception e) {
            System.err.println("Error al analizar el sentimiento: " + e.getMessage());
            return 0.0;
        }
    }
}