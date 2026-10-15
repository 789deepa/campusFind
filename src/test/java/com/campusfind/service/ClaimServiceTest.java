package com.campusfind.service;

import com.campusfind.exception.*;
import com.campusfind.model.*;
import com.campusfind.testutil.InMemoryClaimDao;
import com.campusfind.testutil.InMemoryFoundItemDao;
import com.campusfind.testutil.InMemoryLostItemDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ClaimService Business Logic & Transaction Tests")
class ClaimServiceTest {

    private InMemoryFoundItemDao itemDao;
    private InMemoryClaimDao claimDao;
    private InMemoryLostItemDao lostItemDao;
    private ValidationService validationService;
    private ClaimService claimService;
    private FoundItem sampleItem;

    @BeforeEach
    void setUp() {
        itemDao = new InMemoryFoundItemDao();
        lostItemDao = new InMemoryLostItemDao();
        claimDao = new InMemoryClaimDao(itemDao, lostItemDao);
        validationService = new ValidationService();
        claimService = new ClaimService(claimDao, itemDao, lostItemDao, validationService);

        sampleItem = new FoundItem("Casio Calculator", "Electronics", "Scientific fx-991EX",
                "Hall 101", LocalDate.now(), "Prof. Kumar", "Roll number 442 in pencil");
        itemDao.save(sampleItem);
    }

    @Test
    @DisplayName("Submitting a claim saves it as PENDING and updates item status to CLAIM_PENDING")
    void testSubmitClaimSuccess() {
        Claim claim = new Claim(sampleItem.getId(), "Kavya Patel", "kavya@campus.edu",
                "Left it on desk 4", "Roll number 442 in pencil inside cover");

        Claim saved = claimService.submitClaim(claim);

        assertNotNull(saved.getId());
        assertEquals(ClaimStatus.PENDING, saved.getStatus());

        FoundItem updatedItem = itemDao.findById(sampleItem.getId()).orElseThrow();
        assertEquals(ItemStatus.CLAIM_PENDING, updatedItem.getStatus());
    }

    @Test
    @DisplayName("Cannot submit a claim for a non-existent item ID")
    void testSubmitClaimNonExistentItem() {
        Claim claim = new Claim(9999, "Kavya", "kavya@campus.edu", "Story", "Answer");
        assertThrows(ItemNotFoundException.class, () -> claimService.submitClaim(claim));
    }

    @Test
    @DisplayName("Cannot submit a claim for an already RETURNED item")
    void testSubmitClaimForReturnedItem() {
        itemDao.updateStatus(sampleItem.getId(), ItemStatus.RETURNED);

        Claim claim = new Claim(sampleItem.getId(), "Kavya", "kavya@campus.edu", "Story", "Answer");
        InvalidStatusTransitionException ex = assertThrows(InvalidStatusTransitionException.class,
                () -> claimService.submitClaim(claim));
        assertTrue(ex.getMessage().contains("already been returned"));
    }

    @Test
    @DisplayName("Cannot submit duplicate pending claims from the same claimant for the same item")
    void testSubmitDuplicateClaimFails() {
        Claim claim1 = new Claim(sampleItem.getId(), "Kavya Patel", "kavya@campus.edu", "Story 1", "Answer");
        claimService.submitClaim(claim1);

        Claim claim2 = new Claim(sampleItem.getId(), "Kavya Patel", "kavya@campus.edu", "Story 2", "Answer");
        DuplicateClaimException ex = assertThrows(DuplicateClaimException.class,
                () -> claimService.submitClaim(claim2));
        assertTrue(ex.getMessage().contains("already have an active pending claim"));
    }

    @Test
    @DisplayName("Approving a claim marks it APPROVED and auto-rejects other competing claims")
    void testApproveClaimWithCompetingClaims() {
        // Claimant 1
        Claim claim1 = claimService.submitClaim(new Claim(sampleItem.getId(), "Claimant One",
                "one@campus.edu", "My calculator", "Right answer: Roll 442"));

        // Claimant 2 (competing claim)
        Claim claim2 = claimService.submitClaim(new Claim(sampleItem.getId(), "Claimant Two",
                "two@campus.edu", "My calculator", "Wrong answer"));

        // Approve claimant 1
        boolean approved = claimService.approveClaim(claim1.getId());
        assertTrue(approved);

        Claim updated1 = claimDao.findById(claim1.getId()).orElseThrow();
        assertEquals(ClaimStatus.APPROVED, updated1.getStatus());

        Claim updated2 = claimDao.findById(claim2.getId()).orElseThrow();
        assertEquals(ClaimStatus.REJECTED, updated2.getStatus(),
                "Competing pending claim should be auto-rejected upon approval of rightful owner.");
    }

