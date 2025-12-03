package edu.hcu.triage;

import java.util.LinkedList;
import java.util.List;

/**
 * TreatmentLog stores ALL TreatedCase objects in the order they were added.
 *
 * This class serves as a simple append-only list used for:
 *   - displaying treatment history
 *   - exporting treatment history to CSV
 *   - keeping chronological medical records
 *
 * Internally uses a LinkedList for efficient append operations.
 * LinkedList is a good fit because:
 *   - insert at end = O(1)
 *   - order matters
 *   - rarely need random access (so ArrayList not necessary)
 *
 * The log grows as treatments occur; no cases are deleted.
 */
public class TreatmentLog {

    /**
     * The internal LinkedList that holds all TreatedCase objects.
     *
     * We store oldest → newest in the natural list order:
     *    first element = oldest treatment
     *    last element  = most recent treatment
     *
     * LinkedList is used because:
     *   - append at end is extremely fast (O(1))
     *   - guaranteed order preservation
     */
    private final LinkedList<TreatedCase> log = new LinkedList<>();

    /**
     * Appends a new treated case to the END of the log.
     * This preserves the chronological order in which patients are treated.
     *
     * Why addLast():
     *   - LinkedList.addLast() is O(1)
     *   - Ensures proper time ordering (FIFO style)
     */
    public void append(TreatedCase tc) {
        log.addLast(tc);
    }

    /**
     * Returns how many treatment events are stored.
     * This is useful for debugging or UI display (e.g., "There are 5 cases").
     *
     * This directly calls size() on the LinkedList (O(1) runtime).
     */
    public int size() {
        return log.size();
    }

    /**
     * Return the treatment log in chronological order:
     *   - The first treatment that ever happened is first in the list.
     *   - The most recent treatment is last.
     *
     * We return a COPY of the linked list:
     *    new LinkedList<>(log)
     *
     * Why create a copy?
     *   - Prevents callers from modifying the internal log accidentally.
     *   - Ensures the internal list stays protected (encapsulation).
     *
     * We return a List<TreatedCase> so the calling code can iterate easily.
     */
    public List<TreatedCase> asListOldestFirst() {
        return new LinkedList<>(log); // shallow copy of the linked list
    }

    /**
     * Returns the treatment log in REVERSE chronological order:
     *   - Most recently treated patient appears first
     *   - Oldest treatment appears last
     *
     * Steps:
     *  Copy the internal list
     *  Reverse the copy
     *  Return the reversed list
     *
     * We reverse the COPY only, not the original:
     *   - Keeps the internal "oldest → newest" structure untouched
     *
     * This method is used when displaying the log from newest to oldest,
     * such as when a doctor wants to check recent treatments first.
     */
    public List<TreatedCase> asListNewestFirst() {
        
        LinkedList<TreatedCase> reversed = new LinkedList<>(log);

        
        java.util.Collections.reverse(reversed);

        // reversed version
        return reversed;
    }
}
