package edu.hcu.triage;

import java.util.Comparator;


/**
 * Comparator for PriorityQueue: higher severity first; among equals, smaller arrivalSeq first.
 * In other words, higher severity have highest priority (treated first). If multiple patients
 * have the same severity, then the patient with earlier arrivalSeq is served first (FIFO tie-breaking)
 */

public final class TriageOrder implements Comparator<Patient> {

    // Override comparison of patient1 and patient2 where -1 is returned if patient1 has higher priority,
    // 1 if patient2 has higher priority, zero if both patients have equal priority.
    @Override
    public int compare(Patient patient1, Patient patient2) {
        if (patient1 == patient2) return 0; // return 0 for patients with same object (both have same priority)
        // Null patients have least priority (null handling)
        if (patient1 == null) return 1;
        if (patient2 == null) return -1;

        // Compare severity in descending order where highest severity value has higher priority
        int severityComparison = Integer.compare(patient2.getSeverity(), patient1.getSeverity());
        if (severityComparison != 0) {
            return severityComparison;
        }
        // (Chat-GPT support) Add a tie-breaker to prioritize patient priority based on arrival
        // where earlier arrival have higher priority (FIFO handling among patients with equal severity)
        return Long.compare(patient1.getArrivalSeq(), patient2.getArrivalSeq());
    }
}