    @Test
    @DisplayName("Rejecting a claim when no other claims exist restores item status to AVAILABLE")
    void testRejectClaimRestoresAvailableStatus() {
        Claim claim = claimService.submitClaim(new Claim(sampleItem.getId(), "Claimant One",
                "one@campus.edu", "Story", "Wrong clue"));

        assertEquals(ItemStatus.CLAIM_PENDING, itemDao.findById(sampleItem.getId()).orElseThrow().getStatus());

        boolean rejected = claimService.rejectClaim(claim.getId());
        assertTrue(rejected);

        assertEquals(ClaimStatus.REJECTED, claimDao.findById(claim.getId()).orElseThrow().getStatus());
        assertEquals(ItemStatus.AVAILABLE, itemDao.findById(sampleItem.getId()).orElseThrow().getStatus(),
                "Item status should revert to AVAILABLE when no pending claims remain.");
    }

    @Test
    @DisplayName("Submitting claim with valid lostReportId succeeds")
    void testSubmitClaimWithValidLostReportId() {
        LostItem lost = new LostItem("Casio 991EX", "Electronics", "Lost in 101",
                "Hall 101", LocalDate.now(), "Kavya", "kavya@campus.edu", "Marks");
        lostItemDao.save(lost);

        Claim claim = new Claim(sampleItem.getId(), lost.getId(), "Kavya", "kavya@campus.edu", "My calc", "Roll 442");
        Claim saved = claimService.submitClaim(claim);

        assertNotNull(saved.getId());
        assertEquals(lost.getId(), saved.getLostReportId());
    }

    @Test
    @DisplayName("Submitting claim with non-existent lostReportId throws LostItemNotFoundException")
    void testSubmitClaimWithNonExistentLostReportIdFails() {
        Claim claim = new Claim(sampleItem.getId(), 9999, "Kavya", "kavya@campus.edu", "My calc", "Roll 442");
        assertThrows(LostItemNotFoundException.class, () -> claimService.submitClaim(claim));
    }

    @Test
    @DisplayName("Submitting claim with already RESOLVED lostReportId throws InvalidStatusTransitionException")
    void testSubmitClaimWithResolvedLostReportIdFails() {
        LostItem lost = new LostItem("Casio 991EX", "Electronics", "Lost in 101",
                "Hall 101", LocalDate.now(), "Kavya", "kavya@campus.edu", "Marks");
        lostItemDao.save(lost);
        lostItemDao.updateStatus(lost.getId(), LostItemStatus.RESOLVED);

        Claim claim = new Claim(sampleItem.getId(), lost.getId(), "Kavya", "kavya@campus.edu", "My calc", "Roll 442");
        assertThrows(InvalidStatusTransitionException.class, () -> claimService.submitClaim(claim));
    }

    @Test
    @DisplayName("Approving a claim associated with a lost report updates lost item status to MATCHED")
    void testApproveClaimUpdatesAssociatedLostReportToMatched() {
        LostItem lost = new LostItem("Casio 991EX", "Electronics", "Lost in 101",
                "Hall 101", LocalDate.now(), "Kavya", "kavya@campus.edu", "Marks");
        lostItemDao.save(lost);

        Claim claim = claimService.submitClaim(new Claim(sampleItem.getId(), lost.getId(), "Kavya",
                "kavya@campus.edu", "My calc", "Roll 442"));

        boolean approved = claimService.approveClaim(claim.getId());
        assertTrue(approved);

        LostItem updatedLost = lostItemDao.findById(lost.getId()).orElseThrow();
        assertEquals(LostItemStatus.MATCHED, updatedLost.getStatus(),
                "Associated lost report must transition to MATCHED upon claim approval");
    }
}
