# Task Dashboard

A task management application built with React, plus a self-contained Java Swing desktop application for City of Malabon University's online document requesting system.

## Task Dashboard

The original web application supports task management, tags, lists, filtering, responsive UI, and Vitest coverage. See the existing npm scripts in `package.json` for development, build, preview, and test commands.

## City of Malabon University document requests

The Java desktop application uses AWT/Swing, local persistence, and no external dependencies.

### Run

Requires JDK 8 or newer. From the repository root:

```bash
javac -d out src/com/cmu/documents/Main.java
java -cp out com.cmu.documents.Main
```

Students can sign up with their student ID, password, and department, request documents, and track statuses. Admins sign up/log in with an admin ID and department, filter requests by department, and update statuses. Pickup month, date, and time are mandatory for **Ready for Pickup**. Data is saved in `.cmu-document-requests.dat` in the current user's home directory.
