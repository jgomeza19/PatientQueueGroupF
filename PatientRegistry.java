package edu.hcu.triage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Handles storing patients and assigning unique arrival sequence numbers.
 * This class mainly keeps a HashMap for quick lookups and is responsible
 * for creating NEW Patient objects.
 */
public class PatientRegistry {
    // Stores patients by their ID for fast retrieval
    private final Map<String, Patient> byId = new HashMap<>();

    // Counter used to assign arrival sequence numbers in the order patients register
    private long nextArrivalSeq = 0L;

    /**
     * Register a NEW patient.
     * Steps:
     * 1) Capture current time as their arrival timestamp.
     * 2) Assign a unique arrivalSeq (monotonic increasing number).
     * 3) Create a new Patient object.
     * 4) Store it in the HashMap by its ID.
     */
    public synchronized Patient registerNew(String id, String name, int age, int severity) {
        long seq = nextArrivalSeq++;            // assign sequence then increment counter

        // Create the patient with the assigned sequence
        Patient p = new Patient(id, name, age, severity, seq);

        // Save to map (replace if ID already existed)
        byId.put(p.getId(), p);

        return p;  // return full patient object
    }

    /**
     * Update an EXISTING patient.
     * Steps:
     * 1) Look up the patient by ID.
     * 2) If found, update only the fields that were provided (non-null).
     * 3) Return Optional containing the updated patient.
     */
    public synchronized Optional<Patient> updateExisting(String id, String name, Integer age, Integer severity) {
        Patient p = byId.get(id);  // try to find the patient
        if (p == null) return Optional.empty();

        // Update name only if a new one was given
        if (name != null) p.setName(name);

        // Update age only if provided
        if (age != null) p.setAge(age);

        // Update severity only if provided
        if (severity != null) p.setSeverity(severity);

        return Optional.of(p);
    }

    /**
     * Retrieve a patient by ID quickly.
     */
    public synchronized Optional<Patient> get(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    /**
     * Check if a patient ID exists in the registry.
     */
    public synchronized boolean contains(String id) {
        return byId.containsKey(id);
    }

    /**
     * Return how many patients have ever been registered.
     */
    public synchronized int size() {
        return byId.size();
    }
}
