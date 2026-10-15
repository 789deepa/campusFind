# CampusFind — Build & Verification Log

This document records the exact build, test, and verification commands executed in the environment, along with tool versions and validation results.

---

## 1. Environment Details

- **Operating System**: Windows 11 (amd64)
- **Java Runtime**: OpenJDK 25.0.4 (JBR-25.0.4+1-508.27-nomod)
- **Java Compiler**: javac 25.0.4 (Bytecode compatibility targeted: Java 17 LTS)
- **Build Tool**: Apache Maven 3.9.9
- **Database Driver**: MySQL Connector/J 8.3.0
- **Test Framework**: JUnit 5 Jupiter 5.10.2

---

## 2. Test Execution Log

**Command Executed**:
```powershell
mvn clean test
```

**Results**:
```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.campusfind.model.ClaimTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.099 s -- in com.campusfind.model.ClaimTest
[INFO] Running com.campusfind.model.FoundItemTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.042 s -- in com.campusfind.model.FoundItemTest
[INFO] Running com.campusfind.model.ItemStatusTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.084 s -- in com.campusfind.model.ItemStatusTest
[INFO] Running com.campusfind.model.LostItemStatusTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.038 s -- in com.campusfind.model.LostItemStatusTest
[INFO] Running com.campusfind.model.LostItemTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.021 s -- in com.campusfind.model.LostItemTest
[INFO] Running com.campusfind.service.ClaimServiceTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.101 s -- in com.campusfind.service.ClaimServiceTest
[INFO] Running com.campusfind.service.ItemServiceTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.103 s -- in com.campusfind.service.ItemServiceTest
[INFO] Running com.campusfind.service.LostItemServiceTest
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.104 s -- in com.campusfind.service.LostItemServiceTest
[INFO] Running com.campusfind.service.ValidationServiceTest
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.070 s -- in com.campusfind.service.ValidationServiceTest
[INFO] Running com.campusfind.util.DateUtilTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.038 s -- in com.campusfind.util.DateUtilTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 73, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  6.387 s
```

---

## 3. Package & Assembly Log

**Command Executed**:
```powershell
mvn clean package
```

**Results**:
```text
[INFO] --- maven-jar-plugin:3.3.0:jar (default-jar) @ campusfind ---
[INFO] Building jar: D:\desk\TalkAbtTech\PROJECT-S\campusFind\target\campusfind-1.0.0.jar
[INFO] --- maven-dependency-plugin:3.6.1:copy-dependencies (copy-dependencies) @ campusfind ---
[INFO] Copying mysql-connector-j-8.3.0.jar to D:\desk\TalkAbtTech\PROJECT-S\campusFind\target\lib\mysql-connector-j-8.3.0.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

---

## 4. Application Interactive Execution Verification

**Command Executed**:
```powershell
mvn exec:java
```

**Terminal Output**:
```text
Starting CampusFind Application...
[2026-10-15 23:08:17] [INFO   ] Loaded database configuration from classpath db.properties
Target Database URL : jdbc:mysql://localhost:3306/campusfind?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
Target Database User: root
[+] Database connection verified successfully.

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
Please select an option (1-14): 14
Thank you for using CampusFind! Have a great day.
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```
