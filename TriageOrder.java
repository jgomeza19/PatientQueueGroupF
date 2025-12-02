package edu.hcu.triage;

import java.util.Comparator;


/**

 * Comparator for PriorityQueue: higher severity first; among equals, smaller arrivalSeq first.

 */

public final class TriageOrder implements Comparator<Patient> {

    // Override comparison of patient1 and patient2
    // LATER NOTE: Test to Ensure the comparator is CONSISTENT WITH EQUALS expectations for PriorityQueue usage.
    @Override
    public int compare(Patient patient1, Patient patient2) {
        if (patient1 == patient2) return 0; // return 0 for patients with same object
        if (patient1 == null) return 1; // set nulls to last
        if (patient2 == null) return -1;

        // Compare severity in descending order where highest severity value has higher priority
        int severityComparison = Integer.compare(patient2.getSeverity(), patient1.getSeverity());
        if (severityComparison != 0) {
            return severityComparison;
        }
        // Chat-GPT suggested to add a tie-breaker to prioritize patient priority based on arrival
        // where earlier arrival have higher priority
        return Long.compare(patient1.getArrivalSeq(), patient2.getArrivalSeq());
    }
}
