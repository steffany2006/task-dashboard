# City of Malabon University Online Document Requesting System

A self-contained Java Swing desktop application with local persistence and no external dependencies.

## Run

Requires JDK 8 or newer. From the repository root:

```bash
javac -d out src/com/cmu/documents/Main.java
java -cp out com.cmu.documents.Main
```

Data is saved in `.cmu-document-requests.dat` in the current user's home directory. Students can sign up with their student ID, password, and department, request documents, and track statuses. Admins sign up/log in with an admin ID and department, filter requests by department, and update statuses. Pickup details are mandatory for **Ready for Pickup**.
