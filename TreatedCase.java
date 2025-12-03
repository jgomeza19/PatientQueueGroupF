package edu.hcu.triage;

import java.time.Instant;

/**
 * TreatedCase represents ONE completed treatment event for ONE patient.
 * 
 * This object is stored inside the TreatmentLog.
 * It captures:
 *    - Which patient was treated
 *    - When treatment started
 *    - When treatment ended
 *    - What the medical outcome was (enum: STABLE, OBSERVE, TRANSFER)
 *    - Any notes written by the treating physician
 *
 * This class is IMMUTABLE: once created, its fields never change.
 * This is good for logging because treatment events shouldn't be altered afterward.
 */
public class TreatedCase {

    /**
     * Outcome is an enum representing the three possible results of a treatment:
     *  - STABLE: patient can likely be discharged soon
     *  - OBSERVE: patient needs to remain under monitoring
     *  - TRANSFER: patient must be moved to a different facility with more resources
     *
     * Using an enum keeps the values controlled and prevents invalid strings.
     */
    public enum Outcome { STABLE, OBSERVE, TRANSFER }

    // The specific patient involved in this treated case
    // This references the Patient object from the registry.
    private final Patient patient;

    // Timestamp marking when treatment began (captured at execution time)
    private final Instant start;

    // Timestamp marking when treatment ended
    private final Instant end;

    // The doctor-selected outcome category for this treatment event
    private final Outcome outcome;

    // Optional free text notes entered by the doctor
    private final String notes;

    /**
     * Full constructor to create a TreatedCase record.
     *
     * Why parameters are final:
     *   - We want the treatment event to be historically accurate
     *   - Fields should NOT change later, so constructor sets them permanently
     *
     * @param patient  The patient being treated
     * @param start    Start timestamp (Instant.now() at beginning)
     * @param end      End timestamp (Instant.now() immediately after)
     * @param outcome  Enum describing result of treatment
     * @param notes    Free text notes (blank or empty allowed)
     */
    public TreatedCase(Patient patient,
                       Instant start,
                       Instant end,
                       Outcome outcome,
                       String notes) {

        // Assign constructor arguments directly to fields.
        // No validation added — project requirement: keep it simple.
        this.patient = patient;
        this.start = start;
        this.end = end;
        this.outcome = outcome;
        this.notes = notes;
    }

    // Getter returns the Patient object associated with this treatment.
    public Patient getPatient() { return patient; }

    // Getter for start time (Instant is safe and immutable)
    public Instant getStart() { return start; }

    // Getter for end time (Instant also immutable)
    public Instant getEnd() { return end; }

    // Getter for the enum describing outcome
    public Outcome getOutcome() { return outcome; }

    // Getter for optional doctor notes
    public String getNotes() { return notes; }

    /**
     * toString method is used for printing the TreatedCase in logs and console output.
     *
     * IMPORTANT:
     *  - Only prints patient ID, NOT the full patient object, to keep output shorter.
     *  - Displays all fields in readable string form.
     *
     * This string representation is also used by CsvIO to generate readable log lines.
     */
    @Override
    public String toString() {
        return "TreatedCase{" +
                "patient=" + patient.getId() +       // print patient by ID only
                ", start=" + start +                 // treatment start timestamp
                ", end=" + end +                     // treatment end timestamp
                ", outcome=" + outcome +             // enum outcome
                ", notes='" + notes + '\'' +         // doctor's notes
                '}';
    }
}
