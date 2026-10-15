# CampusFind — Privacy-First Campus Lost & Found Management System

A robust, modular, and privacy-conscious console application built with **Core Java 17+**, **JDBC**, **MySQL**, and **JUnit 5**, designed to demonstrate clean layered architecture, OOP design principles, and transactional database patterns for fresher Java developer interviews.

---

## 1. Problem Statement

Belongings such as calculators, ID cards, wallets, and laptop chargers are frequently misplaced across campus. Existing lost-and-found systems present a major security flaw: **they display all identifying characteristics publicly**. This invites dishonest individuals to falsely claim high-value items simply by repeating details they saw online.

**CampusFind** solves this using **Privacy-First Claim Verification**:
- The finder records a **secret verification clue** (e.g. an engraved initial, hidden sticker, or serial number suffix) that is **never shown publicly** or written to logs.
- When an owner files a claim, they submit their own private answer.
- Campus staff review the claimant's answer alongside the secret clue in a dedicated comparison screen before authorizing approval via an **atomic ACID transaction**.

---

## 2. Key Features

1. **Report a Found Item**: Captures item details and confidential verification clues with strict validation.
2. **List Available Items**: Tabular listing of open items (`AVAILABLE` and `CLAIM_PENDING`), strictly omitting secret clues.
3. **Search & Filter**: Keyword search across item names and descriptions, plus filtering by category and location.
4. **Submit a Claim**: Validates claimant contact information and captures their private verification response. Prevents duplicate active claims.
5. **Privacy-Verified Claim Review**: Staff view side-by-side comparison cards comparing the finder's secret clue with the claimant's answer.
6. **Atomic Claim Approval**: Atomically sets claim to `APPROVED`, synchronizes item status, and auto-rejects competing pending claims in a single transaction with rollback protection.
7. **Safe Return Handover**: Enforces a strict business invariant: an item can only be marked `RETURNED` if an approved claim exists.
8. **Live Statistics Dashboard**: Aggregates total items, pending reviews, approval rates, and hotspot locations directly from database records.
9. **Full Audit History**: View complete historical logs of all reported items and claims.

---

## 3. Technology Stack

- **Language**: Java 17 LTS or newer (Tested on OpenJDK 25)
- **Build Tool**: Apache Maven 3.9+
- **Database**: MySQL 8.0+
- **Data Access**: JDBC (MySQL Connector/J 8.3.0)
- **Unit Testing**: JUnit 5 Jupiter (5.10.2)
- **Logging**: Standard `java.util.logging`

---

## 4. Architecture Overview

CampusFind strictly adheres to an N-Tier **Layered Architecture**:

```
UI Layer (ConsoleMenu, ConsolePrinter)
       │ (User input & presentation)
       ▼
Service Layer (ItemService, ClaimService, ValidationService)
       │ (Business rules, state transitions, privacy filters)
       ▼
DAO Layer (FoundItemDao, ClaimDao)
       │ (PreparedStatements, Transactions, Entity Mapping)
       ▼
Database Layer (MySQL 8.0: found_items, claims)
```

See [`docs/ARCHITECTURE.md`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/docs/ARCHITECTURE.md) for full architectural sequence diagrams and responsibilities.

---

## 5. Prerequisites

- **Java JDK**: Version 17, 21, or 25 installed. Verify with `java -version`.
- **Maven**: Version 3.8+ installed. Verify with `mvn -version`.
- **MySQL Server**: (Optional for unit testing; required for running full console application).

---

## 6. MySQL Setup & Configuration

### Step 1: Initialize Database & Sample Data
Open your terminal or MySQL Workbench and run:
```bash
mysql -u root -p < src/main/resources/db/schema.sql
mysql -u root -p < src/main/resources/db/sample-data.sql
```

### Step 2: Configure Credentials
Copy the example properties file:
```bash
cp src/main/resources/db.properties.example db.properties
```
Edit `db.properties` with your MySQL password:
```properties
db.url=jdbc:mysql://localhost:3306/campusfind?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=your_mysql_password
```
*Alternatively, you can export environment variables without touching any files:*
```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/campusfind?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USER="root"
$env:DB_PASSWORD="your_password"
```

---

## 7. How to Build, Test, and Run

