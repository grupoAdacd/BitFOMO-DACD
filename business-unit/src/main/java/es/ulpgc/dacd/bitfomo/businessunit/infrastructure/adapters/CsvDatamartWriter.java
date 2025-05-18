package es.ulpgc.dacd.bitfomo.businessunit.infrastructure.adapters;

import es.ulpgc.dacd.bitfomo.businessunit.domain.DatamartEntry;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.ports.DatamartWriter;

import java.io.File;
import java.io.FileWriter;
import java.util.Locale;

public class CsvDatamartWriter implements DatamartWriter {
    private final String csvPath;
    private final String header = "ts,sentiment,openPrice,closePrice\n";

    public CsvDatamartWriter(String csvPath) {
        this.csvPath = csvPath;
    }

    public void writeEntry(DatamartEntry entry) {
        File file = new File(csvPath);
        boolean writeHeader = !file.exists() || file.length() == 0;
        appendToFile(formatEntry(entry), writeHeader);
    }

    private String formatEntry(DatamartEntry entry) {
        return String.format(Locale.US, "%d,%.2f,%.2f,%.2f%n",
                entry.ts().toEpochMilli(), entry.sentiment(), entry.openPrice(), entry.closePrice());
    }

    private void appendToFile(String line, boolean writeHeader) {
        try (FileWriter writer = new FileWriter(csvPath, true)) {
            if (writeHeader) writer.append(header);
            writer.append(line);
        } catch (Exception e) {
            System.err.println("Error escribiendo en CSV: " + e.getMessage());
        }
    }
}