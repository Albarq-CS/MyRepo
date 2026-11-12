# Library Management System
## Project Overview
A Java Swing desktop application backed by Microsoft Access through JDBC/UCanAccess. This academic project demonstrates database-backed interfaces for library users and administrators.

## Problem Statement
Bring book discovery, borrowing, returns, and basic library administration into one desktop application.

## Key Features
- Separate administrator and user entry points.
- Login and user registration.
- Book listing and search.
- Borrowing records and book-return workflows.
- User information and overdue-book views.

## Technologies Used
Java 22 (as configured in Maven), Swing/AWT, JDBC, Microsoft Access, Maven, and UCanAccess 5.0.1.

## Architecture Overview
Swing windows call JDBC queries against the included Access database. `LibraryApp` opens the role-selection screen, `Log_Rig_View` handles login/registration, and `AdminWindow` and `LibraryWindow` provide role-specific interfaces. UI and data access currently share the same classes.

## Project Structure
| Path | Purpose |
| --- | --- |
| `pom.xml` | Java target and JDBC dependency |
| `src/main/java/com/mycompany/libraryapp/` | Application windows |
| `Laibrary.accdb` | Existing Access database |
| `Background.jpg` | UI background |
| `ProjectReport_CS342.pdf` | Original academic report |
| `target/` | Previously committed build output |

## Installation
Use JDK 22 and Maven. Back up the database before running.
```sh
mvn compile
mvn exec:java -Dexec.mainClass=com.mycompany.libraryapp.LibraryApp
```
The build and GUI were not executed during this documentation update.

## Configuration
The source currently contains machine-specific Windows paths for the database in `LibraryApp.java` and the background in `AdminWindow.java`. These must point to your local copies before launching. Credentials are not supplied in this documentation. Treat the included database as potentially containing historical records; use a separate local copy for evaluation.

## Usage
Choose Admin or User, then sign in using an account in your local database, or use the available user-registration workflow. Browse/search books and inspect role-specific actions. This is a local Swing application, not a hosted web service.

## Future Improvements
- Replace hard-coded paths with local configuration.
- Review password storage and authorization before any real deployment.
- Separate JDBC repositories from Swing views and add transaction tests.
- Stop tracking generated build output in a separately reviewed cleanup.

## Author
[Albarq-CS](https://github.com/Albarq-CS)
