package edu.hcu.triage;

/** Tiny helper for timing blocks. */
public final class PerfTimer implements AutoCloseable {
    private final String label;
    private final long start;

    public PerfTimer(String label) {
        this.label = label;
        this.start = System.nanoTime();
    }

    @Override
    public void close() {
        long ns = System.nanoTime() - start;
        double ms = ns / 1_000_000.0;
        System.out.printf("%s: %.3f ms%n", label, ms);
    }
}
