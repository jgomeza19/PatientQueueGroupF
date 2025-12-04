Hospital Patient Queue Management System

Group F - COSC 2351 - Fall 2025

==============================================================================================

** Steps:

	1) Open the 'PatientQueue' folder in any Java IDE (like IntelliJ)
	   
	2) Make sure the folder is structured as shown below:
	   src/ 
            └─ edu/hcu/triage/ 
                   Patient.java 
                   TriageOrder.java 
                   PatientRegistry.java 
                   TriageQueue.java 
                   TreatmentLog.java 
                   TreatedCase.java 
                   CsvIO.java 
                   PerfTimer.java 
                   HospitalApp.java 
                   SampleWorkloads.java 
		   patients.csv

	3) In your IDE, open (or right-click) HospitalApp.java and run it.

	4) Once you start running HospitalApp, the menu-driven code will start in the
	   console where you can add patient, update patient, enqueue, log treatment,
	   run a performace demo, export log to patients.csv, etc.


==============================================================================================

** Design Choices:

	* Data Structures
	  - PriorityQueue with comparator for patient severity (1 - 10; low to high)
	  - LinkedList for logging treatment to allow access and insertion of new data
	  - Hashing (seen on 'Patient.java'; HashMap comparator used throughout Java 
	    class files) is used to make patient IDs unique to allow constant-time
            lookup, and prevent duplicates and possible collisions in case two patients
            share the same ID.

	* Comparator
	  - Comparator is used throughout Java classes to make sure that patients with
            higher severity cases are treated first. The comparator interface ensures that in
	    case two patients have the same severity, insertion order (FIFO) is preserved
	    depending on who arrived first.

	* Sample Workloads
	  - 'SampleWorkloads.java' can generate patient data with k and n numbers of queues
	    and queues, mainly for test performance where you can insert, update, and export
            data to a csv file.


==============================================================================================

**Invariants

	1) Each patient in the program has a unique ID (hashing)

	2) Severity is between 1 (lowest; little concern) and 10 (highest; serious concern)

	3) Queue orders patients in an order of descending severity values (highest first).

	4) Treatment log orders patients based on who arrived first.

	5) CSV exports have the format id, name, age, severity.


==============================================================================================

** Known Issues

	* Exporting data to CSV file may take time to load (reload if necessary) and spacing
	  may be distorted.

        * As discussed with Dr. Joshuva, we agreed and checked to measure queue and dequeue
	  in milliseconds (ms) LinkedList of patients rather than in nanoseconds (ns) because 
	  milliseconds tend to provide clearer and more stable timing values that can easily 
	  be read in a program. Milliseconds, however, may not be an ideal time measurement 
	  for the hospital due to unpredictable urgencies.


==============================================================================================

** Who Did What

	* Adrian Beltran:
	  - PatientRegistry.java
	  - TriageQueue.java
	  - Slides

	* Jonathan Gomez:
	  - Patient.java
	  - TriageOrder.java
	  - HospitalApp.java
	  - SampleWorkloads.java
	  - Test and debug code
	  - Fill out README.txt

	* Clifton Jones:
	  - CsvIO.java
	  - PerfTimer.java
	  - Slides

	* Josue Peralta:
	  - TreatmentLog.java
	  - TreatedCase.java
	  - Test and debug code
