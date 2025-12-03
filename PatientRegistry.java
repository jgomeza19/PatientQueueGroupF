package edu.hcu.triage;

// Import HashMap for O(1) average lookup/insert/update
import java.util.HashMap;
// Import Map interface (good practice)
import java.util.Map;
// Import Optional for safe return of maybe-present values
import java.util.Optional;

/**
 * ================================================================
 *                     PatientRegistry.java
 * ================================================================
 * This class is responsible for:
 *    - Creating *new* Patient objects
 *    - Assigning each a unique arrival sequence number
 *    - Storing them in a HashMap for fast ID lookup
 *    - Providing update/retrieval operations
 *
 * WHY THIS CLASS EXISTS:
 * ----------------------
 * Instead of letting HospitalApp create Patients directly, we put
 * creation logic here so we can properly control:
 *      • arrivalSeq assignment (monotonic increasing)
 *      • ID collision handling (replace or not)
 *      • central storage of all patients in the system
 *
 * DESIGN CHOICE:
 * --------------
 * We use a HashMap because:
 *      • ID lookups are O(1) average time
 *      • Very small memory footprint
 *      • No need to maintain ordering here
 *
 * THREAD SAFETY:
 * --------------
 * All public methods are `synchronized`. This ensures:
 *      • no two threads modify the map at the same time
 *      • arrivalSeq increments safely without collisions
 *
 * (Your app is single-threaded, but adding synchronized is simple
 * future-proofing and doesn’t hurt anything.)
 * ================================================================
 */
public class PatientRegistry {

    // --------------------------------------------------------------------
    // HashMap<String, Patient>
    // -------------------------
    // Key   = patient ID (String)
    // Value = Patient object
    // --------------------------------------------------------------------
    // ❗ This gives O(1) average time for insert/update/get.
    // --------------------------------------------------------------------
    private final Map<String, Patient> byId = new HashMap<>();


    // --------------------------------------------------------------------
    // Monotonic sequence counter used to assign arrival order.
    // --------------------------------------------------------------------
    // WHY WE NEED THIS:
    // In real triage systems, if two patients have equal severity,
    // the earlier arrival gets priority.
    //
    // HOW IT WORKS:
    // Each time registerNew() is called:
    //     - use current value
    //     - increment nextArrivalSeq
    //
    // Example:
    // first register → arrivalSeq = 0
    // second register → arrivalSeq = 1
    // third register → arrivalSeq = 2
    // and so on...
    // --------------------------------------------------------------------
    private long nextArrivalSeq = 0L;


    // ================================================================
    //   registerNew()
    // ================================================================
    /**
     * Registers a completely NEW patient in the system.
     *
     * This method:
     *   1) assigns arrivalSeq
     *   2) constructs a new Patient object
     *   3) stores it in our HashMap
     *   4) returns that Patient object
     *
     * @param id       unique identifier for patient (e.g., "P0042")
     * @param name     patient name
     * @param age      patient age
     * @param severity triage severity (1–10)
     */
    public synchronized Patient registerNew(String id, String name, int age, int severity) {

        // ------------------------------------------------------------
        // Assign current sequence number, then increment for next time.
        // ------------------------------------------------------------
        long seq = nextArrivalSeq++;

        // ------------------------------------------------------------
        // Create the Patient object.
        //
        // Patient constructor handles:
        //      • ID validation
        //      • name null/blank handling
        //      • severity range enforcement
        //      • auto timestamp (Instant.now())
        // ------------------------------------------------------------
        Patient p = new Patient(id, name, age, severity, seq);

        // ------------------------------------------------------------
        // Insert/replace the patient using their ID as the key.
        // If an ID already existed, this will overwrite it.
        // ------------------------------------------------------------
        // If the professor requires rejecting duplicates,
        // a check could be added — but YOU SAID not to add any
        // features not explicitly required.
        // ------------------------------------------------------------
        byId.put(p.getId(), p);

        // ------------------------------------------------------------
        // Return the newly created Patient.
        // ------------------------------------------------------------
        return p;
    }


    // ================================================================
    //   updateExisting()
    // ================================================================
    /**
     * Updates an existing patient but *only* the fields that are
     * non-null. This makes updates flexible and also matches your
     * HospitalApp where blank inputs skip fields.
     *
     * @param id       patient’s ID to update
     * @param name     new name (null = don't change)
     * @param age      new age  (null = don't change)
     * @param severity new severity (null = don't change)
     *
     * @return Optional containing updated patient or empty if ID not found
     */
    public synchronized Optional<Patient> updateExisting(
            String id, String name, Integer age, Integer severity) {

        // ------------------------------------------------------------
        // Try to find the patient in the map.
        // If ID missing → return Optional.empty()
        // ------------------------------------------------------------
        Patient p = byId.get(id);
        if (p == null) {
            return Optional.empty();
        }

        // ------------------------------------------------------------
        // Update name only when caller actually provided one.
        //
        // The Patient setters also validate null/blank values internally.
        // ------------------------------------------------------------
        if (name != null) {
            p.setName(name);
        }

        // ------------------------------------------------------------
        // Update age only if not null.
        // ------------------------------------------------------------
        if (age != null) {
            p.setAge(age);
        }

        // ------------------------------------------------------------
        // Update severity only if not null.
        // ------------------------------------------------------------
        if (severity != null) {
            p.setSeverity(severity);
        }

        // ------------------------------------------------------------
        // Return Optional containing updated patient.
        // ------------------------------------------------------------
        return Optional.of(p);
    }


    // ================================================================
    //   get()
    // ================================================================
    /**
     * Retrieves a patient by ID in O(1) time.
     *
     * Because the patient may or may not exist, we wrap it in Optional.
     *
     * @param id patient ID
     * @return Optional<Patient> — empty if ID not found
     */
    public synchronized Optional<Patient> get(String id) {
        return Optional.ofNullable(byId.get(id));
    }


    // ================================================================
    //   contains()
    // ================================================================
    /**
     * Checks if registry contains a patient with this ID.
     *
     * @param id patient ID
     * @return true if found, false otherwise
     */
    public synchronized boolean contains(String id) {
        return byId.containsKey(id);
    }


    // ================================================================
    //   size()
    // ================================================================
    /**
     * @return how many total patients are currently stored.
     */
    public synchronized int size() {
        return byId.size();
    }
}
