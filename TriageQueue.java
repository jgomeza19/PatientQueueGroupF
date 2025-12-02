package edu.hcu.triage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;

/**
 * Manages the triage order using a PriorityQueue.
 * Priority is based on severity first, then arrivalSeq.
 */
public class TriageQueue {

    // PriorityQueue uses TriageOrder comparator so patients are sorted correctly
    private final PriorityQueue<Patient> pq = new PriorityQueue<>(new TriageOrder());

    /**
     * Adds an already-created Patient object to the queue.
     * Steps:
     * 1) Validate the patient object is not null.
     * 2) Insert it into the PriorityQueue.
     */
    public synchronized void enqueue(Patient p) {
        if (p == null) throw new IllegalArgumentException("patient required");
        pq.offer(p);  // priority queue handles ordering automatically
    }

    /**
     * Adds a patient to the triage queue using only their ID.
     * Steps:
     * 1) Look up the patient from the registry.
     * 2) If found, add them to the queue.
     * 3) Return true if successful.
     */
    public synchronized boolean enqueueById(PatientRegistry reg, String id) {
        if (reg == null || id == null) return false;

        Optional<Patient> op = reg.get(id);
        if (op.isEmpty()) return false;    // ID not found

        pq.offer(op.get());                // enqueue patient
        return true;
    }

    /**
     * Look at the next patient WITHOUT removing them from the queue.
     */
    public synchronized Optional<Patient> peekNext() {
        return Optional.ofNullable(pq.peek());
    }

    /**
     * Remove and return the next patient in the triage queue.
     * Steps:
     * 1) PriorityQueue.poll() removes the element with highest priority.
     * 2) Wrap it in Optional and return.
     */
    public synchronized Optional<Patient> dequeueNext() {
        return Optional.ofNullable(pq.poll());
    }

    /**
     * Returns how many patients are currently waiting in triage.
     */
    public synchronized int size() {
        return pq.size();
    }

    /**
     * Create a list that shows the triage order WITHOUT modifying the queue.
     * Steps:
     * 1) Make a copy of the PriorityQueue.
     * 2) Poll from the copy to get patients in priority order.
     * 3) Return the list (unmodifiable for safety).
     */
    public synchronized List<Patient> snapshotOrder() {
        PriorityQueue<Patient> copy = new PriorityQueue<>(pq); // make duplicate
        List<Patient> list = new ArrayList<>();

        while (!copy.isEmpty()) {
            list.add(copy.poll());
        }

        return Collections.unmodifiableList(list);  // return safe list
    }

    /**
     * Completely clear the triage queue.
     */
    public synchronized void clear() {
        pq.clear();
    }
}
