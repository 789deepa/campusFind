package com.campusfind.ui;

import com.campusfind.exception.CampusFindException;
import com.campusfind.model.*;
import com.campusfind.service.ClaimService;
import com.campusfind.service.ItemService;
import com.campusfind.service.LostItemService;
import com.campusfind.util.DateUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Handles the interactive console menu and user input for CampusFind,
 * supporting both lost-item and found-item workflows, search, matching, and claim resolution.
 */
public class ConsoleMenu {

    private final ItemService itemService;
    private final ClaimService claimService;
    private final LostItemService lostItemService;
    private final Scanner scanner;

    public ConsoleMenu(ItemService itemService, ClaimService claimService, Scanner scanner) {
        this(itemService, claimService, null, scanner);
    }

    public ConsoleMenu(ItemService itemService, ClaimService claimService, LostItemService lostItemService, Scanner scanner) {
        this.itemService = itemService;
        this.claimService = claimService;
        this.lostItemService = lostItemService;
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
                    case "1" -> handleReportFoundItem();
                    case "2" -> handleListAvailableFoundItems();
                    case "3" -> handleSearchFoundItems();
                    case "4" -> handleReportLostItem();
                    case "5" -> handleListActiveLostItems();
                    case "6" -> handleSearchLostItems();
                    case "7" -> handleFindPotentialMatches();
                    case "8" -> handleSubmitClaim();
                    case "9" -> handleReviewPendingClaims();
                    case "10" -> handleApproveOrRejectClaim();
                    case "11" -> handleMarkItemReturned();
                    case "12" -> handleDisplayDashboard();
                    case "13" -> handleViewAuditHistory();
                    case "14" -> {
                        System.out.println("\nThank you for using CampusFind! Have a great day.");
                        running = false;
                    }
                    default -> ConsolePrinter.printWarning("Invalid selection. Please choose an option between 1 and 14.");
                }
            } catch (CampusFindException e) {
                ConsolePrinter.printError(e.getMessage());
            } catch (Exception e) {
                ConsolePrinter.printError("An unexpected error occurred: " + e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------------
    // 1. Found Item Handlers
    // ------------------------------------------------------------------------

    private void handleReportFoundItem() {
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

        System.out.print("Enter Your Name & Role (e.g. Rahul Sharma, Library Attendant): ");
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

    private void handleListAvailableFoundItems() {
        List<FoundItem> items = itemService.getAvailableItems();
        ConsolePrinter.printItemList(items, "Available & Claim-Pending Found Items");
    }

    private void handleSearchFoundItems() {
        System.out.println("\n--- [3] Search & Filter Found Items ---");
        System.out.println("  [1] Search by Name or Keyword");
        System.out.println("  [2] Filter by Category");
        System.out.println("  [3] Filter by Location");
        System.out.print("Select search filter (1-3): ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> {
                System.out.print("Enter keyword: ");
                String kw = scanner.nextLine().trim();
                ConsolePrinter.printItemList(itemService.searchItems(kw), "Search Results for '" + kw + "'");
            }
            case "2" -> {
                System.out.print("Enter category (e.g. Electronics, Documents): ");
                String cat = scanner.nextLine().trim();
                ConsolePrinter.printItemList(itemService.filterByCategory(cat), "Category Filter: '" + cat + "'");
            }
            case "3" -> {
                System.out.print("Enter location (e.g. Library, Cafeteria): ");
                String loc = scanner.nextLine().trim();
                ConsolePrinter.printItemList(itemService.filterByLocation(loc), "Location Filter: '" + loc + "'");
            }
            default -> ConsolePrinter.printWarning("Invalid filter selection.");
        }
    }

    // ------------------------------------------------------------------------
    // 2. Lost Item Handlers
    // ------------------------------------------------------------------------

    private void handleReportLostItem() {
        System.out.println("\n--- [4] Report a Lost Item (Student Report) ---");

        System.out.print("Enter Item Name (e.g. Dell 130W USB-C Charger): ");
        String name = scanner.nextLine().trim();

        System.out.println("Available Categories: Electronics, Documents, Accessories, Clothing, Books, Keys, Others");
        System.out.print("Enter Category: ");
        String category = scanner.nextLine().trim();

        System.out.print("Enter Description (General appearance, color, brand): ");
        String desc = scanner.nextLine().trim();

        System.out.print("Enter Location Last Seen (e.g. Central Library - 2nd Floor): ");
        String loc = scanner.nextLine().trim();

        System.out.print("Enter Date Lost (YYYY-MM-DD, press Enter for Today): ");
        String dateStr = scanner.nextLine().trim();
        LocalDate dateLost = dateStr.isEmpty() ? LocalDate.now() : DateUtil.parseDate(dateStr);

        System.out.print("Enter Your Full Name: ");
        String studentName = scanner.nextLine().trim();

        System.out.print("Enter Your Contact Details (Email / Phone): ");
        String contact = scanner.nextLine().trim();

        System.out.println("\n[!] PRIVATE IDENTIFYING DETAILS (Confidential):");
        System.out.println("    Enter secret identifying marks known only to you (hidden scratch, serial number, sticker).");
        System.out.println("    This remains confidential and will be used by staff to verify found items.");
        System.out.print("Enter Secret Identifying Details: ");
        String details = scanner.nextLine().trim();

        LostItem lostItem = new LostItem(name, category, desc, loc, dateLost, studentName, contact, details);
        LostItem saved = (lostItemService != null) ? lostItemService.reportLostItem(lostItem) : lostItem;

        ConsolePrinter.printSuccess("Lost-item report filed successfully! Assigned Lost Report ID: #" + saved.getId());
        System.out.println("Tip: Use menu option [7] to check if anyone has already found an item matching your report!");
    }

    private void handleListActiveLostItems() {
        if (lostItemService == null) {
            ConsolePrinter.printWarning("Lost-item service not initialized.");
            return;
        }
        List<LostItem> items = lostItemService.getOpenLostItems();
        ConsolePrinter.printLostItemList(items, "Active Student Lost-Item Reports");
    }

    private void handleSearchLostItems() {
        if (lostItemService == null) {
            ConsolePrinter.printWarning("Lost-item service not initialized.");
            return;
        }
        System.out.println("\n--- [6] Search & Filter Lost Items ---");
        System.out.println("  [1] Search by Keyword");
        System.out.println("  [2] Filter by Category");
        System.out.println("  [3] Filter by Location");
        System.out.print("Select search filter (1-3): ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> {
                System.out.print("Enter keyword: ");
                String kw = scanner.nextLine().trim();
                ConsolePrinter.printLostItemList(lostItemService.searchLostItems(kw), "Lost Search Results for '" + kw + "'");
            }
            case "2" -> {
                System.out.print("Enter category: ");
                String cat = scanner.nextLine().trim();
                ConsolePrinter.printLostItemList(lostItemService.filterByCategory(cat), "Lost Category Filter: '" + cat + "'");
            }
            case "3" -> {
                System.out.print("Enter location: ");
                String loc = scanner.nextLine().trim();
                ConsolePrinter.printLostItemList(lostItemService.filterByLocation(loc), "Lost Location Filter: '" + loc + "'");
            }
            default -> ConsolePrinter.printWarning("Invalid filter selection.");
        }
    }

    // ------------------------------------------------------------------------
    // 3. Matching & Claim Handlers
    // ------------------------------------------------------------------------

    private void handleFindPotentialMatches() {
        if (lostItemService == null) {
            ConsolePrinter.printWarning("Matching service not available.");
            return;
        }
        System.out.println("\n--- [7] Suggest Potential Matches Between Lost & Found Reports ---");
        System.out.println("  [1] Find matches for a specific Lost-Item Report ID");
        System.out.println("  [2] Scan all active Lost and Found items across campus");
        System.out.print("Choice (1-2): ");
        String choice = scanner.nextLine().trim();

        if ("1".equals(choice)) {
            System.out.print("Enter Lost Report ID: ");
            Integer lostId = parseIntegerInput();
            if (lostId == null) return;
            List<MatchSuggestion> matches = lostItemService.findPotentialMatchesForLost(lostId);
            ConsolePrinter.printMatchSuggestions(matches, "Potential Matches for Lost Report #" + lostId);
        } else {
            List<MatchSuggestion> matches = lostItemService.findAllPotentialMatches();
            ConsolePrinter.printMatchSuggestions(matches, "Campus-Wide Potential Match Suggestions");
        }
    }

    private void handleSubmitClaim() {
        System.out.println("\n--- [8] Submit a Claim for a Found Item ---");
        System.out.print("Enter the ID of the found item you wish to claim: ");
        Integer itemId = parseIntegerInput();
        if (itemId == null) return;

        Optional<FoundItem> itemOpt = itemService.getItemById(itemId);
        if (itemOpt.isEmpty()) {
            ConsolePrinter.printError("Found Item #" + itemId + " does not exist.");
            return;
        }

        FoundItem item = itemOpt.get();
        if (item.getStatus() == ItemStatus.RETURNED) {
            ConsolePrinter.printError("Found Item #" + itemId + " has already been returned to its owner.");
            return;
        }

        System.out.println("Claiming Item: " + item.getItemName() + " (Found at: " + item.getLocationFound() + ")");

        // Optional linking to existing lost report
        Integer lostReportId = null;
        System.out.print("Do you have an existing Lost-Item Report ID? (Enter ID or press Enter to skip): ");
        String lostInput = scanner.nextLine().trim();
        if (!lostInput.isEmpty()) {
            try {
                lostReportId = Integer.parseInt(lostInput);
                if (lostItemService != null) {
                    Optional<LostItem> lostOpt = lostItemService.getLostItemById(lostReportId);
                    if (lostOpt.isEmpty()) {
                        ConsolePrinter.printError("Lost-item report #" + lostReportId + " was not found.");
                        return;
                    }
                    LostItem lostItem = lostOpt.get();
                    if (lostItem.getStatus() == LostItemStatus.RESOLVED) {
                        ConsolePrinter.printError("Lost-item report #" + lostReportId + " is already marked as RESOLVED.");
                        return;
                    }
                    System.out.println("[+] Linked with Lost Report #" + lostItem.getId() + " (" + lostItem.getItemName() + ")");
                }
            } catch (NumberFormatException e) {
                ConsolePrinter.printWarning("Invalid lost report ID format. Proceeding without link.");
            }
        }

        System.out.print("Enter Your Full Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter Your Contact Info (Email or Phone): ");
        String contact = scanner.nextLine().trim();

        System.out.print("Enter General Claim Details (When and where did you lose it?): ");
        String desc = scanner.nextLine().trim();

        System.out.println("\n[!] PRIVATE VERIFICATION STEP:");
        System.out.println("    Provide private details/marks of the item to prove it is yours.");
        System.out.println("    (This will remain private and is only seen by the reviewer to verify ownership).");
        System.out.print("Enter Your Private Verification Answer: ");
        String answer = scanner.nextLine().trim();

        Claim claim = new Claim(itemId, lostReportId, name, contact, desc, answer);
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
            LostItem linkedLost = null;
            if (claim.getLostReportId() != null && lostItemService != null) {
                linkedLost = lostItemService.getLostItemById(claim.getLostReportId()).orElse(null);
            }

            if (itemOpt.isPresent()) {
                ConsolePrinter.printVerificationReviewCard(itemOpt.get(), claim, linkedLost);
            }
        } catch (NumberFormatException e) {
            ConsolePrinter.printWarning("Invalid claim ID format.");
        }
    }

    private void handleApproveOrRejectClaim() {
        System.out.println("\n--- [10] Approve or Reject a Claim ---");
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

        LostItem linkedLost = null;
        if (claim.getLostReportId() != null && lostItemService != null) {
            linkedLost = lostItemService.getLostItemById(claim.getLostReportId()).orElse(null);
        }

        // Present verification review comparison
        ConsolePrinter.printVerificationReviewCard(itemOpt.get(), claim, linkedLost);

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
                    if (claim.getLostReportId() != null) {
                        System.out.println("[i] Associated Lost-Item Report #" + claim.getLostReportId() + " updated to MATCHED.");
                    }
                    System.out.println("Reminder: The claimant may now collect their item. Use menu option [11] to mark as RETURNED once handed over.");
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
        System.out.println("\n--- [11] Mark Item as Returned (Handover to Owner) ---");
        System.out.print("Enter Item ID to mark as returned: ");
        Integer itemId = parseIntegerInput();
        if (itemId == null) return;

        boolean success = itemService.markItemAsReturned(itemId);
        if (success) {
            ConsolePrinter.printSuccess("Item #" + itemId + " marked as RETURNED! Case closed and any linked lost-item report resolved.");
        }
    }

    private void handleDisplayDashboard() {
        DashboardStatistics stats = itemService.getDashboardStatistics();
        ConsolePrinter.printDashboard(stats);
    }

    private void handleViewAuditHistory() {
        System.out.println("\n--- [13] View Complete Audit History ---");
        List<FoundItem> items = itemService.getAllItemsHistory();
        ConsolePrinter.printItemList(items, "Complete Found Items History");

        if (lostItemService != null) {
            List<LostItem> lostItems = lostItemService.getAllLostItems();
            ConsolePrinter.printLostItemList(lostItems, "Complete Lost Items History");
        }

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
