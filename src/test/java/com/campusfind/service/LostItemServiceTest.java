package com.campusfind.service;

import com.campusfind.exception.LostItemNotFoundException;
import com.campusfind.exception.ValidationException;
import com.campusfind.model.*;
import com.campusfind.testutil.InMemoryFoundItemDao;
import com.campusfind.testutil.InMemoryLostItemDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LostItemService Business Logic & Heuristic Matching Tests")
class LostItemServiceTest {

    private InMemoryLostItemDao lostItemDao;
    private InMemoryFoundItemDao foundItemDao;
    private ValidationService validationService;
    private LostItemService lostItemService;

    @BeforeEach
    void setUp() {
        lostItemDao = new InMemoryLostItemDao();
        foundItemDao = new InMemoryFoundItemDao();
        validationService = new ValidationService();
        lostItemService = new LostItemService(lostItemDao, foundItemDao, validationService);
    }

    @Test
    @DisplayName("Reporting a lost item sets status to OPEN, assigns ID, and saves to repository")
    void testReportLostItemSuccess() {
        LostItem item = new LostItem("Navy Blue Backpack", "Bags", "Nike backpack with water bottle pocket",
                "Central Library", LocalDate.now().minusDays(1), "Aman Gupta", "aman@campus.edu",
                "Silver keychain attached to zipper with initials AG");

        LostItem saved = lostItemService.reportLostItem(item);

        assertNotNull(saved.getId());
        assertEquals(LostItemStatus.OPEN, saved.getStatus());
        assertTrue(lostItemDao.findById(saved.getId()).isPresent());
    }

    @Test
    @DisplayName("Reporting invalid lost item throws ValidationException")
    void testReportInvalidLostItemThrows() {
        LostItem item = new LostItem("", "Bags", "Desc", "Loc", LocalDate.now(), "Name", "Contact", "Clue");
        assertThrows(ValidationException.class, () -> lostItemService.reportLostItem(item));
    }

    @Test
    @DisplayName("getOpenLostItems returns OPEN and MATCHED items, but excludes RESOLVED items")
    void testGetOpenLostItemsExcludesResolved() {
        LostItem openItem = new LostItem("Watch", "Accessories", "Casio digital watch",
                "Cafeteria", LocalDate.now(), "Ravi", "ravi@campus.edu", "Clue1");
        lostItemService.reportLostItem(openItem);

        LostItem resolvedItem = new LostItem("Umbrella", "Accessories", "Black folding umbrella",
                "Auditorium", LocalDate.now(), "Neha", "neha@campus.edu", "Clue2");
        LostItem savedResolved = lostItemService.reportLostItem(resolvedItem);
        lostItemDao.updateStatus(savedResolved.getId(), LostItemStatus.RESOLVED);

        List<LostItem> active = lostItemService.getOpenLostItems();
        assertEquals(1, active.size());
        assertEquals("Watch", active.get(0).getItemName());
    }

    @Test
    @DisplayName("Keyword search filters lost items by name, description, or location")
    void testSearchLostItems() {
        lostItemService.reportLostItem(new LostItem("Hydro Flask Bottle", "Others", "Blue insulated bottle",
                "Sports Complex", LocalDate.now(), "Arun", "arun@campus.edu", "Scratch at base"));
        lostItemService.reportLostItem(new LostItem("Scientific Calculator", "Electronics", "Casio 991EX",
                "Exam Hall", LocalDate.now(), "Bhavna", "bhavna@campus.edu", "White sticker"));

        List<LostItem> results = lostItemService.searchLostItems("Flask");
        assertEquals(1, results.size());
        assertEquals("Hydro Flask Bottle", results.get(0).getItemName());

        List<LostItem> descResults = lostItemService.searchLostItems("insulated");
        assertEquals(1, descResults.size());
        assertEquals("Hydro Flask Bottle", descResults.get(0).getItemName());

        List<LostItem> locationResults = lostItemService.filterByLocation("Sports");
        assertEquals(1, locationResults.size());
        assertEquals("Hydro Flask Bottle", locationResults.get(0).getItemName());
    }

