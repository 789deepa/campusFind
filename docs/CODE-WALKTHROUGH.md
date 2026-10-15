# CampusFind — Source Code Walkthrough & Learning Guide

This document provides a guided tour through the **CampusFind** codebase. It walks through every major source file and traces the exact execution paths of key features to help you understand and explain the implementation.

---

## 1. Guided Source Code Reading Order

To understand the project from the outside in, read the files in this logical sequence:

1. **Configuration & Entry**:
   - [`pom.xml`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/pom.xml) — Dependencies (MySQL Connector/J, JUnit 5) and build plugins.
   - [`DatabaseConfig.java`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/config/DatabaseConfig.java) — How environment variables and properties are loaded.
   - [`ConnectionFactory.java`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/config/ConnectionFactory.java) — Driver loading, connection creation, health checks.
   - [`Main.java`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/Main.java) — Application startup, diagnostic health check, manual dependency injection.

2. **Domain Model & Exceptions**:
   - [`ItemStatus.java`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/model/ItemStatus.java) & [`ClaimStatus.java`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/model/ClaimStatus.java) — Lifecycle enums.
   - [`FoundItem.java`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/model/FoundItem.java) — Note `privateVerificationClue` encapsulation and `[REDACTED]` in `toString()`.
   - [`Claim.java`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/model/Claim.java) — Student claim entity with hidden `verificationResponse`.
   - [`CampusFindException.java`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/exception/CampusFindException.java) & subclasses — Domain exception hierarchy.

3. **Data Access Layer (DAO)**:
   - [`FoundItemDao.java`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/dao/FoundItemDao.java) — Contract for item queries and statistics.
   - [`FoundItemDaoJdbcImpl.java`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/dao/FoundItemDaoJdbcImpl.java) — JDBC implementation with `PreparedStatement` and `try-with-resources`.
   - [`ClaimDao.java`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/dao/ClaimDao.java) & [`ClaimDaoJdbcImpl.java`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/dao/ClaimDaoJdbcImpl.java) — Notice `approveClaimAtomic()` with explicit ACID transaction.

4. **Service & Validation Layer**:
   - [`ValidationService.java`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/service/ValidationService.java) — Business validations (dates, required fields).
   - [`ItemService.java`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/service/ItemService.java) — Invariant checks (e.g. return verification).
   - [`ClaimService.java`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/service/ClaimService.java) — Submission, duplicate check, and approval dispatch.

5. **Presentation (UI) Layer**:
   - [`ConsolePrinter.java`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/ui/ConsolePrinter.java) — Formatted tables and confidential review card.
   - [`ConsoleMenu.java`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/ui/ConsoleMenu.java) — User interaction loops and menu routing.

6. **Automated Tests**:
   - [`FoundItemTest.java`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/test/java/com/campusfind/model/FoundItemTest.java) — Verifies secret clue redaction.
   - [`ItemServiceTest.java`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/test/java/com/campusfind/service/ItemServiceTest.java) — Tests business invariants with in-memory double.
   - [`ClaimServiceTest.java`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/test/java/com/campusfind/service/ClaimServiceTest.java) — Tests competing claims auto-rejection.

---

## 2. Execution Traces of Major Features

### Feature 1: Reporting a Found Item
1. **Entry**: User selects Option `1` in [`ConsoleMenu.run()`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/ui/ConsoleMenu.java).
2. **Input Capture**: `ConsoleMenu.handleReportItem()` prompts for item name, category, description, location, date, finder name, and the **private verification clue**.
3. **Service Validation**: Invokes [`ItemService.reportItem(item)`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/service/ItemService.java#L47).
   - `ItemService` delegates to [`ValidationService.validateFoundItem(item)`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/service/ValidationService.java#L21).
   - Checks that `dateFound` is not in the future (`isAfter(LocalDate.now())`), and all fields meet length requirements.
4. **Persistence**: `ItemService` sets `status = AVAILABLE` and calls [`FoundItemDaoJdbcImpl.save(item)`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/dao/FoundItemDaoJdbcImpl.java#L53).
   - Opens connection via `ConnectionFactory.getConnection()`.
   - Executes SQL `INSERT INTO found_items (...)` with `Statement.RETURN_GENERATED_KEYS`.
   - Reads the auto-increment ID from `ps.getGeneratedKeys()` and assigns it to `item.setId(...)`.
5. **Output**: UI prints a receipt with the assigned item ID via `ConsolePrinter.printSuccess(...)`.

---

### Feature 2: Privacy-Conscious Claim Submission
1. **Entry**: User selects Option `6` in `ConsoleMenu`.
2. **Pre-checks**:
   - Prompts for item ID.
   - [`ItemService.getItemById(itemId)`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/service/ItemService.java#L61) ensures the item exists.
   - Prevents claiming if `item.getStatus() == ItemStatus.RETURNED`.
3. **Capture**: User enters their name, contact, context story, and their **private answer** to the secret clue.
4. **Service Verification**: [`ClaimService.submitClaim(claim)`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/service/ClaimService.java#L45):
   - Validates fields via `ValidationService.validateClaim(claim)`.
   - Checks [`claimDao.hasActiveClaim(itemId, claimantContact)`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/dao/ClaimDaoJdbcImpl.java#L108). If true, throws [`DuplicateClaimException`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/exception/DuplicateClaimException.java).
   - Saves claim with status `PENDING`.
   - Transitions item status from `AVAILABLE` to `CLAIM_PENDING`.
5. **Output**: UI shows confirmation that claim is pending review.

---

### Feature 3: Staff Review & Atomic Claim Approval
1. **Entry**: Campus staff selects Option `8` ("Approve or Reject a Claim").
2. **Review Display**:
   - Staff enters Claim ID.
   - System fetches the claim and referenced item.
   - [`ConsolePrinter.printVerificationReviewCard(item, claim)`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/ui/ConsolePrinter.java#L98) displays:
     ```
     [!] Finder's Secret Clue : Has blue electrical tape near connector
     [?] Claimant's Answer    : Blue tape wrapped near the jack
     ```
3. **Approval Execution**: Staff types `A` (Approve).
   - Calls [`ClaimService.approveClaim(claimId)`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/service/ClaimService.java#L87).
   - Dispatches to [`ClaimDaoJdbcImpl.approveClaimAtomic(claimId, itemId)`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/dao/ClaimDaoJdbcImpl.java#L133).
   - `conn.setAutoCommit(false)` starts the transaction.
   - Sets target claim to `APPROVED`.
   - Sets item to `CLAIM_PENDING`.
   - Sets competing pending claims for that item to `REJECTED`.
   - `conn.commit()` finalizes the changes atomically.
   - If any SQL step fails, `conn.rollback()` restores prior state.

---

### Feature 4: Handover & Returning an Item
1. **Entry**: Staff selects Option `9` ("Mark Item as Returned").
2. **Invariant Check**:
   - Prompts for Item ID.
   - Calls [`ItemService.markItemAsReturned(itemId)`](file:///d:/desk/TalkAbtTech/PROJECT-S/campusFind/src/main/java/com/campusfind/service/ItemService.java#L104).
   - Checks:
     ```java
     boolean hasApprovedClaim = claimDao.findByItemId(itemId).stream()
             .anyMatch(c -> c.getStatus() == ClaimStatus.APPROVED);
     ```
   - If `hasApprovedClaim` is false, it throws `InvalidStatusTransitionException` preventing staff from marking an item returned without prior verification!
3. **Finalization**:
   - If approved claim exists, sets item status to `RETURNED`.
   - The item is now archived and will no longer appear in public available listings.
