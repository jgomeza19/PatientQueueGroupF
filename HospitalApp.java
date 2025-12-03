package edu.hcu.triage;

import java.nio.file.Path;    // Used for accessing filesystem locations when exporting/importing CSV files
import java.time.Instant;     // Used to timestamp treatment start and end times
import java.util.*;           // Provides List, Optional, Scanner, etc.

/**
 * HospitalApp is the MAIN DRIVER of the entire triage system.
 *
 * This class ties together all core components:
 *
 *   - PatientRegistry  → stores all registered patients
 *   - TriageQueue      → priority queue for managing which patient is treated next
 *   - TreatmentLog     → records all completed treatments
 *
 * It provides a simple, text-based menu that allows the user to:
 *
 *   1) register patients
 *   2) update patient information
 *   3) add patients to the triage queue
 *   4) peek at next patient
 *   5) treat next patient (removes from queue and adds to log)
 *   6) print triage ordering
 *   7) search for a patient by ID
 *   8) show the treatment log
 *   9) run a performance test
 *   10) export log to CSV
 *   11) import patient data from CSV
 *
 * ALL user input and system coordination happens in this class.
 * ALL actual data handling happens in the other classes.
 */
public class HospitalApp {

    // PatientRegistry holds ALL registered patients by ID
    private final PatientRegistry registry = new PatientRegistry();

    // TriageQueue uses a PriorityQueue + custom comparator to rank patients
    private final TriageQueue triage = new TriageQueue();

    // TreatmentLog stores TreatedCase entries in chronological order
    private final TreatmentLog log = new TreatmentLog();

    // Scanner used for reading user input in the console
    private final Scanner in = new Scanner(System.in);

    /**
     * main(...) is the official starting point of the program.
     * It simply creates a new HospitalApp instance and calls run().
     *
     * This keeps main() clean and keeps all logic inside run().
     */
    public static void main(String[] args) {
        new HospitalApp().run();      // create app instance + start main loop
    }

    /**
     * The run() method implements the MAIN MENU LOOP.
     *
     * As long as the user doesn't choose "0" to exit,
     * this loop keeps showing the menu and performing selected actions.
     *
     * This is a classic "while(true)" system menu pattern.
     */
    private void run() {

        while (true) {                       // infinite loop until "return"

            printMenu();                     // display the menu text

            System.out.print("Choose: ");    // prompt for input
            String choice = in.nextLine().trim();   // read user input and strip whitespace

            // Switch statement handles every numbered menu choice
            switch (choice) {

                case "1": registerPatient(); break;       // option 1: register new patient
                case "2": updatePatient(); break;         // option 2: update info
                case "3": enqueueForTriage(); break;      // option 3: add patient to triage queue
                case "4": peekNext(); break;              // option 4: show but don't remove next patient
                case "5": admitAndTreat(); break;         // option 5: treat next patient
                case "6": printTriageOrder(); break;      // option 6: snapshot of priority queue
                case "7": findPatient(); break;           // option 7: look up patient by ID
                case "8": showTreatmentLog(); break;      // option 8: show treatment history
                case "9": performanceDemo(); break;       // option 9: run performance test
                case "10": exportLogToCsv(); break;       // option 10: export log
                case "11": importFromCsv(); break;        // option 11: import patients

                case "0":                                 // exit condition
                    System.out.println("Goodbye.");
                    return;                                // leave run(), ending program

                default:
                    System.out.println("Invalid choice."); // if user typed something random
            }
        }
    }

    /* =====================================================
     *                  MENU OPTION METHODS
     * ===================================================== */