    @Test
    @DisplayName("Filter lost items by category")
    void testFilterByCategory() {
        lostItemService.reportLostItem(new LostItem("Novel", "Books", "DSA in Java",
                "Library", LocalDate.now(), "Sanjay", "sanjay@campus.edu", "Cover torn"));
        lostItemService.reportLostItem(new LostItem("Notebook", "Books", "Class notes",
                "Class 201", LocalDate.now(), "Pooja", "pooja@campus.edu", "Spiral bound"));
        lostItemService.reportLostItem(new LostItem("Earphones", "Electronics", "Wireless earbuds",
                "Lawn", LocalDate.now(), "Kunal", "kunal@campus.edu", "Red case"));

        List<LostItem> books = lostItemService.filterByCategory("Books");
        assertEquals(2, books.size());
    }

    @Test
    @DisplayName("findPotentialMatchesForLost suggests matching found item with high score and plausible timeline")
    void testPotentialMatchingHeuristicsHighMatch() {
        // Lost item: lost 2 days ago in Library
        LostItem lost = new LostItem("Dell Laptop Charger 65W", "Electronics", "Black USB-C barrel charger",
                "Central Library", LocalDate.now().minusDays(2), "Prashant Kumar", "prashant@campus.edu",
                "Initials PK scratched on brick");
        LostItem savedLost = lostItemService.reportLostItem(lost);

        // Found item: found yesterday in Library (plausible: found after lost)
        FoundItem found = new FoundItem("Dell Charger", "Electronics", "65W laptop charger left on study desk",
                "Central Library Reading Room", LocalDate.now().minusDays(1), "Librarian",
                "Initials PK on brick");
        foundItemDao.save(found);

        List<MatchSuggestion> matches = lostItemService.findPotentialMatchesForLost(savedLost.getId());

        assertFalse(matches.isEmpty(), "Should suggest at least one potential match");
        MatchSuggestion topMatch = matches.get(0);
        assertEquals(found.getId(), topMatch.getFoundItem().getId());
        assertTrue(topMatch.getMatchScore() >= 50, "Expected match score >= 50% for high category/keyword/location match");
        assertFalse(topMatch.getMatchReasons().isEmpty());
    }

    @Test
    @DisplayName("Matching heuristic scores 0 or drops items with totally different categories and no keyword overlap")
    void testPotentialMatchingHeuristicsNoMatch() {
        LostItem lost = new LostItem("Gold Ring", "Jewelry", "Small gold band",
                "Gym", LocalDate.now(), "Divya", "divya@campus.edu", "Engraving");
        LostItem savedLost = lostItemService.reportLostItem(lost);

        FoundItem found = new FoundItem("Water Bottle", "Others", "Stainless steel flask",
                "Auditorium", LocalDate.now(), "Security", "Dent on cap");
        foundItemDao.save(found);

        List<MatchSuggestion> matches = lostItemService.findPotentialMatchesForLost(savedLost.getId());
        assertTrue(matches.isEmpty(), "Should not suggest completely unrelated items as matches");
    }

    @Test
    @DisplayName("Matching heuristic penalizes items where date found is BEFORE date lost")
    void testMatchingHeuristicTimelinePlausibility() {
        // Lost item: lost today
        LostItem lost = new LostItem("Black Umbrella", "Accessories", "Compact folding umbrella",
                "Canteen", LocalDate.now(), "Sunil", "sunil@campus.edu", "Broken rib");
        LostItem savedLost = lostItemService.reportLostItem(lost);

        // Found item: found 5 days ago (before the item was even lost!)
        FoundItem found = new FoundItem("Black Umbrella", "Accessories", "Compact umbrella",
                "Canteen", LocalDate.now().minusDays(5), "Staff", "Broken rib");
        foundItemDao.save(found);

        List<MatchSuggestion> matches = lostItemService.findPotentialMatchesForLost(savedLost.getId());
        if (!matches.isEmpty()) {
            MatchSuggestion match = matches.get(0);
            assertFalse(match.getMatchReasons().stream().anyMatch(r -> r.contains("Plausible timeline")),
                    "Should not award timeline points if found date precedes lost date");
        }
    }

    @Test
    @DisplayName("findPotentialMatchesForLost throws LostItemNotFoundException for invalid ID")
    void testPotentialMatchesNonExistentLostIdThrows() {
        assertThrows(LostItemNotFoundException.class, () -> lostItemService.findPotentialMatchesForLost(8888));
    }
}
