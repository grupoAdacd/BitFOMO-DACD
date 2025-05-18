package es.ulpgc.dacd.bitfomo.businessunit2.adapters;

import es.ulpgc.dacd.bitfomo.businessunit2.domain.DatamartEnsembler;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class EventFileReader {
    private final DatamartEnsembler ensembler;

    public EventFileReader(DatamartEnsembler ensembler) {
        this.ensembler = ensembler;
    }

    public void loadHistoricalEvents(String folderPath) {
        File dir = new File(folderPath);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                JSONObject json = new JSONObject(line);
                                if (folderPath.contains("RedditPost")) {
                                    ensembler.processRedditMessage(json);
                                } else if (folderPath.contains("CryptoPrice")) {
                                    ensembler.processBinanceMessage(json);
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Error loading historical events: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }
}