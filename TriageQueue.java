package edu.hcu.triage;

import java.util.ArrayList;          // Used for snapshot list creation
import java.util.Collections;        // Used to make snapshot unmodifiable
import java.util.List;               // Generic List interface
import java.util.Optional;           // For safe return type without nulls
import java.util.PriorityQueue;      // Core PQ implementation from Java

/**
 * ============================================================================
 *                                TriageQueue.java
 * ============================================================================
 * PURPOSE OF THIS CLASS:
 * ----------------------
 * This class manages the actual waiting line (queue) of patients during triage.
 *
 * The requirements:
 *   • Order patients automatically by severity (higher → first)
 *   • Break ties by arrival order (earlier → first)
 *   • Allow enqueue by Patient object OR by ID (via PatientRegistry)
 *   • Provide peek(), dequeue(), snapshot(), size(), and clear()
 *
 * WHY A PRIORITY QUEUE?
 * ---------------------
 * A PriorityQueue allows us to always extract the "highest priority"
 * patient in O(log n) time.
 *
 * With TriageOrder controlling the comparator, the queue sorts by:
 *      1) severity (desc)
 *      2) arrivalSeq (asc)
 *
 * COMPLEXITY:
 * -----------
 *   enqueue()     : O(log n)
 *   dequeueNext() : O(log n)
 *   peek()        : O(1)
 *   snapshot()    : O(n log n)  (because it repeatedly polls a COPY)
 *
 * THREAD SAFETY:
 * --------------
 * All methods are synchronized to ensure internal consistency.
 * ============================================================================
 */
public class TriageQueue {

    // =========================================================================
    // PriorityQueue<Patient> pq
    // =========================================================================
    // This PriorityQueue uses a custom Comparator (TriageOrder) so that the
    // queue always keeps the correct triage order.
    //
    // NOTE:
    //   • PriorityQueue does NOT sort globally.
    //   • It only guarantees that the *head* is the highest priority element.
    //
    // Every offer(), poll(), or peek() checks order via TriageOrder comparator.
    // =========================================================================
    private final PriorityQueue<Patient> pq = new PriorityQueue<>(new TriageOrder());


    // =========================================================================
    // enqueue(Patient p)
    // =========================================================================
    /**
     * Add a Patient directly into the triage queue.
     *
     * @param p patient object (must be fully initialized)
     *
     * LOGIC:
     *   • Reject null (as your original code did)
     *   • Insert into PriorityQueue using offer()
     *   • PQ automatically positions it by severity & arrivalSeq
     *
     * TIME COMPLEXITY:
     *   offer() = O(log n)
     */
    public synchronized void enqueue(Patient p) {
        if (p == null) {
            // This matches your original strictness: null not allowed.
            throw new IllegalArgumentException("patient required");
        }

        // PQ will use TriageOrder to place p correctly.
        pq.offer(p);
    }


    // =========================================================================
    // enqueueById(PatientRegistry reg, String id)
    // =========================================================================
    /**
     * Looks up a patient by ID from the registry and enqueues them.
     *
     * @param reg registry storing all patients
     * @param id  ID of patient to enqueue
     * @return true if the patient was found and enqueued, false otherwise
     *
     * LOGIC:
     *   1) Ask PatientRegistry for patient (Optional)
     *   2) If present, offer to PQ
     *   3) If missing, return false so caller knows
     */
    public synchronized boolean enqueueById(PatientRegistry reg, String id) {

        // If registry or ID is null, we cannot proceed.
        if (reg == null || id == null) {
            return false;
        }

        // Attempt to retrieve the patient using the registry.
        Optional<Patient> op = reg.get(id);

        // If no patient with that ID exists → cannot enqueue.
        if (op.isEmpty()) {
            return false;
        }

        // Enqueue the found patient.
        pq.offer(op.get());

        return true;
    }


    // =========================================================================
    // peekNext()
    // =========================================================================
    /**
     * Returns the next patient to be treated WITHOUT removing them.
     *
     * @return Optional of first patient, or empty if queue is empty
     *
     * LOGIC:
     *   peek() simply returns the head of the PQ in O(1) time.
     *
     * WHY Optional?
     *   avoids returning null, makes caller check emptiness cleanly.
     */
    public synchronized Optional<Patient> peekNext() {
        return Optional.ofNullable(pq.peek());
    }


    // =========================================================================
    // dequeueNext()
    // =========================================================================
    /**
     * Removes AND returns the highest priority patient.
     *
     * @return Optional<Patient> (empty if PQ empty)
     *
     * LOGIC:
     *   • poll() removes head → O(log n)
     *   • wrap in Optional
     */
    public synchronized Optional<Patient> dequeueNext() {
        return Optional.ofNullable(pq.poll());
    }


    // =========================================================================
    // size()
    // =========================================================================
    /**
     * @return number of patients currently waiting in triage
     *
     * LOGIC:
     *   • PQ size retrieval is O(1)
     */
    public synchronized int size() {
        return pq.size();
    }


    // =========================================================================
    // snapshotOrder()
    // =========================================================================
    /**
     * Returns a *full list* showing current triage order WITHOUT
     * modifying the actual priority queue.
     *
     * HOW IT WORKS:
     *   1) Copy the original PQ into a NEW PriorityQueue
     *   2) Poll repeatedly from the copy (this gives sorted order)
     *   3) Store in ArrayList
     *   4) Return an UNMODIFIABLE list to prevent external mutation
     *
     * TIME COMPLEXITY:
     *   Copy PQ           = O(n)
     *   Poll n elements   = n * O(log n) = O(n log n)
     *
     * NOTE:
     *   We cannot simply iterate the PQ directly because iteration
     *   DOES NOT guarantee elements in priority order.
     */
    public synchronized List<Patient> snapshotOrder() {

        // Make a *duplicate* PQ (shallow copy: contains the same patient objects)
        PriorityQueue<Patient> copy = new PriorityQueue<>(pq);

        // This list will contain the sorted triage order.
        List<Patient> list = new ArrayList<>();

        // Poll copy until empty → yields patients in correct order.
        while (!copy.isEmpty()) {
            list.add(copy.poll());
        }

        // Return an unmodifiable wrapper so caller cannot change our list.
        return Collections.unmodifiableList(list);
    }


    // =========================================================================
    // clear()
    // =========================================================================
    /**
     * Removes ALL patients from the queue.
     *
     * LOGIC:
     *   • Just call clear() on PriorityQueue
     *   • O(n) to discard all internal references
     */
    public synchronized void clear() {
        pq.clear();
    }
}
