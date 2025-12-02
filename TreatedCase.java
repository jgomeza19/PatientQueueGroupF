package edu.hcu.triage;

import java.time.Instant;

public class TreatedCase {

    public enum Outcome { STABLE, OBSERVE, TRANSFER }

    private final Patient patient;
    private final Instant start;
    private final Instant end;
    private final Outcome outcome;
    private final String notes;

    // ADDED: full constructor
    public TreatedCase(Patient patient,
                       Instant start,
                       Instant end,
                       Outcome outcome,
                       String notes) {
        this.patient = patient;
        this.start = start;
        this.end = end;
        this.outcome = outcome;
        this.notes = notes;
    }

    // ADDED: getter
    public Patient getPatient() { return patient; }
    public Instant getStart() { return start; }
    public Instant getEnd() { return end; }
    public Outcome getOutcome() { return outcome; }
    public String getNotes() { return notes; }


    @Override
    public String toString() {
        return "TreatedCase{" +
                "patient=" + patient.getId() +
                ", start=" + start +
                ", end=" + end +
                ", outcome=" + outcome +
                ", notes='" + notes + '\'' +
                '}';
    }
}
