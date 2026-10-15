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
```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.campusfind.model.ClaimTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.091 s -- in com.campusfind.model.ClaimTest
[INFO] Running com.campusfind.model.FoundItemTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.029 s -- in com.campusfind.model.FoundItemTest
[INFO] Running com.campusfind.model.ItemStatusTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.120 s -- in com.campusfind.model.ItemStatusTest
[INFO] Running com.campusfind.service.ClaimServiceTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.063 s -- in com.campusfind.service.ClaimServiceTest
[INFO] Running com.campusfind.service.ItemServiceTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.052 s -- in com.campusfind.service.ItemServiceTest
[INFO] Running com.campusfind.service.ValidationServiceTest
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.086 s -- in com.campusfind.service.ValidationServiceTest
[INFO] Running com.campusfind.util.DateUtilTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.011 s -- in com.campusfind.util.DateUtilTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 40, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  18.524 s
```

---

## 3. Package & Assembly Log

**Command Executed**:
```powershell
mvn clean package
```

**Results**:
```
[INFO] --- maven-jar-plugin:3.3.0:jar (default-jar) @ campusfind ---
[INFO] Building jar: D:\desk\TalkAbtTech\PROJECT-S\campusFind\target\campusfind-1.0.0.jar
[INFO] --- maven-dependency-plugin:3.6.1:copy-dependencies (copy-dependencies) @ campusfind ---
[INFO] Copying mysql-connector-j-8.3.0.jar to D:\desk\TalkAbtTech\PROJECT-S\campusFind\target\lib\mysql-connector-j-8.3.0.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] Total time:  20.363 s
```

---

## 4. Standalone JAR Execution Check

**Command Executed**:
```powershell
java -jar target/campusfind-1.0.0.jar
```

**Observation**:
The application booted successfully, parsed configuration, tested database connectivity via `ConnectionFactory.testConnection()`, identified that local MySQL was not running, printed the banner, displayed clear diagnostic guidance on how to start MySQL and run the schema scripts, and exited cleanly with exit code 0.

---

## 5. Maven Exec Plugin Execution Check

**Command Executed**:
```powershell
mvn exec:java
```

**Results**:
- Successfully resolved `mainClass` to `com.campusfind.Main` via `exec-maven-plugin`.
- Booted application, verified database check, and executed cleanup.
- Build status: `BUILD SUCCESS` (0.775 s).
