package edu.hcu.triage;

import java.util.LinkedList;
import java.util.List;

public class TreatmentLog {

    private final LinkedList<TreatedCase> log = new LinkedList<>();

    // ADDED: append case
    public void append(TreatedCase tc) {
        log.addLast(tc);
    }

    // ADDED: size
    public int size() {
        return log.size();
    }

    // ADDED: return oldest → newest
    public List<TreatedCase> asListOldestFirst() {
        return new LinkedList<>(log);
    }

    // ADDED: return newest → oldest
    public List<TreatedCase> asListNewestFirst() {
        LinkedList<TreatedCase> reversed = new LinkedList<>(log);
        java.util.Collections.reverse(reversed);
        return reversed;
    }
}
