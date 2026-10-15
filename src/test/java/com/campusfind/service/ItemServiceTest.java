package com.campusfind.service;

import com.campusfind.exception.InvalidStatusTransitionException;
import com.campusfind.exception.ItemNotFoundException;
import com.campusfind.model.*;
import com.campusfind.testutil.InMemoryClaimDao;
import com.campusfind.testutil.InMemoryFoundItemDao;
import com.campusfind.testutil.InMemoryLostItemDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ItemService Business Logic Tests")
class ItemServiceTest {

    private InMemoryFoundItemDao itemDao;
    private InMemoryClaimDao claimDao;
    private InMemoryLostItemDao lostItemDao;
    private ValidationService validationService;
    private ItemService itemService;

    @BeforeEach
    void setUp() {
        itemDao = new InMemoryFoundItemDao();
        lostItemDao = new InMemoryLostItemDao();
        claimDao = new InMemoryClaimDao(itemDao, lostItemDao);
        validationService = new ValidationService();
        itemService = new ItemService(itemDao, claimDao, lostItemDao, validationService);
    }

    @Test
    @DisplayName("Reporting an item assigns ID, sets status AVAILABLE, and saves to repository")
    void testReportItemSuccess() {
        FoundItem item = new FoundItem("Casio Calculator", "Electronics", "Scientific calc",
                "Hall 101", LocalDate.now(), "Prof. Kumar", "Serial 1234");

        FoundItem saved = itemService.reportItem(item);

        assertNotNull(saved.getId());
        assertEquals(ItemStatus.AVAILABLE, saved.getStatus());
        assertTrue(itemDao.findById(saved.getId()).isPresent());
    }

    @Test
    @DisplayName("Available items list excludes RETURNED items")
    void testGetAvailableItemsExcludesReturned() {
        FoundItem activeItem = new FoundItem("Wallet", "Accessories", "Brown wallet", "Court",
                LocalDate.now(), "Guard", "DL inside");
        itemService.reportItem(activeItem);

        FoundItem returnedItem = new FoundItem("Keys", "Keys", "Room keys", "Lobby",
                LocalDate.now(), "Guard", "Red keychain");
        FoundItem savedReturned = itemService.reportItem(returnedItem);
        itemDao.updateStatus(savedReturned.getId(), ItemStatus.RETURNED);

        List<FoundItem> available = itemService.getAvailableItems();
        assertEquals(1, available.size());
        assertEquals("Wallet", available.get(0).getItemName());
    }

    @Test
    @DisplayName("markItemAsReturned throws ItemNotFoundException when item does not exist")
    void testMarkReturnedNonExistentItem() {
        assertThrows(ItemNotFoundException.class, () -> itemService.markItemAsReturned(9999));
    }

    @Test
    @DisplayName("markItemAsReturned throws InvalidStatusTransitionException when item already returned")
    void testMarkReturnedAlreadyReturned() {
        FoundItem item = itemService.reportItem(new FoundItem("ID Card", "Documents", "Campus ID",
                "Hostel", LocalDate.now(), "Guard", "Roll 23BCSE108"));
        itemDao.updateStatus(item.getId(), ItemStatus.RETURNED);

        assertThrows(InvalidStatusTransitionException.class,
                () -> itemService.markItemAsReturned(item.getId()));
    }

    @Test
    @DisplayName("markItemAsReturned FAILS if there is NO approved claim for the item")
    void testMarkReturnedFailsWithoutApprovedClaim() {
        FoundItem item = itemService.reportItem(new FoundItem("Smart Watch", "Accessories", "Silver watch",
                "Library", LocalDate.now(), "Attendant", "Back engraving"));

        // Item has no claims, or only pending claims
        claimDao.save(new Claim(item.getId(), "Student", "student@campus.edu", "Lost watch", "Secret answer"));

        InvalidStatusTransitionException ex = assertThrows(InvalidStatusTransitionException.class,
                () -> itemService.markItemAsReturned(item.getId()));

        assertTrue(ex.getMessage().contains("No APPROVED claim was found"),
                "Should enforce that an approved claim must precede return.");
    }

    @Test
    @DisplayName("markItemAsReturned SUCCEEDS when an approved claim exists")
    void testMarkReturnedSucceedsWithApprovedClaim() {
        FoundItem item = itemService.reportItem(new FoundItem("Smart Watch", "Accessories", "Silver watch",
                "Library", LocalDate.now(), "Attendant", "Back engraving"));

        // Create an approved claim
        Claim claim = new Claim(item.getId(), "Student", "student@campus.edu", "Lost watch", "Back engraving");
        claim.setStatus(ClaimStatus.APPROVED);
        claim.setReviewedAt(LocalDateTime.now());
        claimDao.save(claim);

        boolean result = itemService.markItemAsReturned(item.getId());
        assertTrue(result);

        FoundItem updated = itemDao.findById(item.getId()).orElseThrow();
        assertEquals(ItemStatus.RETURNED, updated.getStatus());
    }

    @Test
    @DisplayName("Keyword search finds items by partial name or description")
    void testSearchItems() {
        itemService.reportItem(new FoundItem("Casio Scientific Calculator", "Electronics", "Scientific calc",
                "Hall 101", LocalDate.now(), "Prof", "Clue 1"));
        itemService.reportItem(new FoundItem("Dell Laptop Charger", "Electronics", "Barrel pin 65W",
                "Library", LocalDate.now(), "Prof", "Clue 2"));

        List<FoundItem> results = itemService.searchItems("Calculator");
        assertEquals(1, results.size());
        assertEquals("Casio Scientific Calculator", results.get(0).getItemName());
    }

    @Test
    @DisplayName("markItemAsReturned transitions associated lost item report status to RESOLVED")
    void testMarkReturnedResolvesAssociatedLostItem() {
        // 1. Report lost item
        LostItem lost = new LostItem("Wrist Watch", "Accessories", "Titan gold watch",
                "Cafeteria", LocalDate.now(), "Nikhil", "nikhil@campus.edu", "Scratch on back");
        lostItemDao.save(lost);

        // 2. Report found item
        FoundItem found = itemService.reportItem(new FoundItem("Wrist Watch", "Accessories", "Titan gold watch",
                "Cafeteria", LocalDate.now(), "Staff", "Scratch on back"));

        // 3. Approved claim linked to lost report
        Claim claim = new Claim(found.getId(), lost.getId(), "Nikhil", "nikhil@campus.edu", "My watch", "Scratch on back");
        claim.setStatus(ClaimStatus.APPROVED);
        claim.setReviewedAt(LocalDateTime.now());
        claimDao.save(claim);

        // 4. Mark item returned
        boolean returned = itemService.markItemAsReturned(found.getId());
        assertTrue(returned);

        // 5. Verify lost report is RESOLVED
        LostItem updatedLost = lostItemDao.findById(lost.getId()).orElseThrow();
        assertEquals(LostItemStatus.RESOLVED, updatedLost.getStatus(),
                "Associated lost item must transition to RESOLVED when found item is returned.");
    }
}
