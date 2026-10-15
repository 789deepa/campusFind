package com.campusfind.ui;

import com.campusfind.exception.CampusFindException;
import com.campusfind.model.*;
import com.campusfind.service.ClaimService;
import com.campusfind.service.ItemService;
import com.campusfind.util.DateUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Handles the interactive console menu and user input for CampusFind.
 * 
 * <p>Demonstrates:
 * <ul>
 *   <li>Defensive input handling (preventing Scanner buffer pitfalls)</li>
 *   <li>Structured exception handling keeping the console user-friendly</li>
 *   <li>Clear separation of UI concerns from business logic</li>
 * </ul>
 * </p>
 */
public class ConsoleMenu {

    private final ItemService itemService;
    private final ClaimService claimService;
    private final Scanner scanner;

    public ConsoleMenu(ItemService itemService, ClaimService claimService, Scanner scanner) {
        this.itemService = itemService;
        this.claimService = claimService;
        this.scanner = scanner;
    }

    public void run() {
        ConsolePrinter.printBanner();
        boolean running = true;

        while (running) {
            try {
                ConsolePrinter.printMenu();
                String input = scanner.nextLine().trim();

                switch (input) {
                    case "1" -> handleReportItem();
                    case "2" -> handleListAvailableItems();
                    case "3" -> handleSearchItems();
                    case "4" -> handleFilterByCategory();
                    case "5" -> handleFilterByLocation();
                    case "6" -> handleSubmitClaim();
                    case "7" -> handleReviewPendingClaims();
                    case "8" -> handleApproveOrRejectClaim();
                    case "9" -> handleMarkItemReturned();
                    case "10" -> handleDisplayDashboard();
                    case "11" -> handleViewAuditHistory();
                    case "12" -> {
                        System.out.println("\nThank you for using CampusFind! Have a great day.");
                        running = false;
                    }
                    default -> ConsolePrinter.printWarning("Invalid selection. Please choose an option between 1 and 12.");
                }
            } catch (CampusFindException e) {
                ConsolePrinter.printError(e.getMessage());
            } catch (Exception e) {
                ConsolePrinter.printError("An unexpected error occurred: " + e.getMessage());
            }
        }
    }

    private void handleReportItem() {
        System.out.println("\n--- [1] Report a Found Item ---");

        System.out.print("Enter Item Name (e.g. Casio fx-991EX Calculator): ");
        String name = scanner.nextLine().trim();

        System.out.println("Available Categories: Electronics, Documents, Accessories, Clothing, Books, Keys, Others");
        System.out.print("Enter Category: ");
        String category = scanner.nextLine().trim();

        System.out.print("Enter Public Description (visible to campus): ");
        String desc = scanner.nextLine().trim();

        System.out.print("Enter Location Found (e.g. Library 2nd Floor): ");
        String loc = scanner.nextLine().trim();

        System.out.print("Enter Date Found (YYYY-MM-DD, press Enter for Today): ");
        String dateStr = scanner.nextLine().trim();
        LocalDate dateFound = dateStr.isEmpty() ? LocalDate.now() : DateUtil.parseDate(dateStr);

        System.out.print("Enter Your Name & Role (e.g. Rahul Sharma, Hostel Warden): ");
        String reportedBy = scanner.nextLine().trim();

        System.out.println("\n[!] CRITICAL PRIVACY DETAIL:");
        System.out.println("    Enter a secret identifying feature known ONLY to the true owner");
        System.out.println("    (e.g., 'Serial number ends in 890', 'Sticker of blue dragon inside cover').");
        System.out.print("Enter Private Verification Clue: ");
        String clue = scanner.nextLine().trim();

        FoundItem item = new FoundItem(name, category, desc, loc, dateFound, reportedBy, clue);
        FoundItem saved = itemService.reportItem(item);

        ConsolePrinter.printSuccess("Found item registered successfully! Assigned ID: #" + saved.getId());
    }

    private void handleListAvailableItems() {
        List<FoundItem> items = itemService.getAvailableItems();
        ConsolePrinter.printItemList(items, "Available & Claim-Pending Campus Items");
    }

    private void handleSearchItems() {
        System.out.println("\n--- [3] Search Items by Name or Keyword ---");
        System.out.print("Enter search keyword: ");
        String query = scanner.nextLine().trim();

        List<FoundItem> items = itemService.searchItems(query);
        ConsolePrinter.printItemList(items, "Search Results for '" + query + "'");
    }

    private void handleFilterByCategory() {
        System.out.println("\n--- [4] Filter Items by Category ---");
        System.out.println("Common categories: Electronics, Documents, Accessories, Clothing, Books, Keys, Others");
        System.out.print("Enter category to filter: ");
        String cat = scanner.nextLine().trim();

        List<FoundItem> items = itemService.filterByCategory(cat);
        ConsolePrinter.printItemList(items, "Category Filter: '" + cat + "'");
    }

    private void handleFilterByLocation() {
        System.out.println("\n--- [5] Filter Items by Location ---");
        System.out.print("Enter location keyword (e.g., Library, Cafeteria): ");
        String loc = scanner.nextLine().trim();

        List<FoundItem> items = itemService.filterByLocation(loc);
        ConsolePrinter.printItemList(items, "Location Filter: '" + loc + "'");
    }

