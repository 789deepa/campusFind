package com.campusfind.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data Transfer Object (DTO) representing a suggested match between
 * a student's lost-item report and an available found-item record.
 */
public class MatchSuggestion {
    private final LostItem lostItem;
    private final FoundItem foundItem;
    private final int matchScore;
    private final List<String> matchReasons;

    public MatchSuggestion(LostItem lostItem, FoundItem foundItem, int matchScore, List<String> matchReasons) {
        this.lostItem = lostItem;
        this.foundItem = foundItem;
        this.matchScore = matchScore;
        this.matchReasons = matchReasons != null ? new ArrayList<>(matchReasons) : Collections.emptyList();
    }

    public LostItem getLostItem() {
        return lostItem;
    }

    public FoundItem getFoundItem() {
        return foundItem;
    }

    public int getMatchScore() {
        return matchScore;
    }

    public List<String> getMatchReasons() {
        return Collections.unmodifiableList(matchReasons);
    }

    @Override
    public String toString() {
        return "MatchSuggestion{" +
                "lostItemId=" + (lostItem != null ? lostItem.getId() : "null") +
                ", foundItemId=" + (foundItem != null ? foundItem.getId() : "null") +
                ", score=" + matchScore + "%" +
                ", reasons=" + matchReasons +
                '}';
    }
}
