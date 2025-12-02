// Declare project for organizing related project files
package edu.hcu.triage;

// Import for timestamp recording
import java.time.Instant;

// Import for equals() and hashcode() objects
import java.util.Objects;

/**
 * Immutable identity (id) + mutable clinical state (severity).
 * Arrival order must be trackable for stable tie-breaking.
 * Arrival timestamp and sequence number follows FIFO (queue data structure).
 */

public class Patient {
    private final String id;        // Unique patient ID
    private String name;            // mutable string for patient's name
    private int age;                // mutable string for patient's age
    private int severity;           // define scale (1 of least concern - 10 requiring critical care): higher = more urgent
    private final Instant arrival;  // registration time
    private final long arrivalSeq;  // monotonically increasing sequence for FIFO ties for stable tie-breaking

    // Define Constructors with basic validation with safe defaults and no exceptions (Chat-GPT for constructor only)
    public Patient(String id,String name, int age, int severity, long arrivalSeq) {
        // Assign unique ID or default to "No Id" if patient has no ID
        this.id = (id != null && !id.isBlank()) ? id : "No Id";
        // Assign name or default to "No name" if patient name is empty
        this.name = (name != null && !name.isBlank()) ? name : "No Name";
        // Set age default to 0
        this.age = (age >= 0) ? age : 0;
        // Severity is set to 1 by default and ensures it's between 1 and 10
        this.severity = (severity >= 1 && severity <= 10) ? severity : 1;
        // Create time for patient object
        this.arrival = Instant.now();
        // Set the sequence number for ordering patients in triage queue
        this.arrivalSeq = arrivalSeq;
    }
    // the ? is a Conditional (Ternary) Operator is a shorthand operation for an if-then-else states that is used with
    // colon conjunction. The ? syntax is: condition ? valueIfTrue : valueIfFalse

    // Define read-only getters
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public int getAge() {
        return age;
    }
    public int getSeverity() {
        return severity;
    }
    public Instant getArrival() {
        return arrival;
    }
    public long getArrivalSeq() {
        return arrivalSeq;
    }

    // Setter to update names if it is not null and not blank
    public void setName(String name) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
    }
    // Setter to change patient's age if it's greater than or equal to 0
    public void setAge(int age) {
        if (age >= 0) {
            this.age = age;
        }
    }
    // setter to update patient severity from 1-10
    public void setSeverity(int severity) {
        if (severity >= 1 && severity <= 10) {
            this.severity = severity;
        }
    }

    // Patients are equal if they have the same ID; 
    // this is to ensure that each patient have their unique ID when
    // stored in HashMap and other collections (Chat-GPT support).
    @Override
    public boolean equals(Object o) {
        if (this == o) return false;    // Same object
        if (!(o instanceof Patient)) return false;    // If not a patient, they're not equal
        Patient other = (Patient) o;
        return Objects.equals(id, other.id);    // Compare patient IDs
    }

    // Run Hash code based only on patient ID when thay match the equals() method condition
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // SHOW toString() concise of a readable patient data summary
    @Override
    public String toString() {
        return String.format("Patient{id='%s', name='%s', age=%d, severity=%d, arrivalSeq=%d}",
                id, name, age, severity, arrivalSeq);
    }
}