    private void handleSubmitClaim() {
        System.out.println("\n--- [6] Submit a Claim for a Found Item ---");
        System.out.print("Enter the ID of the item you wish to claim: ");
        Integer itemId = parseIntegerInput();
        if (itemId == null) return;

        Optional<FoundItem> itemOpt = itemService.getItemById(itemId);
        if (itemOpt.isEmpty()) {
            ConsolePrinter.printError("Item #" + itemId + " does not exist.");
            return;
        }

        FoundItem item = itemOpt.get();
        if (item.getStatus() == ItemStatus.RETURNED) {
            ConsolePrinter.printError("Item #" + itemId + " has already been returned to its owner.");
            return;
        }

        System.out.println("Claiming Item: " + item.getItemName() + " (Found at: " + item.getLocationFound() + ")");

        System.out.print("Enter Your Full Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter Your Contact Info (Email or Phone Number): ");
        String contact = scanner.nextLine().trim();

        System.out.print("Enter General Claim Details (When and how did you lose it?): ");
        String desc = scanner.nextLine().trim();

        System.out.println("\n[!] PRIVATE VERIFICATION STEP:");
        System.out.println("    Provide private details/marks of the item to prove it is yours.");
        System.out.println("    (This will remain private and is only seen by the reviewer to verify ownership).");
        System.out.print("Enter Your Private Verification Answer: ");
        String answer = scanner.nextLine().trim();

        Claim claim = new Claim(itemId, name, contact, desc, answer);
        Claim saved = claimService.submitClaim(claim);

        ConsolePrinter.printSuccess("Claim #" + saved.getId() + " submitted successfully! Status: PENDING review.");
    }

    private void handleReviewPendingClaims() {
        List<Claim> pendingClaims = claimService.getPendingClaims();
        if (pendingClaims.isEmpty()) {
            ConsolePrinter.printSuccess("All caught up! There are currently no pending claims.");
            return;
        }

        ConsolePrinter.printClaimList(pendingClaims, "Pending Claims Awaiting Review");
        System.out.print("Would you like to inspect a claim in detail? Enter Claim ID (or press Enter to skip): ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) return;

        try {
            int claimId = Integer.parseInt(input);
            Optional<Claim> claimOpt = claimService.getClaimById(claimId);
            if (claimOpt.isEmpty() || claimOpt.get().getStatus() != ClaimStatus.PENDING) {
                ConsolePrinter.printError("No pending claim found with ID #" + claimId);
                return;
            }

            Claim claim = claimOpt.get();
            Optional<FoundItem> itemOpt = itemService.getItemById(claim.getItemId());
            if (itemOpt.isPresent()) {
                ConsolePrinter.printVerificationReviewCard(itemOpt.get(), claim);
            }
        } catch (NumberFormatException e) {
            ConsolePrinter.printWarning("Invalid claim ID format.");
        }
    }

    private void handleApproveOrRejectClaim() {
        System.out.println("\n--- [8] Approve or Reject a Claim ---");
        System.out.print("Enter Claim ID to review: ");
        Integer claimId = parseIntegerInput();
        if (claimId == null) return;

        Optional<Claim> claimOpt = claimService.getClaimById(claimId);
        if (claimOpt.isEmpty()) {
            ConsolePrinter.printError("Claim #" + claimId + " not found.");
            return;
        }

        Claim claim = claimOpt.get();
        if (claim.getStatus() != ClaimStatus.PENDING) {
            ConsolePrinter.printWarning("Claim #" + claimId + " has already been reviewed (Current status: " + claim.getStatus() + ").");
            return;
        }

        Optional<FoundItem> itemOpt = itemService.getItemById(claim.getItemId());
        if (itemOpt.isEmpty()) {
            ConsolePrinter.printError("Referenced item #" + claim.getItemId() + " was not found.");
            return;
        }

        FoundItem item = itemOpt.get();
        // Present verification review comparison
        ConsolePrinter.printVerificationReviewCard(item, claim);

        System.out.println("\nSelect Action:");
        System.out.println("  [A] Approve Claim (Owner verified)");
        System.out.println("  [R] Reject Claim (Failed verification / invalid)");
        System.out.println("  [C] Cancel / Back to Main Menu");
        System.out.print("Action (A/R/C): ");
        String action = scanner.nextLine().trim().toUpperCase();

        switch (action) {
            case "A" -> {
                boolean approved = claimService.approveClaim(claimId);
                if (approved) {
                    ConsolePrinter.printSuccess("Claim #" + claimId + " APPROVED! Transaction committed.");
                    System.out.println("Reminder: The claimant may now collect their item. Use menu option [9] to mark as RETURNED once handed over.");
                }
            }
            case "R" -> {
                boolean rejected = claimService.rejectClaim(claimId);
                if (rejected) {
                    ConsolePrinter.printSuccess("Claim #" + claimId + " has been REJECTED.");
                }
            }
            case "C" -> System.out.println("Review cancelled.");
            default -> ConsolePrinter.printWarning("Unrecognized action. No changes made.");
        }
    }

    private void handleMarkItemReturned() {
        System.out.println("\n--- [9] Mark Item as Returned (Handover to Owner) ---");
        System.out.print("Enter Item ID to mark as returned: ");
        Integer itemId = parseIntegerInput();
        if (itemId == null) return;

        boolean success = itemService.markItemAsReturned(itemId);
        if (success) {
            ConsolePrinter.printSuccess("Item #" + itemId + " marked as RETURNED! Case closed.");
        }
    }

    private void handleDisplayDashboard() {
        DashboardStatistics stats = itemService.getDashboardStatistics();
        ConsolePrinter.printDashboard(stats);
    }

    private void handleViewAuditHistory() {
        System.out.println("\n--- [11] View Item & Claim Audit History ---");
        List<FoundItem> items = itemService.getAllItemsHistory();
        ConsolePrinter.printItemList(items, "Complete Found Items History");

        List<Claim> claims = claimService.getAllClaimsHistory();
        ConsolePrinter.printClaimList(claims, "Complete Claims History");
    }

    private Integer parseIntegerInput() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            ConsolePrinter.printError("Invalid numerical ID. Please enter numbers only.");
            return null;
        }
    }
}
