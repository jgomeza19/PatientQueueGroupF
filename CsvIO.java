package edu.hcu.triage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

public final class CsvIO {

    public static void loadPatients(Path csv, PatientRegistry reg) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(csv, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) {
                throw new IOException("CSV is empty: " + csv);
            }

            if (!header.trim().equalsIgnoreCase("id,name,age,severity")) {
                throw new IOException("Invalid CSV header: " + header);
            }

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue; // skip blank lines

                String[] parts = line.split(",");
                if (parts.length != 4) {
                    throw new IOException("Invalid row (wrong number of fields): " + line);
                }

                String idStr = parts[0].trim();
                String name = parts[1].trim();
                String ageStr = parts[2].trim();
                String sevStr = parts[3].trim();

                if (idStr.isEmpty() || name.isEmpty() || ageStr.isEmpty() || sevStr.isEmpty()) {
                    throw new IOException("Missing field in row: " + line);
                }

                int age, severity;
                String id = idStr; // treat CSV ID as string

                try {
                    age = Integer.parseInt(ageStr);
                    severity = Integer.parseInt(sevStr);
                } catch (NumberFormatException e) {
                    throw new IOException("Invalid number in row: " + line, e);
                }

                Patient p = reg.registerNew(id, name, age, severity);
            }
        }
    }

    public static void exportLog(Path csv, List<TreatedCase> cases) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(csv, StandardCharsets.UTF_8)) {
            writer.write("id,name,age,severity,treatedAt");
            writer.newLine();

            for (TreatedCase c : cases) {
                Patient p = c.getPatient();
                Instant ts = c.getEnd(); // using end time as treatedAt

                writer.write(
                        p.getId() + "," +
                                escape(p.getName()) + "," +
                                p.getAge() + "," +
                                p.getSeverity() + "," +
                                ts.toString()
                );
                writer.newLine();
            }
        }
    }

    private static String escape(String s) {
        if (s.contains(",") || s.contains("\"")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
