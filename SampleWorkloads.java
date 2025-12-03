package edu.hcu.triage;

import java.util.Random;

/**
 * Deterministic workload generator for performance testing.
 * This class is used to automatically create realistic patient queue
 * so that the TriageQueue and PatientRegistry can be tested under load.
 * The workload is deterministic due to the fixed RNG seed, enabling repeatable
 * performance comparisons between implementations.
 */
public final class SampleWorkloads {

    /** 
     * Choice of statistical distribution for severity values. (Chat-GPT suggestion)
     * UNIFORM → every severity 1–10 equally likely.
     * SKEWED → intentionally biased so low severities appear more often,
     *           simulating real-world triage where minor cases dominate.
     */
    public enum SeverityDistribution {
        UNIFORM,
        SKEWED
    }

    /** Pseudo-random generator used for repeatable workloads. */
    private final Random rng;

    /** The chosen distribution for severity generation. */
    private final SeverityDistribution distribution;

    /** Counter for generating unique synthetic patient IDs (P0001, P0002...). */
    private int nextIdCounter = 1;

    /**
     * Constructor.
     * @param seed deterministic seed so test runs can be reproduced exactly.
     * @param dist severity distribution to use for workload generation.
     */
    public SampleWorkloads(long seed, SeverityDistribution dist) {
        this.rng = new Random(seed);
        this.distribution = dist;
    }

    /**
     * Generate a random severity value according to the chosen distribution.
     * UNIFORM → simple 1–10 range.
     * SKEWED → weighted categories where low numbers appear more frequently.
     */
    private int randomSeverity() {
        switch (distribution) {
            case UNIFORM:
                return 1 + rng.nextInt(10);   // uniform 1–10

            case SKEWED:
                // Weighted distribution where ~50% of patients land in the 1–3 range.
                int roll = rng.nextInt(100);
                if (roll < 25) return 1;
                if (roll < 50) return 2;
                if (roll < 60) return 3;
                if (roll < 75) return 4;
                if (roll < 85) return 5;
                if (roll < 92) return 6;
                if (roll < 96) return 7;
                if (roll < 98) return 8;
                if (roll < 99) return 9;
                return 10;  // extremely rare high-severity event

            default:
                // Should never hit, but safe fallback.
                return 5;
        }
    }

    /**
     * Generates the next unique artificial patient ID in a fixed-width format.
     * Example → P0001, P0002, ..., ensuring lexicographic ordering.
     */
    private String nextGeneratedId() {
        return String.format("P%04d", nextIdCounter++);
    }

    /**
     * Bulk-enqueue: creates 'count' brand-new synthetic patients and
     * inserts them into both the registry and the triage queue.
     * 
     * Uses the registry first so arrivalSeq numbers remain strictly increasing.
     */
    public void enqueueRandomPatients(int count, PatientRegistry reg, TriageQueue queue) {
        for (int i = 0; i < count; i++) {
            String id = nextGeneratedId();
            String name = "Patient-" + id;
            int age = rng.nextInt(120);    // enqueue random ages from 0 to 120
            int severity = randomSeverity();

            // Registry assigns arrival order.
            Patient p = reg.registerNew(id, name, age, severity);

            // Then patient is placed in queue based on severity + arrival order.
            queue.enqueue(p);
        }
    }

    /**
     * Safely performs 'count' dequeue operations.
     * 
     * If the queue is empty, dequeueNext() returns null — ignored intentionally,
     * since this method is meant to simulate general traffic patterns where
     * dequeue attempts may occur even when no patients are waiting.
     */
    public void performDequeues(int count, TriageQueue queue) {
        for (int i = 0; i < count; i++) {
            queue.dequeueNext(); // intentionally ignoring returned value
        }
    }

    /**
     * Mixed workload generator.
     * 
     * For totalOps operations, randomly chooses between enqueue and dequeue
     * based on the provided ratio.
     */
    public void runMixedWorkload(int totalOps,
                                 int ratioEnq,
                                 int ratioDeq,
                                 PatientRegistry reg,
                                 TriageQueue queue) {

        int totalRatio = ratioEnq + ratioDeq;

        for (int i = 0; i < totalOps; i++) {
            // Randomly decide whether to enqueue or dequeue.
            int r = rng.nextInt(totalRatio);

            if (r < ratioEnq) {
                // Enqueue Operation
                String id = nextGeneratedId();
                String name = "Patient-" + id;
                int age = rng.nextInt(120);
                int severity = randomSeverity();

                Patient p = reg.registerNew(id, name, age, severity);
                queue.enqueue(p);

            } else {
                // Dequeue operation
                // Dequeue ignored safely if queue empty.
                queue.dequeueNext();
            }
        }
    }
}
