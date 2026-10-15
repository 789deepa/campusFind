# Core Java & OOP Concepts in CampusFind

This document details every major Core Java and Object-Oriented Programming (OOP) concept utilized in **CampusFind**, linking each concept directly to the actual classes and methods in the codebase. This serves as a study guide for fresher technical interviews.

---

## 1. Encapsulation & Data Hiding

### Concept
Encapsulation involves bundling data (fields) and methods operating on that data within a single class, while restricting direct access to internal state from outside the class using `private` access modifiers.

### Code Demonstration
- **Classes**: [`FoundItem.java`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/model/FoundItem.java), [`Claim.java`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/model/Claim.java).
- **Implementation**:
  - Fields such as `id`, `itemName`, `privateVerificationClue`, `verificationResponse` are declared `private`.
  - State is modified only via setters or constructors.
  - **Privacy Redaction in `toString()`**:
    ```java
    @Override
    public String toString() {
        return "FoundItem{" +
                "id=" + id +
                ", itemName='" + itemName + '\'' +
                ", privateVerificationClue='[REDACTED]'" +
                ", status=" + status +
                '}';
    }
    ```
    This prevents accidental disclosure of secret clues into log files or console streams.
  - Controlled access: The secret clue is only exposed through [`getPrivateVerificationClue()`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/model/FoundItem.java#L125) when explicitly invoked by authorized review routines.

---

## 2. Abstraction & Programming to Interfaces

### Concept
Abstraction hides implementation complexity and shows only essential functionality. By programming to interfaces rather than concrete classes, high-level modules remain decoupled from low-level database details.

### Code Demonstration
- **Interfaces**: [`FoundItemDao.java`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/dao/FoundItemDao.java), [`ClaimDao.java`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/dao/ClaimDao.java).
- **Service Dependency**:
  ```java
  public class ItemService {
      private final FoundItemDao foundItemDao; // Depends on interface, NOT JDBC implementation!
      private final ClaimDao claimDao;
      ...
  }
  ```
- **Interview Value**: Why is this useful?
  1. Allows swapping the database implementation (e.g. from MySQL to PostgreSQL or MongoDB) without changing a single line of business code in `ItemService`.
  2. Enables fast unit testing via in-memory test doubles ([`InMemoryFoundItemDao`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/test/java/com/campusfind/testutil/InMemoryFoundItemDao.java)), allowing 40 tests to run in under 300ms without needing a live MySQL server.

---

## 3. Polymorphism

### Concept
Polymorphism allows objects of different classes to be treated as objects of a common superclass or interface, executing the appropriate implementation at runtime.

### Code Demonstration
- Both [`FoundItemDaoJdbcImpl`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/dao/FoundItemDaoJdbcImpl.java) (in production) and [`InMemoryFoundItemDao`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/test/java/com/campusfind/testutil/InMemoryFoundItemDao.java) (in unit tests) implement `FoundItemDao`.
- At runtime, `itemService.getAvailableItems()` calls `foundItemDao.findAllAvailable()`. If running in production, JDBC queries MySQL; if running in unit tests, Java Streams filter an in-memory map.

---

## 4. Enums vs Arbitrary Strings

### Concept
Enums represent a fixed set of named constants.

### Code Demonstration
- **Enums**: [`ItemStatus`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/model/ItemStatus.java), [`ClaimStatus`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/model/ClaimStatus.java), [`ItemCategory`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/model/ItemCategory.java).
- **Why Enums Are Superior to Strings**:
  1. **Compile-time Type Safety**: Passing an invalid status like `"PENDING_RETURN"` fails at compile time.
  2. **No Magic Strings**: Eliminates typos like `"Available "` (trailing whitespace) causing silent bugs.
  3. **Rich Behavior**: Enums in Java can contain fields (such as `displayName`) and helper methods (like `fromString()` or state verification logic).

---

## 5. Collections Framework & Java Streams

### Concept
Managing dynamic groups of entities using `List`, `Map`, `ArrayList`, and `LinkedHashMap`, combined with declarative Stream transformations.

### Code Demonstration
- `List<FoundItem>`: Returned by DAO queries and filtered using Java Streams.
- `Stream.anyMatch()`:
  ```java
  boolean hasApprovedClaim = claimDao.findByItemId(itemId).stream()
          .anyMatch(c -> c.getStatus() == ClaimStatus.APPROVED);
  ```
  Declarative, readable check verifying if an approved claim exists before allowing an item to be returned.
- `Map<Integer, FoundItem>`: Used in [`InMemoryFoundItemDao`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/test/java/com/campusfind/testutil/InMemoryFoundItemDao.java) using `LinkedHashMap` to maintain insertion order while offering $O(1)$ lookup by primary key ID.

---

## 6. Generics & Optional

### Concept
Generics ensure type safety by eliminating manual casting. `Optional<T>` is a container object used to represent the possible absence of a value, preventing `NullPointerException` (the billion-dollar mistake).

### Code Demonstration
- **DAO Signatures**:
  ```java
  Optional<FoundItem> findById(Integer id);
  ```
- **Service Usage**:
  ```java
  FoundItem item = foundItemDao.findById(itemId)
          .orElseThrow(() -> new ItemNotFoundException(itemId));
  ```
  Clearly communicates to the caller that an item might not exist, requiring explicit handling.

---

## 7. Exception Handling: Custom Exception Hierarchy

### Concept
Separating error handling from normal business flow using clear, domain-specific exception types.

### Code Demonstration
```
Throwable
  └── Exception
        └── RuntimeException (Unchecked)
              └── CampusFindException (Base Application Exception)
                    ├── ItemNotFoundException
                    ├── ClaimNotFoundException
                    ├── InvalidStatusTransitionException
                    ├── DuplicateClaimException
                    ├── ValidationException
                    └── DatabaseOperationException
```
- **Why Unchecked Exceptions (`RuntimeException`)?**
  Prevents cluttering service and DAO method signatures with checked exception throws clauses, while still allowing the UI layer (`ConsoleMenu`) to catch `CampusFindException` at the top level and display friendly error messages without crashing the program.
- **Exception Wrapping (Chaining)**:
  In `FoundItemDaoJdbcImpl`, low-level checked `SQLException` instances are caught and wrapped into `DatabaseOperationException` preserving the original stack trace via `super(message, cause)`.

---

## 8. Modern Date & Time API (`java.time`)

### Concept
Java 8 introduced `java.time` (`LocalDate`, `LocalDateTime`, `DateTimeFormatter`) to replace legacy, mutable, and thread-unsafe classes (`java.util.Date`, `java.text.SimpleDateFormat`).

### Code Demonstration
- [`DateUtil.java`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/util/DateUtil.java):
  ```java
  public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  ```
  `DateTimeFormatter` is completely thread-safe and immutable.
- `LocalDate`: Models calendar dates without timezones (e.g. `dateFound: 2026-10-15`).
- `LocalDateTime`: Models exact timestamps (e.g. `dateReported`, `submittedAt`).
- Validation rule: `item.getDateFound().isAfter(LocalDate.now())` cleanly checks if a date is mistakenly set in the future.

---

## 9. JDBC & Resource Management

### Concept
Using JDBC properly: `PreparedStatement` to prevent SQL Injection, and `try-with-resources` to ensure deterministic closure of database resources.

### Code Demonstration
- **Automatic Resource Closure**:
  ```java
  try (Connection conn = ConnectionFactory.getConnection();
       PreparedStatement ps = conn.prepareStatement(SQL)) {
      ...
  } // conn and ps are automatically closed here even if an exception is thrown!
  ```
- **SQL Injection Prevention**:
  Instead of string concatenation (`"WHERE name = '" + name + "'"`), parameters are parameterized with placeholders (`?`):
  ```java
  ps.setString(1, "%" + keyword.trim() + "%");
  ```
  The database driver escapes input characters, treating user inputs purely as literal data rather than executable SQL code.

---

## 10. Thread-Safe Singletons & Utility Classes

### Code Demonstration
- [`DatabaseConfig.java`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/config/DatabaseConfig.java): Implements a thread-safe Singleton pattern using synchronized lazy initialization so configuration properties are loaded once into memory.
- [`DateUtil.java`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/util/DateUtil.java), [`LoggerUtil.java`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/util/LoggerUtil.java):
  Declared `final` with private constructors to prevent accidental instantiation, serving purely as utility containers.
