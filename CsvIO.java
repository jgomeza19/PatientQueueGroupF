package edu.hcu.triage;

import java.io.BufferedReader;       // BufferedReader used for efficient line-by-line reading
import java.io.BufferedWriter;       // BufferedWriter used for efficient line-by-line writing
import java.io.IOException;          // Required for methods that can throw I/O errors
import java.nio.charset.StandardCharsets;  // Ensures consistent text encoding (UTF-8)
import java.nio.file.Files;          // Provides file read/write utilities
import java.nio.file.Path;           // Represents filesystem paths
import java.time.Instant;            // Used for timestamps in exported CSV
import java.util.List;               // Used when exporting a list of TreatedCase objects

/**
 * CsvIO is a utility (helper) class that handles reading and writing CSV files.
 *
 * CSV stands for "Comma Separated Values" — a common format for storing table-like data.
 *
 * This class supports two main operations:
 *
 *    1. loadPatients(...) – imports registered patients from a CSV into your PatientRegistry
 *    2. exportLog(...)    – exports treatment cases into a CSV for reporting or external use
 *
 * This class is "final" because:
 *    - It's a pure utility class
 *    - It is not intended to be subclassed
 *
 * All methods are static because:
 *    - No instance fields required
 *    - Useful for direct calling: CsvIO.loadPatients(...)
 */
public final class CsvIO {

    /**
     * Reads a CSV file containing patient information and loads it into the provided registry.
     *
     * EXPECTED FORMAT OF THE CSV FILE:
     *
     *      id,name,age,severity
     *      P001,John Doe,32,4
     *      P002,Jane Smith,44,2
     *
     * The first row MUST be the exact header string "id,name,age,severity".
     *
     * Why require a header?
     *    - Makes the CSV self-documenting
     *    - Prevents accidentally loading wrong-format files
     *    - Helps avoid confusion if external software exports CSV differently
     *
     * This method throws IOException if:
     *    - the file is missing
     *    - the header does not match
     *    - any row is malformed
     */
    public static void loadPatients(Path csv, PatientRegistry reg) throws IOException {

        // try-with-resources automatically closes the reader
        try (BufferedReader reader =
                     Files.newBufferedReader(csv, StandardCharsets.UTF_8)) {

            // First line of the CSV should contain the header
            String header = reader.readLine();

            // If the file is empty, header will be null; this is considered invalid
            if (header == null) {
                throw new IOException("CSV is empty: " + csv);
            }

            // The project expects the exact header: id,name,age,severity
            // trim() removes extra whitespace around the line
            if (!header.trim().equalsIgnoreCase("id,name,age,severity")) {

                // Throwing IOException stops loading and reports the issue
                throw new IOException("Invalid CSV header: " + header);
            }

            // Now process the file line by line
            String line;
            while ((line = reader.readLine()) != null) {  // reads next line; null means EOF

                // Remove leading/trailing whitespace, ignore empty lines
                line = line.trim();
                if (line.isEmpty()) continue;  // skip blank lines safely

                // Split each row by commas — expecting EXACTLY 4 fields
                String[] parts = line.split(",");

                // If the row does not contain 4 values, it is malformed
                if (parts.length != 4) {
                    throw new IOException("Invalid row (wrong number of fields): " + line);
                }

                // Extract each field and trim whitespace to avoid format issues
                String idStr = parts[0].trim();
                String name = parts[1].trim();
                String ageStr = parts[2].trim();
                String sevStr = parts[3].trim();

                // Validate no empty fields
                if (idStr.isEmpty() || name.isEmpty() ||
                    ageStr.isEmpty() || sevStr.isEmpty()) {
                    throw new IOException("Missing field in row: " + line);
                }

                // Declare local variables for parsed integers
                int age;
                int severity;

                // ID stored as string — not parsed into int
                String id = idStr;

                try {
                    // Convert age and severity from text to integers
                    age = Integer.parseInt(ageStr);
                    severity = Integer.parseInt(sevStr);

                } catch (NumberFormatException e) {
                    // If parsing fails (e.g., "twenty"), throw a new IOException
                    throw new IOException("Invalid number in row: " + line, e);
                }

                // Call registerNew to create a Patient and add to registry
                // Note: If duplicate ID occurs, PatientRegistry throws an exception
                Patient p = reg.registerNew(id, name, age, severity);
            }
        }
    }

    /**
     * Exports a list of TreatedCase objects into a CSV file.
     *
     * EXPECTED OUTPUT FORMAT:
     *
     *      id,name,age,severity,treatedAt
     *      P001,"John Doe",32,4,2025-01-02T23:31:44.672Z
     *
     * Why export treatment logs?
     *    - For hospital records
     *    - To share data between systems
     *    - For class assignment (requirement)
     *
     * Notes:
     *   - The CSV always uses UTF-8 encoding for universal compatibility.
     *   - The timestamp uses ISO-8601 standard (Instant.toString()).
     */
    public static void exportLog(Path csv, List<TreatedCase> cases) throws IOException {

        // Try-with-resources ensures the file writer is closed automatically
        try (BufferedWriter writer =
                     Files.newBufferedWriter(csv, StandardCharsets.UTF_8)) {

            // First write the header row for clarity and consistency
            writer.write("id,name,age,severity,treatedAt");
            writer.newLine(); // end of header line

            // Now export each TreatedCase in order
            for (TreatedCase c : cases) {

                // Get associated patient object
                Patient p = c.getPatient();

                // We use end timestamp as the treatedAt time
                Instant ts = c.getEnd();

                // Write out each column separated by commas
                // NOTE: name may require escaping if it contains commas or quotes
                writer.write(
                        p.getId() + "," +                  // patient ID
                        escape(p.getName()) + "," +        // name (escaped if needed)
                        p.getAge() + "," +                 // patient's age
                        p.getSeverity() + "," +            // severity at time of treatment
                        ts.toString()                      // ISO-8601 timestamp
                );

                // After each row, write a newline
                writer.newLine();
            }
        }
    }

    /**
     * Escapes a string for safe CSV output.
     *
     * Why needed?
     *    - If a string contains a comma, CSV programs interpret it as column separator.
     *    - If it contains quotes, it must be escaped by doubling them (" becomes "").
     *
     * Example:
     *    Original:  John "The Boss", Doe
     *    Output:   "John ""The Boss"", Doe"
     *
     * This method applies minimal required CSV escaping rules.
     */
    private static String escape(String s) {

        // Case 1: If the string contains a comma or a quote → must be wrapped in quotes
        if (s.contains(",") || s.contains("\"")) {

            // Replace " with "" to escape it
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }

        // Case 2: No escaping needed → return unchanged
        return s;
    }
}
