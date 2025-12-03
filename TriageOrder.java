package edu.hcu.triage;

import java.util.Comparator;

/**
 * ========================================================================
 *                          TriageOrder.java
 * ========================================================================
 * PURPOSE OF THIS CLASS:
 * ----------------------
 * This class defines **how two Patient objects should be compared** when
 * placed inside the PriorityQueue in TriageQueue.
 *
 * PRIORITY RULES:
 * ---------------
 * 1) Patients with **higher severity** should come first.
 *        e.g., severity 10 > severity 3
 *
 * 2) If severities are equal, use **arrivalSeq** as a tiebreaker.
 *      → smaller arrivalSeq = arrived earlier = higher priority
 *
 * WHY THIS CLASS EXISTS:
 * ----------------------
 * Java’s PriorityQueue requires a Comparator so it knows which element
 * has "higher priority." It does *not* assume that lower values are
 * higher priority; instead, comparator decides.
 *
 * TriageOrder ensures:
 *   • Emergency patients come first
 *   • FIFO stability among equal-severity patients
 *
 * WHY NOT COMPARE arrival timestamp instead?
 * ------------------------------------------
 * Nanosecond timestamps are too fast and unrealistic for triage,
 * and two patients could be created in the same nanosecond.
 *
 * arrivalSeq is:
 *   → simple
 *   → monotonic
 *   → collision-free
 *   → stable ordering
 *
 * Big-O:
 * ------
 * Comparator itself is O(1).
 * PriorityQueue operations:
 *     add() / poll() = O(log n)
 * Comparator does NOT change asymptotic behavior.
 * ========================================================================
 */
public final class TriageOrder implements Comparator<Patient> {

    /**
     * ============================================================
     * compare(patient1, patient2)
     * ============================================================
     * This method returns:
     *     negative number → p1 < p2 → p1 has HIGHER priority
     *     zero            → equal priority
     *     positive number → p1 > p2 → p1 has LOWER priority
     *
     * ⚠ IMPORTANT:
     * PriorityQueue in Java always removes the "smallest" element
     * according to the comparator. That means:
     *     "smallest" = "highest priority"
     *
     * So if we want high severity FIRST, we must make comparator
     * treat high severity as "smaller."
     */
    @Override
    public int compare(Patient patient1, Patient patient2) {

        // --------------------------------------------------------
        // Case: both references point to the same object
        // Return 0 so the PriorityQueue doesn’t misbehave.
        // --------------------------------------------------------
        if (patient1 == patient2) return 0;

        // --------------------------------------------------------
        // Null handling (not expected in your program)
        // but required for comparator completeness.
        //
        // Null is treated as *lowest* priority so it ends up last.
        //
        // NOTE: You specifically asked not to add unnecessary
        // features, but this null check is part of your original
        // code, so it remains.
        // --------------------------------------------------------
        if (patient1 == null) return 1;
        if (patient2 == null) return -1;

        // --------------------------------------------------------
        // PRIMARY SORT KEY: severity (descending)
        //
        // Higher severity = higher priority.
        //
        // But comparator must return:
        //   negative → p1 higher priority
        //
        // So we compare severity in REVERSE ORDER:
        //
        //     Integer.compare(patient2, patient1)
        //
        // Example:
        //   p1.sev = 9, p2.sev = 3
        //   compare(3,9) → negative → p1 < p2 → p1 comes first ✔
        // --------------------------------------------------------
        int severityComparison =
                Integer.compare(patient2.getSeverity(), patient1.getSeverity());

        if (severityComparison != 0) {
            return severityComparison;  // return first non-zero comparison
        }

        // --------------------------------------------------------
        // SECONDARY SORT KEY: arrivalSeq (ascending)
        //
        // Smaller arrivalSeq = arrived earlier = higher priority.
        //
        // You wrote:
        //     return Long.compare(patient1.getArrivalSeq(), ...)
        //
        // This is correct because:
        //   smaller seq → negative → higher priority
        //
        // Example:
        //   seq1 = 5, seq2 = 9
        //   compare(5,9) = -1  → p1 higher priority ✔
        // --------------------------------------------------------
        return Long.compare(patient1.getArrivalSeq(), patient2.getArrivalSeq());
    }
}
