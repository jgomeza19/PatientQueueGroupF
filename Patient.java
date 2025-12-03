package edu.hcu.triage;

import java.time.Instant;     // Instant is used to record an exact timestamp, precise to nanoseconds
import java.util.Objects;     // Used to correctly implement equals() and hashCode()

/**
 * Patient represents ONE individual who is registered in the hospital system.
 *
 * This class mixes:
 *    - Immutable identity data (ID, arrivalSeq, arrival timestamp)
 *    - Mutable clinical data (name, age, severity)
 *
 * IMPORTANT DESIGN POINT:
 *    - ID uniquely identifies a patient permanently.
 *    - arrivalSeq is used for tie-breaking in priority queue.
 *    - severity is mutable so it can be updated after registration.
 */
public class Patient {

    /** 
     * The **unique identifier** for the patient.
     *
     * This MUST be unique across the entire registry.
     * It is a final field because identity never changes.
     */
    private final String id;

    /**
     * The patient's current name.
     * This is mutable because names can be updated.
     */
    private String name;

    /**
     * The patient's age.
     * Mutable so hospital staff can correct mistakes (e.g., typo).
     */
    private int age;

    /**
     * The patient's severity score, representing how urgent their case is.
     * Higher severity → higher priority in triage queue.
     * Mutable because a patient's condition can change.
     */
    private int severity;

    /**
     * Timestamp marking EXACTLY when the patient object was created.
     * This is final because arrival time never changes.
     */
    private final Instant arrival;

    /**
     * A monotonically increasing sequence number assigned on registration.
     *
     * Purpose:
     *    - Used for FIFO ordering when severity ties occur.
     *    - If two patients have same severity, lower arrivalSeq = higher priority.
     *
     * This is final because once a patient registers, their order never changes.
     */
    private final long arrivalSeq;

    /**
     * Patient constructor.
     *
     * This constructor applies **basic validation** while not throwing errors,
     * because your early design intention was "safe defaults instead of exceptions."
     *
     * @param id          Unique patient ID, required (falls back to "No Id" if invalid)
     * @param name        Patient name (defaults to "No Name" if blank)
     * @param age         Patient age (minimum allowed = 0)
     * @param severity    Severity score (must be 1–10; defaults to 1)
     * @param arrivalSeq  The sequence number assigned by PatientRegistry
     */
    public Patient(String id, String name, int age, int severity, long arrivalSeq) {

        // Validate ID: if null or blank, assign placeholder
        // Using ternary for compact conditional assignment
        this.id = (id != null && !id.isBlank()) ? id : "No Id";

        // Validate name: fallback if empty or null
        this.name = (name != null && !name.isBlank()) ? name : "No Name";

        // Age must be >= 0; otherwise default to 0
        this.age = (age >= 0) ? age : 0;

        // Severity must be between 1 and 10; otherwise fallback to 1
        this.severity = (severity >= 1 && severity <= 10) ? severity : 1;

        // Capture the precise time this patient was created
        this.arrival = Instant.now();

        // Store the arrival sequence provided by PatientRegistry
        this.arrivalSeq = arrivalSeq;
    }

    /* =====================================================================
     *                             GETTERS
     * ===================================================================== */

    /** Returns the unique patient ID. */
    public String getId() { return id; }

    /** Returns the patient's current name. */
    public String getName() { return name; }

    /** Returns the patient's age. */
    public int getAge() { return age; }

    /** Returns the severity score (1–10). */
    public int getSeverity() { return severity; }

    /** Returns the timestamp when the patient was registered. */
    public Instant getArrival() { return arrival; }

    /** Returns the FIFO sequence number assigned at registration time. */
    public long getArrivalSeq() { return arrivalSeq; }

    /* =====================================================================
     *                             SETTERS
     * ===================================================================== */

    /**
     * Updates the patient's name ONLY if the new value is non-null and non-empty.
     * This prevents overwriting good data with blank strings.
     */
    public void setName(String name) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
    }

    /**
     * Update patient age only if it is valid (>= 0).
     * Negative ages are ignored.
     */
    public void setAge(int age) {
        if (age >= 0) {
            this.age = age;
        }
    }

    /**
     * Update severity ONLY if the value is within 1–10.
     * Out-of-range values are ignored.
     */
    public void setSeverity(int severity) {
        if (severity >= 1 && severity <= 10) {
            this.severity = severity;
        }
    }

    /* =====================================================================
     *                       equals() AND hashCode()
     * ===================================================================== */

    /**
     * equals(Object) determines whether two Patient objects refer to the SAME PERSON.
     *
     * In this project:
     *    - Two patients are considered equal if they have the same ID.
     *    - ID is final and unique.
     *
     * WARNING: You originally had a bug where the line incorrectly returned false
     * when `this == o` — which would make objects not equal to themselves.
     *
     * This has now been corrected and the original bug line commented out.
     */
    @Override
    public boolean equals(Object o) {

        // if (this == o) return false;   // ❌ WRONG — identical objects should return true
        if (this == o) return true;        // ✔ FIXED

        if (!(o instanceof Patient)) return false;

        Patient other = (Patient) o;

        // If IDs match, patients are considered equal
        return Objects.equals(id, other.id);
    }

    /**
     * hashCode() MUST match equals().
     * Since equals is based ONLY on ID, hashCode must also use ONLY ID.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /* =====================================================================
     *                             toString()
     * ===================================================================== */

    /**
     * Creates a clean, readable string representation of the Patient.
     *
     * Includes:
     *    - ID
     *    - name
     *    - age
     *    - severity
     *    - arrivalSeq
     *
     * The timestamp is intentionally omitted for cleaner output.
     */
    @Override
    public String toString() {
        return String.format(
                "Patient{id='%s', name='%s', age=%d, severity=%d, arrivalSeq=%d}",
                id, name, age, severity, arrivalSeq
        );
    }
}