    /**
     * OPTION 1 — Register a new patient.
     *
     * Prompts user for:
     *   - ID
     *   - name
     *   - age
     *   - severity
     *
     * Calls registry.registerNew(...) which may throw an exception
     * if the ID already exists (based on your choice B).
     */
    private void registerPatient() {
        System.out.println("---- Register New Patient ----");

        String id = prompt("ID: ");                 // get patient ID
        String name = prompt("Name: ");             // get name
        int age = promptInt("Age: ");               // validated integer input
        int severity = promptInt("Severity (1–10): ");

        try {
            Patient p = registry.registerNew(id, name, age, severity);
            System.out.println("Registered: " + p); // prints full patient object
        } catch (Exception e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    /**
     * OPTION 2 — Update existing patient info.
     *
     * Users may update:
     *   - name
     *   - age
     *   - severity
     *
     * Leaving the entry blank means "do not change this field".
     *
     * Uses Optional to indicate success or failure.
     */
    private void updatePatient() {
        System.out.println("---- Update Patient ----");

        String id = prompt("ID: ");

        // Passing null for unchanged fields
        Optional<Patient> updated = registry.updateExisting(
                id,
                promptAllowBlank("New name (blank = no change): "),
                promptIntAllowBlank("New age (blank = no change): "),
                promptIntAllowBlank("New severity (blank = no change): ")
        );

        if (updated.isPresent())
            System.out.println("Patient updated.");
        else
            System.out.println("Patient not found.");
    }

    /**
     * OPTION 3 — Add an existing patient into the triage queue.
     *
     * Uses `enqueueById(...)` so we do not need the Patient object.
     */
    private void enqueueForTriage() {
        String id = prompt("Enter patient ID to enqueue: ");

        boolean ok = triage.enqueueById(registry, id);

        if (ok) System.out.println("Added to queue.");
        else System.out.println("No such patient ID.");
    }

    /**
     * OPTION 4 — Peek at the next patient WITHOUT removing them.
     *
     * Uses Optional because the queue could be empty.
     */
    private void peekNext() {
        Optional<Patient> p = triage.peekNext();

        if (p.isPresent())
            System.out.println("Next: " + p.get());
        else
            System.out.println("Triage queue empty.");
    }

    /**
     * OPTION 5 — Treat the next patient.
     *
     * Steps:
     *   1) dequeueNext() removes highest-priority patient
     *   2) gather treatment outcome + notes
     *   3) timestamp the start and end (simple Instant.now())
     *   4) create a TreatedCase object
     *   5) append it to the TreatmentLog
     */
    private void admitAndTreat() {

        // Try removing the next patient from queue
        Optional<Patient> pOpt = triage.dequeueNext();

        if (pOpt.isEmpty()) {
            System.out.println("Queue empty.");
            return;
        }

        Patient p = pOpt.get();             // actual patient removed from queue
        System.out.println("Treating: " + p);

        Instant start = Instant.now();      // treatment start timestamp
        Instant end = Instant.now();        // treatment end timestamp

        TreatedCase.Outcome outcome = askOutcome();  // doctor selects outcome
        String notes = prompt("Notes: ");            // optional free text

        // Create a new treatment record
        TreatedCase tc = new TreatedCase(p, start, end, outcome, notes);

        // Add it to the treatment log
        log.append(tc);

        System.out.println("Treatment logged.");
    }

    /**
     * OPTION 6 — Show triage ordering using snapshotOrder().
     *
     * This does NOT modify queue — it creates a copy and sorts it.
     */
    private void printTriageOrder() {
        System.out.println("---- Triage Order ----");

        List<Patient> list = triage.snapshotOrder();

        // Print each patient in order
        list.forEach(System.out::println);
    }

    /**
     * OPTION 7 — Look up a patient in the registry by ID.
     *
     * Shows Optional usage again.
     */
    private void findPatient() {
        String id = prompt("ID: ");

        Optional<Patient> p = registry.get(id);

        if (p.isPresent())
            System.out.println(p.get());
        else
            System.out.println("Not found.");
    }

    /**
     * OPTION 8 — Display treatment log.
     *
     * User chooses:
     *   1 → Oldest first
     *   2 → Newest first
     */
    private void showTreatmentLog() {

        System.out.println("1) Oldest → Newest");
        System.out.println("2) Newest → Oldest");

        String c = prompt("Choose: ");

        List<TreatedCase> list =
                c.equals("2") ? log.asListNewestFirst()
                              : log.asListOldestFirst();

        System.out.println("---- Treatment Log ----");

        for (TreatedCase t : list)
            System.out.println(t);
    }

    /**
     * OPTION 9 — Performance demo.
     *
     * Uses SampleWorkloads to:
     *   - enqueue N random patients
     *   - dequeue K patients
     *
     * PerfTimer measures the time taken for each stage.
     */
    private void performanceDemo() {

        System.out.println("---- Performance Demo ----");

        int n = promptInt("How many patients to enqueue? ");
        int k = promptInt("How many dequeues? ");

        SampleWorkloads workloads = new SampleWorkloads(
                12345L,                                    // fixed seed for repeatable randomness
                SampleWorkloads.SeverityDistribution.UNIFORM
        );

        try (PerfTimer t = new PerfTimer("Enqueue N")) {
            workloads.enqueueRandomPatients(n, registry, triage);
        }

        try (PerfTimer t = new PerfTimer("Dequeue K")) {
            workloads.performDequeues(k, triage);
        }
    }

    /**
     * OPTION 10 — export treatment log to CSV.
     *
     * Uses CsvIO.exportLog(...)
     */
    private void exportLogToCsv() {

        String path = prompt("CSV file name: ");
        Path file = Path.of(path);    // convert string to a Path object

        try {
            CsvIO.exportLog(file, log.asListOldestFirst());
            System.out.println("Exported to " + file);
        } catch (Exception e) {
            System.out.println("Export failed: " + e.getMessage());
        }
    }

    /**
     * OPTION 11 — load patients from CSV file.
     *
     * Uses CsvIO.loadPatients(...)
     */
    private void importFromCsv() {

        String path = prompt("CSV file name: ");
        Path file = Path.of(path);

        try {
            CsvIO.loadPatients(file, registry);
            System.out.println("Loaded patients from " + file);
        } catch (Exception e) {
            System.out.println("Import failed: " + e.getMessage());
        }
    }

    /* =====================================================
     *               INPUT UTILITY METHODS
     * ===================================================== */

    /**
     * Simple input helper that prints a message and returns trimmed input.
     */
    private String prompt(String msg) {
        System.out.print(msg);
        return in.nextLine().trim();
    }

    /**
     * Same as prompt, but blank input returns null.
     * Used for updateExisting() where blank means "do not change".
     */
    private String promptAllowBlank(String msg) {
        System.out.print(msg);
        String s = in.nextLine().trim();
        return s.isEmpty() ? null : s;
    }

    /**
     * Reads a REQUIRED integer — re-prompts until input is valid.
     */
    private int promptInt(String msg) {
        while (true) {
            System.out.print(msg);

            try {
                return Integer.parseInt(in.nextLine().trim());
            } catch (Exception e) {
                System.out.println("Enter a valid integer.");
            }
        }
    }

    /**
     * Reads an OPTIONAL integer — returns null if user input blank line.
     */
    private Integer promptIntAllowBlank(String msg) {
        System.out.print(msg);
        String s = in.nextLine().trim();

        if (s.isEmpty()) return null;   // blank = "do not change"

        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Prompts the doctor for a treatment outcome.
     *
     * Loops until a valid choice (1–3) is entered.
     *
     * Returns the corresponding enum constant.
     */
    private TreatedCase.Outcome askOutcome() {

        while (true) {
            System.out.println("Outcome: 1) STABLE  2) OBSERVE  3) TRANSFER");
            String s = prompt("Choose: ");

            switch (s) {
                case "1": return TreatedCase.Outcome.STABLE;
                case "2": return TreatedCase.Outcome.OBSERVE;
                case "3": return TreatedCase.Outcome.TRANSFER;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    /* =====================================================
     *                   MENU PRINTING
     * ===================================================== */

    /**
     * Simple method to print menu layout.
     * Keeping it separate improves readability.
     */
    private void printMenu() {
        System.out.println();
        System.out.println("========= Hospital Menu =========");
        System.out.println("1) Register patient");
        System.out.println("2) Update patient");
        System.out.println("3) Enqueue patient");
        System.out.println("4) Peek next");
        System.out.println("5) Admit & Treat next");
        System.out.println("6) Print triage order");
        System.out.println("7) Find patient");
        System.out.println("8) Show treatment log");
        System.out.println("9) Performance demo");
        System.out.println("10) Export log to CSV");
        System.out.println("11) Import patients from CSV");
        System.out.println("0) Exit");
        System.out.println("=================================");
    }
}
