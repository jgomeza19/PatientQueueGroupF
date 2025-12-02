package edu.hcu.triage;

import java.util.Random;

/**
 * Deterministic workload generator for performance and stress testing.
 * Integrates with PatientRegistry, TriageQueue, and the project's Patient model.
 */
public final class SampleWorkloads {

    /** Distribution options for severity generation. */
    public enum SeverityDistribution {
        UNIFORM,   // 1–10 uniform
        SKEWED     // Suggested skewed for for those to have higher severity
    }

    private final Random rng;
    private final SeverityDistribution distribution;
    private int nextIdCounter = 1;

    public SampleWorkloads(long seed, SeverityDistribution dist) {
        this.rng = new Random(seed);
        this.distribution = dist;
    }

    /** Generate random severity depending on configured distribution. */
    private int randomSeverity() {
        switch (distribution) {
            case UNIFORM:
                return 1 + rng.nextInt(10);   // uniform 1–10
            case SKEWED:
                // ~50% will be severity 1–3
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
                return 10;
            default:
                return 5;
        }
    }

    /** Generate a sequential patient ID like P001, P002, etc. */
    private String nextGeneratedId() {
        return String.format("P%04d", nextIdCounter++);
    }

    /**
     * Enqueues N random new patients.
     * Uses the PatientRegistry to ensure arrivalSeq is assigned correctly.
     */
    public void enqueueRandomPatients(int count, PatientRegistry reg, TriageQueue queue) {
        for (int i = 0; i < count; i++) {
            String id = nextGeneratedId();
            String name = "Patient-" + id;
            int age = 18 + rng.nextInt(65);
            int severity = randomSeverity();

            Patient p = reg.registerNew(id, name, age, severity);
            queue.enqueue(p);
        }
    }

    /**
     * Performs K dequeues safely (ignores empty queue).
     */
    public void performDequeues(int count, TriageQueue queue) {
        for (int i = 0; i < count; i++) {
            queue.dequeueNext(); // Optional ignored safely
        }
    }

    /**
     * Runs a mixed enqueue/dequeue workload.
     * ratioEnq = e.g. 7, ratioDeq = 3 → 70% enqueues, 30% dequeues.
     */
    public void runMixedWorkload(int totalOps,
                                 int ratioEnq,
                                 int ratioDeq,
                                 PatientRegistry reg,
                                 TriageQueue queue) {

        int totalRatio = ratioEnq + ratioDeq;

        for (int i = 0; i < totalOps; i++) {
            int r = rng.nextInt(totalRatio);

            if (r < ratioEnq) {
                // Enqueue for new patient
                String id = nextGeneratedId();
                String name = "Patient-" + id;
                int age = 18 + rng.nextInt(65);
                int severity = randomSeverity();

                Patient p = reg.registerNew(id, name, age, severity);
                queue.enqueue(p);
            } else {
                // DEQUEUE (if possible)
                queue.dequeueNext();
            }
        }
    }
}
