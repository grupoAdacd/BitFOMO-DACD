package es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class LastKlineTimeManager {
    private final String filePath;
    private long lastKlineTime;

    public LastKlineTimeManager(String filePath) {
        this.filePath = filePath;
        this.lastKlineTime = -1;
    }

    public long loadLastKlineTime() {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine();
            return parseLastKlineTime(line);
        } catch (Exception e) {
            System.err.println("Error leyendo último timestamp: " + e.getMessage());
            return -1;
        }
    }

    private long parseLastKlineTime(String line) {
        if (line != null && !line.trim().isEmpty()) {
            return Long.parseLong(line.trim());
        }
        return -1;
    }

    public void saveLastKlineTime(long lastKlineTime) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(String.valueOf(lastKlineTime));
        } catch (Exception e) {
            System.err.println("Error guardando último timestamp: " + e.getMessage());
        }
    }
}