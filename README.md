# CampusFind — Privacy-First Campus Lost & Found Management System

A robust, modular, and privacy-conscious console application built with **Core Java 17+**, **JDBC**, **MySQL**, and **JUnit 5**, designed to demonstrate clean layered architecture, OOP design principles, and transactional database patterns for fresher Java developer interviews.

Supports **both Lost-Item and Found-Item reports**, intelligent matching heuristics, and privacy-preserving claim verification.

---

## 1. Problem Statement

Belongings such as calculators, ID cards, wallets, backpacks, and laptop chargers are frequently misplaced across campus. Existing lost-and-found systems present a major security flaw: **they display all identifying characteristics publicly**. This invites dishonest individuals to falsely claim high-value items simply by repeating details they saw online.

**CampusFind** solves this using **Privacy-First Claim Verification & Integrated Dual-Tracking**:
- **Found Items**: The finder records a **secret verification clue** (e.g. an engraved initial, hidden sticker, or serial number suffix) that is **never shown publicly** or written to logs.
- **Lost Items**: Students register their lost belongings with confidential identifying details, location last seen, date lost, and contact info.
- **Match Suggestions**: A heuristic matching engine calculates correlation scores between open lost reports and available found items without auto-resolving.
- **Claim Linkage & Resolution**: When filing a claim, a student can associate their lost report ID. On claim approval, the lost report status transitions to `MATCHED` atomically. When the physical item is handed over and marked `RETURNED`, the lost report is marked `RESOLVED`.

---

## 2. Key Features

### [Found Items]
1. **Report a Found Item**: Captures item details and confidential verification clues with strict validation.
2. **List Available Items**: Tabular listing of open items (`AVAILABLE` and `CLAIM_PENDING`), strictly omitting secret clues.
3. **Search & Filter Found Items**: Keyword search across item names and descriptions, plus filtering by category and location.

### [Lost Items]
4. **Report a Lost Item**: Captures student lost reports with confidential identifying details, location last seen, date lost, and contact details. Defaults to status `OPEN`.
5. **List Active Lost Reports**: Tabular listing of active student reports (`OPEN` and `MATCHED`), with confidential identifying details safely redacted.
6. **Search & Filter Lost Items**: Separate keyword search and category/location filtering for lost reports.

### [Matching & Claim Resolution]
7. **Potential Match Suggestions**: Evaluates potential matches between lost and found items using multi-factor heuristics (category match, keyword overlap, location similarity, and chronological plausibility). Displays match score (%) and reason breakdown without auto-modifying records.
8. **Submit a Claim (with Lost Report Linkage)**: Students can submit a claim for a found item, optionally linking their open lost report ID. Prevents duplicate active claims and invalid references.
9. **Privacy-Verified Claim Review**: Staff view side-by-side comparison cards comparing the finder's secret clue with the claimant's answer without public exposure.
10. **Atomic Claim Approval**: Atomically sets claim to `APPROVED`, synchronizes item status, auto-rejects competing pending claims, and sets linked lost reports to `MATCHED` in a single ACID transaction with rollback protection.
11. **Safe Return Handover**: Marks item as `RETURNED` only after an approved claim, automatically completing the lifecycle by marking any linked lost report as `RESOLVED`.

### [System & Audit]
12. **Live Statistics Dashboard**: Aggregates found item metrics (`AVAILABLE`, `CLAIM_PENDING`, `RETURNED`), lost item metrics (`OPEN`, `MATCHED`, `RESOLVED`), and claim resolution rates.
13. **Full Audit History**: View complete historical logs of all reported found items, lost items, and claims.

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
       │ (User input, menus & formatted tables)
       ▼
Service Layer (ItemService, LostItemService, ClaimService, ValidationService)
       │ (Business rules, match heuristics, state transitions, privacy filters)
       ▼
DAO Layer (FoundItemDao, LostItemDao, ClaimDao)
       │ (PreparedStatements, Transactions, Entity Mapping, Resource Management)
       ▼
Database Layer (MySQL 8.0: found_items, lost_items, claims)
```

See [`docs/ARCHITECTURE.md`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/docs/ARCHITECTURE.md) for full architectural sequence diagrams and responsibilities.

---

## 5. Prerequisites

- **Java JDK**: Version 17, 21, or 25 installed. Verify with `java -version`.
- **Maven**: Version 3.8+ installed. Verify with `mvn -version`.
- **MySQL Server**: (Optional for unit testing; required for running full console application).

---

## 6. MySQL Setup & Configuration

### For Existing Databases (Non-Destructive Migration)
If you already ran version 1 of CampusFind, apply the migration script to add lost items and claim linkage **without losing existing data**:
```bash
mysql -u root -p campusfind < src/main/resources/db/migration_v2_lost_items.sql
```

### For Fresh Installations
Open your terminal or MySQL client and run:
```bash
mysql -u root -p < src/main/resources/db/schema.sql
mysql -u root -p < src/main/resources/db/sample-data.sql
```

### Database Credentials Configuration
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
*Alternatively, you can export environment variables:*
```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/campusfind?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USER="root"
$env:DB_PASSWORD="your_password"
```

---

## 7. How to Build, Test, and Run

### Run Unit Tests
All 73 business logic, heuristic matching, validation, and transaction tests execute independently using in-memory test doubles without requiring MySQL:
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

## 8. Interactive Console Menu

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
 [FOUND ITEMS]
  1. Report a Found Item
  2. List Available / Open Found Items
  3. Search & Filter Found Items (Keyword / Category / Location)
 [LOST ITEMS]
  4. Report a Lost Item (Student Report)
  5. List Active Lost-Item Reports
  6. Search & Filter Lost Items (Keyword / Category / Location)
 [MATCHING & CLAIMS]
  7. Find Potential Matches (Suggest Matches between Lost & Found)
  8. Submit a Claim for an Item (with optional Lost Report ID)
  9. Review Pending Claims (Privacy-Verified Review)
 10. Approve or Reject a Claim
 11. Mark Item as Returned (Handover to Owner & Resolve Lost Report)
 [SYSTEM & AUDIT]
 12. Display Live Statistics Dashboard
 13. View All History & Audit Logs (Found, Lost & Claims)
 14. Exit
------------------------------------------------------------------------------
```

---

## 9. Interview & Study Documentation

The [`docs/`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/docs/) directory includes complete interview preparation guides:

- [`docs/INTERVIEW-PREP.md`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/docs/INTERVIEW-PREP.md): 30s/90s elevator pitch, comprehensive Q&As covering Core Java, OOP, JDBC, Transactions, Lost-Item Integration, Heuristics, and follow-ups.
- [`docs/JAVA-CONCEPTS.md`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/docs/JAVA-CONCEPTS.md): How encapsulation, abstraction, polymorphism, enums, streams, and exceptions are used.
- [`docs/DATABASE.md`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/docs/DATABASE.md): ER diagram with `lost_items`, indexing rationale, PreparedStatement security, non-destructive migration, and transaction design.
- [`docs/ARCHITECTURE.md`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/docs/ARCHITECTURE.md): Architecture layers, DTOs, heuristic matching design, and sequence diagrams.
- [`docs/CODE-WALKTHROUGH.md`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/docs/CODE-WALKTHROUGH.md): Guided reading order and trace of every feature.
- [`docs/sql-practice.sql`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/docs/sql-practice.sql): Interview SQL practice queries including joins between lost items, found items, and claims.
- [`docs/BUILD-LOG.md`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/docs/BUILD-LOG.md): Exact compilation and test logs for all 73 tests.