### Run Unit Tests
All 40 business logic and validation tests execute independently using in-memory test doubles without requiring MySQL:
```bash
mvn clean test
```

### Build & Package Standalone JAR
```bash
mvn clean package
```
This produces `target/campusfind-1.0.0.jar` along with runtime dependencies in `target/lib/`.

*Run directly with Maven:*
```bash
mvn exec:java
```

*Or run the standalone JAR:*
```bash
java -jar target/campusfind-1.0.0.jar
```

---

## 8. Example Console Session

```text
==========================================================================================
   ____                                  _____ _           _ 
  / ___|__ _ _ __ ___  _ __  _   _ ___  |  ___(_)_ __   __| |
 | |   / _` | '_ ` _ \| '_ \| | | / __| | |_  | | '_ \ / _` |
 | |__| (_| | | | | | | |_) | |_| \__ \ |  _| | | | | | (_| |
  \____\__,_|_| |_| |_| .__/ \__,_|___/ |_|   |_|_| |_|\__,_|
                      |_|                                    
   Privacy-First Campus Lost & Found Management System
==========================================================================================

-------------------------------- MENU OPTIONS --------------------------------
 1. Report a Found Item
 2. List Available / Open Items
 3. Search Items by Name or Keyword
 4. Filter Items by Category
 5. Filter Items by Location
 6. Submit a Claim for an Item
 7. Review Pending Claims (Privacy-Verified Review)
 8. Approve or Reject a Claim
 9. Mark Item as Returned (Handover to Owner)
 10. Display Statistics Dashboard
 11. View All History & Audit Logs
 12. Exit
------------------------------------------------------------------------------
Please select an option (1-12): 2

=== Available & Claim-Pending Campus Items (5 items found) ===
+------+------------------------------+---------------+------------------------+------------+---------------+
| ID   | Item Name                    | Category      | Location               | Date Found | Status        |
+------+------------------------------+---------------+------------------------+------------+---------------+
| 4    | Brown Leather Bifold Wallet  | Accessories   | Sports Complex - Ba... | 2026-10-13 | Available     |
| 3    | Blue Hydro Flask Water Bo... | Others        | Student Cafeteria      | 2026-10-12 | Available     |
| 2    | Scientific Calculator Cas... | Electronics   | Academic Block A - ... | 2026-10-11 | Claim Pending |
| 1    | Dell Precision Laptop Cha... | Electronics   | Central Library - 2... | 2026-10-10 | Available     |
| 6    | Titan Fastrack Silver Wri... | Accessories   | Lecture Hall Comple... | 2026-10-09 | Available     |
+------+------------------------------+---------------+------------------------+------------+---------------+
NOTE: Private verification clues are confidential and hidden from public view.
```

---

## 9. Interview & Study Documentation

The [`docs/`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/docs/) directory includes complete interview preparation guides:

- [`docs/INTERVIEW-PREP.md`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/docs/INTERVIEW-PREP.md): 30s/90s pitch, 32 comprehensive Q&As covering Core Java, OOP, JDBC, Transactions, and potential follow-up questions.
- [`docs/JAVA-CONCEPTS.md`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/docs/JAVA-CONCEPTS.md): How encapsulation, abstraction, polymorphism, enums, streams, and exceptions are used.
- [`docs/DATABASE.md`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/docs/DATABASE.md): ER diagram, indexing rationale, PreparedStatement security, and transaction design.
- [`docs/CODE-WALKTHROUGH.md`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/docs/CODE-WALKTHROUGH.md): Guided reading order and trace of every feature.
- [`docs/sql-practice.sql`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/docs/sql-practice.sql): 15 interview SQL practice queries with explanations.
- [`docs/BUILD-LOG.md`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/docs/BUILD-LOG.md): Exact compilation and test logs.

---

## 10. Known Limitations & Future Improvements

- **Connection Pool**: Currently creates direct connections via `DriverManager`. In a production web deployment, a pool like HikariCP would reduce connection latency.
- **Console Interface**: Terminal-based interface. In the future, a Spring Boot REST API with a React or Angular frontend could be added.
- **Optimistic Locking**: Concurrent updates are protected via database transactions; high-concurrency environments could add version columns (`version INT`).
