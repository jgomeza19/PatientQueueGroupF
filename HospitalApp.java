package edu.hcu.triage;

import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

/**
 * Uses:
 *   - PatientRegistry to store all patients
 *   - TriageQueue for priority queue for severity-based ordering and arrivalSeq
 *   - TreatmentLog to record state/condition of treated patients
 */
public class HospitalApp {

    private final PatientRegistry registry = new PatientRegistry();
    private final TriageQueue triage = new TriageQueue();
    private final TreatmentLog log = new TreatmentLog();
    private final Scanner in = new Scanner(System.in);    // Use scanner in order to enter patient's condition

    // Main method to run Hospital App
    public static void main(String[] args) {
        new HospitalApp().run();
    }

    /** Main control loop to drive console application with user options*/
    private void run() {
        // Simple console menu loop
        while (true) {
            printMenu();
            System.out.print("Choose: ");
            String choice = in.nextLine().trim();

            switch (choice) {

                case "1": registerPatient(); break;
                case "2": updatePatient(); break;
                case "3": enqueueForTriage(); break;
                case "4": peekNext(); break;
                case "5": admitAndTreat(); break;
                case "6": printTriageOrder(); break;
                case "7": findPatient(); break;
                case "8": showTreatmentLog(); break;
                case "9": performanceDemo(); break;
                case "10": exportLogToCsv(); break;

                case "0":
                    System.out.println("Goodbye.");
                    return;

                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    /** (1) Register a new patient */
    // Try-catch is used for exception handling whenever the console operates on one
    // of the user choices
    private void registerPatient() {
        System.out.println("---- Register New Patient ----");

        String id = prompt("ID: ");
        String name = prompt("Name: ");
        int age = promptInt("Age: ");
        int severity = promptInt("Severity (1–10): ");

        // try-block exception to handle errors and any potential errors that can print a message
        try {
            Patient p = registry.registerNew(id, name, age, severity);
            System.out.println("Registered: " + p);
        } catch (Exception e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    /** (2) Update an existing patient */
    private void updatePatient() {
        System.out.println("---- Update Patient ----");

        String id = prompt("ID: ");
        Optional<Patient> updated = registry.updateExisting(
                id,
                promptAllowBlank("New name (blank = no change): "),
                promptIntAllowBlank("New age (blank = no change): "),
                promptIntAllowBlank("New severity (blank = no change): ")
        );

        if (updated.isPresent()) System.out.println("Patient updated.");
        else System.out.println("Patient ID not found.");
    }

    /** (3) Enqueue patient by ID into the triage queue */
    private void enqueueForTriage() {
        String id = prompt("Enter patient ID to enqueue: ");
        boolean ok = triage.enqueueById(registry, id);

        if (ok) System.out.println("Added to triage.");
        else System.out.println("No such ID.");
    }

    /** (4) Peek at next patient */
    private void peekNext() {
        Optional<Patient> p = triage.peekNext();
        if (p.isPresent()) System.out.println("Next: " + p.get());
        else System.out.println("Triage empty.");
    }

    /** (5) Admit and treat next patient */
    private void admitAndTreat() {
        Optional<Patient> pOpt = triage.dequeueNext();
        if (pOpt.isEmpty()) {
            System.out.println("Queue is empty.");
            return;
        }

        Patient p = pOpt.get();
        System.out.println("Treating: " + p);

        Instant start = Instant.now();
        Instant end = Instant.now(); // In real app, you'd simulate duration

        // Ask doctor for outcome
        TreatedCase.Outcome outcome = askOutcome();
        String notes = prompt("Notes: ");

        TreatedCase tc = new TreatedCase(p, start, end, outcome, notes);
        log.append(tc);

        System.out.println("Treatment logged.");
    }

    /** (6) Print snapshot of triage order for treatment log*/
    private void printTriageOrder() {
        List<Patient> list = triage.snapshotOrder();
        System.out.println("---- Triage Order ----");
        list.forEach(System.out::println);
    }

    /** (7) Find a patient by ID */
    private void findPatient() {
        String id = prompt("ID: ");
        Optional<Patient> p = registry.get(id);

        if (p.isPresent()) System.out.println(p.get());
        else System.out.println("Not found.");
    }

    /** (8) Show treatment history */
    private void showTreatmentLog() {
        System.out.println("1) Oldest → Newest");
        System.out.println("2) Newest → Oldest");
        String c = prompt("Choose: ");

        List<TreatedCase> cases =
                c.equals("2") ? log.asListNewestFirst() : log.asListOldestFirst();

        System.out.println("---- Treatment Log ----");
        for (TreatedCase t : cases) {
            System.out.println(t);
        }
    }

    /** (9) Performance demo using SampleWorkloads */
    private void performanceDemo() {
        System.out.println("---- Performance Demo ----");

        int n = promptInt("How many patients to enqueue? ");
        int k = promptInt("How many dequeues? ");

        SampleWorkloads workloads = new SampleWorkloads(
                12345L,
                SampleWorkloads.SeverityDistribution.UNIFORM
        );

        try (PerfTimer t = new PerfTimer("Enqueue N")) {
            workloads.enqueueRandomPatients(n, registry, triage);
        }

        try (PerfTimer t = new PerfTimer("Dequeue K")) {
            workloads.performDequeues(k, triage);
        }
    }

    /** (10) Export treatment log to CSV */
    private void exportLogToCsv() {
        String path = prompt("CSV file name to export to: ");
        Path file = Path.of(path);

        try {
            CsvIO.exportLog(file, log.asListOldestFirst());
            System.out.println("Exported to: " + file);
        } catch (Exception e) {
            System.out.println("Export failed: " + e.getMessage());
        }
    }

    // Menu printing
    private void printMenu() {
        System.out.println();
        System.out.println("========= Hospital Menu =========");
        System.out.println("1) Register patient");
        System.out.println("2) Update patient");
        System.out.println("3) Enqueue for triage");
        System.out.println("4) Peek next");
        System.out.println("5) Admit & Treat next");
        System.out.println("6) Print triage snapshot");
        System.out.println("7) Find patient");
        System.out.println("8) Show treatment log");
        System.out.println("9) Performance demo");
        System.out.println("10) Export log to CSV");
        System.out.println("0) Exit");
        System.out.println("=================================");
    }

    // Input helper method (Chat-GPT support)
    private String prompt(String msg) {
        System.out.print(msg);
        return in.nextLine().trim();
    }

    private String promptAllowBlank(String msg) {
        System.out.print(msg);
        String s = in.nextLine().trim();
        return s.isEmpty() ? null : s;
    }

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

    private Integer promptIntAllowBlank(String msg) {
        System.out.print(msg);
        String s = in.nextLine().trim();
        if (s.isEmpty()) return null;

        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return null;
        }
    }

    // Choose STABLE / OBSERVE / TRANSFER according to TreatedCase java code
    private TreatedCase.Outcome askOutcome() {
        while (true) {
            System.out.println("Outcome: 1) STABLE  2) OBSERVE  3) TRANSFER");
            String s = prompt("Choose: ");

            switch (s) {
                case "1": return TreatedCase.Outcome.STABLE;
                case "2": return TreatedCase.Outcome.OBSERVE;
                case "3": return TreatedCase.Outcome.TRANSFER;
                default: System.out.println("Invalid.");
            }
        }
    }
}
