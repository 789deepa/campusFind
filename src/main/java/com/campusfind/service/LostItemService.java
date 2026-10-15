package com.campusfind.service;

import com.campusfind.dao.FoundItemDao;
import com.campusfind.dao.LostItemDao;
import com.campusfind.exception.LostItemNotFoundException;
import com.campusfind.model.FoundItem;
import com.campusfind.model.LostItem;
import com.campusfind.model.LostItemStatus;
import com.campusfind.model.MatchSuggestion;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Service encapsulating business logic for lost-item reports and potential match heuristics.
 */
public class LostItemService {
    private static final Logger logger = Logger.getLogger(LostItemService.class.getName());

    private final LostItemDao lostItemDao;
    private final FoundItemDao foundItemDao;
    private final ValidationService validationService;

    public LostItemService(LostItemDao lostItemDao, FoundItemDao foundItemDao, ValidationService validationService) {
        this.lostItemDao = lostItemDao;
        this.foundItemDao = foundItemDao;
        this.validationService = validationService;
    }

    /**
     * Reports a lost item on campus.
     */
    public LostItem reportLostItem(LostItem item) {
        validationService.validateLostItem(item);
        item.setStatus(LostItemStatus.OPEN);
        LostItem saved = lostItemDao.save(item);
        logger.info("New lost-item report registered with ID: #" + saved.getId());
        return saved;
    }

    public List<LostItem> getOpenLostItems() {
        return lostItemDao.findAllOpen();
    }

    public Optional<LostItem> getLostItemById(Integer id) {
        if (id == null || id <= 0) return Optional.empty();
        return lostItemDao.findById(id);
    }

    public List<LostItem> searchLostItems(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getOpenLostItems();
        }
        return lostItemDao.searchByKeyword(keyword.trim());
    }

    public List<LostItem> filterByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return getOpenLostItems();
        }
        return lostItemDao.findByCategory(category.trim());
    }

    public List<LostItem> filterByLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            return getOpenLostItems();
        }
        return lostItemDao.findByLocation(location.trim());
    }

    public List<LostItem> getAllLostItems() {
        return lostItemDao.findAll();
    }

    public boolean updateStatus(Integer id, LostItemStatus newStatus) {
        return lostItemDao.updateStatus(id, newStatus);
    }

    /**
     * Finds potential matches for a specific lost item among available found items.
     * Heuristic evaluates category equality, keyword overlaps, location similarity, and date plausibility.
     */
    public List<MatchSuggestion> findPotentialMatchesForLost(Integer lostItemId) {
        LostItem lost = lostItemDao.findById(lostItemId)
                .orElseThrow(() -> new LostItemNotFoundException(lostItemId));

        List<FoundItem> availableFound = foundItemDao.findAllAvailable();
        List<MatchSuggestion> suggestions = new ArrayList<>();

        for (FoundItem found : availableFound) {
            MatchSuggestion suggestion = evaluateMatch(lost, found);
            if (suggestion.getMatchScore() >= 35) { // Match threshold
                suggestions.add(suggestion);
            }
        }

        suggestions.sort(Comparator.comparingInt(MatchSuggestion::getMatchScore).reversed());
        return suggestions;
    }

    /**
     * Scans all open lost items against all available found items and returns potential match pairs.
     */
    public List<MatchSuggestion> findAllPotentialMatches() {
        List<LostItem> openLost = lostItemDao.findAllOpen();
        List<FoundItem> availableFound = foundItemDao.findAllAvailable();
        List<MatchSuggestion> suggestions = new ArrayList<>();

        for (LostItem lost : openLost) {
            for (FoundItem found : availableFound) {
                MatchSuggestion suggestion = evaluateMatch(lost, found);
                if (suggestion.getMatchScore() >= 35) {
                    suggestions.add(suggestion);
                }
            }
        }

        suggestions.sort(Comparator.comparingInt(MatchSuggestion::getMatchScore).reversed());
        return suggestions;
    }

    /**
     * Evaluates matching criteria between a lost report and a found item.
     */
    public MatchSuggestion evaluateMatch(LostItem lost, FoundItem found) {
        int score = 0;
        List<String> reasons = new ArrayList<>();

        // 1. Category Matching (Weight: 35)
        if (lost.getCategory() != null && lost.getCategory().equalsIgnoreCase(found.getCategory())) {
            score += 35;
            reasons.add("Exact Category Match (" + lost.getCategory() + ")");
        }

        // 2. Name Keyword Overlap (Weight: Up to 35)
        Set<String> lostWords = extractKeywords(lost.getItemName());
        Set<String> foundWords = extractKeywords(found.getItemName() + " " + found.getDescription());

        long commonWords = lostWords.stream().filter(foundWords::contains).count();
        if (commonWords > 0) {
            int keywordPoints = (int) Math.min(35, commonWords * 15);
            score += keywordPoints;
            reasons.add("Keyword Overlap in Title/Description (" + commonWords + " common term(s))");
        }

        // 3. Location Proximity / Similarity (Weight: 20)
        if (isLocationSimilar(lost.getLocationLost(), found.getLocationFound())) {
            score += 20;
            reasons.add("Location Match / Proximity (" + found.getLocationFound() + ")");
        }

        // 4. Chronological Plausibility: Item found on or after the date it was lost (Weight: 10)
        if (lost.getDateLost() != null && found.getDateFound() != null) {
            if (!found.getDateFound().isBefore(lost.getDateLost())) {
                score += 10;
                reasons.add("Date Plausible: Found on/after date lost");
            }
        }

        score = Math.min(100, score);
        return new MatchSuggestion(lost, found, score, reasons);
    }

    private Set<String> extractKeywords(String text) {
        if (text == null) return Collections.emptySet();
        Set<String> stopWords = Set.of("the", "and", "with", "for", "from", "left", "near", "lost", "found", "this", "that");
        return Arrays.stream(text.toLowerCase().split("[^a-zA-Z0-9]+"))
                .filter(w -> w.length() >= 3 && !stopWords.contains(w))
                .collect(Collectors.toSet());
    }

    private boolean isLocationSimilar(String loc1, String loc2) {
        if (loc1 == null || loc2 == null) return false;
        String l1 = loc1.toLowerCase().trim();
        String l2 = loc2.toLowerCase().trim();
        return l1.contains(l2) || l2.contains(l1);
    }
}
