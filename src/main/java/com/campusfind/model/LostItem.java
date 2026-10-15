package com.campusfind.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing a lost-item report filed by a student.
 * 
 * <p><b>Privacy-First Design Principle:</b>
 * The {@code identifyingDetails} field holds private identifying marks
 * (e.g., sticker details, unique scratches, or custom wallpapers).
 * To prevent privacy leaks and fake claims, {@link #toString()} always masks this field with {@code [REDACTED]}.
 * Authorized staff compare this clue during claim review via {@link #getIdentifyingDetails()}.
 * </p>
 */
public class LostItem {
    private Integer id;
    private String itemName;
    private String category;
    private String description;
    private String locationLost;
    private LocalDate dateLost;
    private LocalDateTime dateReported;
    private String studentName;
    private String studentContact;
    private String identifyingDetails;
    private LostItemStatus status;

    public LostItem() {
        this.status = LostItemStatus.OPEN;
        this.dateReported = LocalDateTime.now();
    }

    /**
     * Constructor for reporting a new lost item before persistence.
     */
    public LostItem(String itemName, String category, String description,
                    String locationLost, LocalDate dateLost, String studentName,
                    String studentContact, String identifyingDetails) {
        this.itemName = itemName;
        this.category = category;
        this.description = description;
        this.locationLost = locationLost;
        this.dateLost = dateLost;
        this.studentName = studentName;
        this.studentContact = studentContact;
        this.identifyingDetails = identifyingDetails;
        this.status = LostItemStatus.OPEN;
        this.dateReported = LocalDateTime.now();
    }

    /**
     * Full constructor for hydrating entities from the database.
     */
    public LostItem(Integer id, String itemName, String category, String description,
                    String locationLost, LocalDate dateLost, LocalDateTime dateReported,
                    String studentName, String studentContact, String identifyingDetails,
                    LostItemStatus status) {
        this.id = id;
        this.itemName = itemName;
        this.category = category;
        this.description = description;
        this.locationLost = locationLost;
        this.dateLost = dateLost;
        this.dateReported = dateReported;
        this.studentName = studentName;
        this.studentContact = studentContact;
        this.identifyingDetails = identifyingDetails;
        this.status = status != null ? status : LostItemStatus.OPEN;
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

    public String getLocationLost() {
        return locationLost;
    }

    public void setLocationLost(String locationLost) {
        this.locationLost = locationLost;
    }

    public LocalDate getDateLost() {
        return dateLost;
    }

    public void setDateLost(LocalDate dateLost) {
        this.dateLost = dateLost;
    }

    public LocalDateTime getDateReported() {
        return dateReported;
    }

    public void setDateReported(LocalDateTime dateReported) {
        this.dateReported = dateReported;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentContact() {
        return studentContact;
    }

    public void setStudentContact(String studentContact) {
        this.studentContact = studentContact;
    }

    /**
     * Retrieves the confidential identifying details.
     * To be accessed strictly during reviewer claim verification or match evaluation.
     */
    public String getIdentifyingDetails() {
        return identifyingDetails;
    }

    public void setIdentifyingDetails(String identifyingDetails) {
        this.identifyingDetails = identifyingDetails;
    }

    public LostItemStatus getStatus() {
        return status;
    }

    public void setStatus(LostItemStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LostItem lostItem)) return false;
        return Objects.equals(id, lostItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Safe string representation masking sensitive identifying details.
     */
    @Override
    public String toString() {
        return "LostItem{" +
                "id=" + id +
                ", itemName='" + itemName + '\'' +
                ", category='" + category + '\'' +
                ", description='" + description + '\'' +
                ", locationLost='" + locationLost + '\'' +
                ", dateLost=" + dateLost +
                ", dateReported=" + dateReported +
                ", studentName='" + studentName + '\'' +
                ", studentContact='" + studentContact + '\'' +
                ", identifyingDetails='[REDACTED]'" +
                ", status=" + status +
                '}';
    }
}
