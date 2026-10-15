package com.campusfind.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing an item found on campus.
 * 
 * <p><b>Privacy-First Design Principle:</b>
 * The {@code privateVerificationClue} field holds identifying marks, serial numbers,
 * or unique characteristics known only to the finder and the true owner.
 * To prevent accidental leaks in logs or UI prints, {@link #toString()} always masks this field.
 * Only authorized reviewers access the clue via {@link #getPrivateVerificationClue()}.
 * </p>
 */
public class FoundItem {
    private Integer id;
    private String itemName;
    private String category;
    private String description;
    private String locationFound;
    private LocalDate dateFound;
    private LocalDateTime dateReported;
    private String reportedBy;
    private String privateVerificationClue;
    private ItemStatus status;

    /**
     * Default no-arg constructor required for standard JavaBean conventions.
     */
    public FoundItem() {
        this.status = ItemStatus.AVAILABLE;
        this.dateReported = LocalDateTime.now();
    }

    /**
     * Constructor for creating a newly reported item (before persistence / auto-generated ID).
     */
    public FoundItem(String itemName, String category, String description,
                     String locationFound, LocalDate dateFound, String reportedBy,
                     String privateVerificationClue) {
        this.itemName = itemName;
        this.category = category;
        this.description = description;
        this.locationFound = locationFound;
        this.dateFound = dateFound;
        this.reportedBy = reportedBy;
        this.privateVerificationClue = privateVerificationClue;
        this.status = ItemStatus.AVAILABLE;
        this.dateReported = LocalDateTime.now();
    }

    /**
     * Full constructor for hydrating entities from the database.
     */
    public FoundItem(Integer id, String itemName, String category, String description,
                     String locationFound, LocalDate dateFound, LocalDateTime dateReported,
                     String reportedBy, String privateVerificationClue, ItemStatus status) {
        this.id = id;
        this.itemName = itemName;
        this.category = category;
        this.description = description;
        this.locationFound = locationFound;
        this.dateFound = dateFound;
        this.dateReported = dateReported;
        this.reportedBy = reportedBy;
        this.privateVerificationClue = privateVerificationClue;
        this.status = status != null ? status : ItemStatus.AVAILABLE;
    }

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocationFound() {
        return locationFound;
    }

    public void setLocationFound(String locationFound) {
        this.locationFound = locationFound;
    }

    public LocalDate getDateFound() {
        return dateFound;
    }

    public void setDateFound(LocalDate dateFound) {
        this.dateFound = dateFound;
    }

    public LocalDateTime getDateReported() {
        return dateReported;
    }

    public void setDateReported(LocalDateTime dateReported) {
        this.dateReported = dateReported;
    }

    public String getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(String reportedBy) {
        this.reportedBy = reportedBy;
    }

    /**
     * Retrieves the confidential clue. Accessible strictly during the claim verification review.
     */
    public String getPrivateVerificationClue() {
        return privateVerificationClue;
    }

    public void setPrivateVerificationClue(String privateVerificationClue) {
        this.privateVerificationClue = privateVerificationClue;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FoundItem foundItem)) return false;
        return Objects.equals(id, foundItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Safe string representation ensuring confidential clues are REDACTED.
     * Prevents security leaks when objects are logged or printed.
     */
    @Override
    public String toString() {
        return "FoundItem{" +
                "id=" + id +
                ", itemName='" + itemName + '\'' +
                ", category='" + category + '\'' +
                ", description='" + description + '\'' +
                ", locationFound='" + locationFound + '\'' +
                ", dateFound=" + dateFound +
                ", dateReported=" + dateReported +
                ", reportedBy='" + reportedBy + '\'' +
                ", privateVerificationClue='[REDACTED]'" +
                ", status=" + status +
                '}';
    }
}
