package com.campusfind.ui;

import com.campusfind.model.Claim;
import com.campusfind.model.DashboardStatistics;
import com.campusfind.model.FoundItem;
import com.campusfind.model.LostItem;
import com.campusfind.model.MatchSuggestion;
import com.campusfind.util.DateUtil;

import java.util.List;

/**
 * Utility for formatting and printing console outputs, tables, and alert boxes.
 * 
 * <p><b>Privacy-First Verification Rule:</b>
 * Public listings strictly omit confidential clues and answers.
 * Only the designated verification review screen displays them side-by-side.
 * </p>
 */
public final class ConsolePrinter {

    private ConsolePrinter() {}

    public static void printBanner() {
        System.out.println("==========================================================================================");
        System.out.println("   ____                                  _____ _           _ ");
        System.out.println("  / ___|__ _ _ __ ___  _ __  _   _ ___  |  ___(_)_ __   __| |");
        System.out.println(" | |   / _` | '_ ` _ \\| '_ \\| | | / __| | |_  | | '_ \\ / _` |");
        System.out.println(" | |__| (_| | | | | | | |_) | |_| \\__ \\ |  _| | | | | | (_| |");
        System.out.println("  \\____\\__,_|_| |_| |_| .__/ \\__,_|___/ |_|   |_|_| |_|\\__,_|");
        System.out.println("                      |_|                                    ");
        System.out.println("   Privacy-First Campus Lost & Found Management System");
        System.out.println("==========================================================================================");
    }

    public static void printMenu() {
        System.out.println("\n-------------------------------- MENU OPTIONS --------------------------------");
        System.out.println(" [FOUND ITEMS]");
        System.out.println("  1. Report a Found Item");
        System.out.println("  2. List Available / Open Found Items");
        System.out.println("  3. Search & Filter Found Items (Keyword / Category / Location)");
        System.out.println(" [LOST ITEMS]");
        System.out.println("  4. Report a Lost Item (Student Report)");
        System.out.println("  5. List Active Lost-Item Reports");
        System.out.println("  6. Search & Filter Lost Items (Keyword / Category / Location)");
        System.out.println(" [MATCHING & CLAIMS]");
        System.out.println("  7. Find Potential Matches (Suggest Matches between Lost & Found)");
        System.out.println("  8. Submit a Claim for an Item (with optional Lost Report ID)");
        System.out.println("  9. Review Pending Claims (Privacy-Verified Review)");
        System.out.println(" 10. Approve or Reject a Claim");
        System.out.println(" 11. Mark Item as Returned (Handover to Owner & Resolve Lost Report)");
        System.out.println(" [SYSTEM & AUDIT]");
        System.out.println(" 12. Display Live Statistics Dashboard");
        System.out.println(" 13. View All History & Audit Logs (Found, Lost & Claims)");
        System.out.println(" 14. Exit");
        System.out.println("------------------------------------------------------------------------------");
        System.out.print("Please select an option (1-14): ");
    }

    public static void printItemList(List<FoundItem> items, String title) {
        System.out.println("\n=== " + title + " (" + items.size() + " items found) ===");
        if (items.isEmpty()) {
            System.out.println("No items match the criteria.");
            return;
        }

        String format = "| %-4s | %-28s | %-13s | %-22s | %-10s | %-13s |%n";
        String separator = "+------+------------------------------+---------------+------------------------+------------+---------------+";

        System.out.println(separator);
        System.out.printf(format, "ID", "Item Name", "Category", "Location", "Date Found", "Status");
        System.out.println(separator);

        for (FoundItem item : items) {
            System.out.printf(format,
                    item.getId(),
                    truncate(item.getItemName(), 28),
                    truncate(item.getCategory(), 13),
                    truncate(item.getLocationFound(), 22),
                    DateUtil.formatDate(item.getDateFound()),
                    item.getStatus().getDisplayName());
        }
        System.out.println(separator);
        System.out.println("NOTE: Private verification clues are confidential and hidden from public view.");
    }

    public static void printLostItemList(List<LostItem> items, String title) {
        System.out.println("\n=== " + title + " (" + items.size() + " reports found) ===");
        if (items.isEmpty()) {
            System.out.println("No lost reports match the criteria.");
            return;
        }

        String format = "| %-4s | %-26s | %-13s | %-22s | %-10s | %-10s |%n";
        String separator = "+------+----------------------------+---------------+------------------------+------------+------------+";

        System.out.println(separator);
        System.out.printf(format, "LID", "Item Name", "Category", "Last Seen Location", "Date Lost", "Status");
        System.out.println(separator);

        for (LostItem item : items) {
            System.out.printf(format,
                    item.getId(),
                    truncate(item.getItemName(), 26),
                    truncate(item.getCategory(), 13),
                    truncate(item.getLocationLost(), 22),
                    DateUtil.formatDate(item.getDateLost()),
                    item.getStatus().getDisplayName());
        }
        System.out.println(separator);
        System.out.println("NOTE: Private identifying details are confidential and hidden from public view.");
    }

    public static void printMatchSuggestions(List<MatchSuggestion> matches, String title) {
        System.out.println("\n=== " + title + " (" + matches.size() + " suggestions) ===");
        if (matches.isEmpty()) {
            System.out.println("No potential matches found with current matching criteria.");
            return;
        }

        int count = 1;
        for (MatchSuggestion m : matches) {
            LostItem lost = m.getLostItem();
            FoundItem found = m.getFoundItem();
            System.out.println("\n[" + count++ + "] MATCH SCORE: " + m.getMatchScore() + "%");
            System.out.println("    LOST REPORT  #" + lost.getId() + ": '" + lost.getItemName() + "'");
            System.out.println("                 Category : " + lost.getCategory() + " | Lost at: " + lost.getLocationLost() + " on " + DateUtil.formatDate(lost.getDateLost()));
            System.out.println("                 Reported by Student: " + lost.getStudentName() + " (" + lost.getStudentContact() + ")");
            System.out.println("    FOUND RECORD #" + found.getId() + ": '" + found.getItemName() + "'");
            System.out.println("                 Category : " + found.getCategory() + " | Found at: " + found.getLocationFound() + " on " + DateUtil.formatDate(found.getDateFound()));
            System.out.println("                 Status: " + found.getStatus().getDisplayName());
            System.out.println("    Matching Criteria: " + String.join(" | ", m.getMatchReasons()));
            System.out.println("    ----------------------------------------------------------------------");
        }
    }

    public static void printClaimList(List<Claim> claims, String title) {
        System.out.println("\n=== " + title + " (" + claims.size() + " claims) ===");
        if (claims.isEmpty()) {
            System.out.println("No claims found.");
            return;
        }

        String format = "| %-4s | %-7s | %-8s | %-20s | %-22s | %-10s | %-19s |%n";
        String separator = "+------+---------+----------+----------------------+------------------------+------------+---------------------+";

        System.out.println(separator);
        System.out.printf(format, "CID", "Item ID", "Lost ID", "Claimant Name", "Contact", "Status", "Submitted At");
        System.out.println(separator);

        for (Claim c : claims) {
            System.out.printf(format,
                    c.getId(),
                    c.getItemId(),
                    c.getLostReportId() != null ? "#" + c.getLostReportId() : "-",
                    truncate(c.getClaimantName(), 20),
                    truncate(c.getClaimantContact(), 22),
                    c.getStatus().name(),
                    DateUtil.formatDateTime(c.getSubmittedAt()));
        }
        System.out.println(separator);
    }

    public static void printVerificationReviewCard(FoundItem item, Claim claim) {
        printVerificationReviewCard(item, claim, null);
    }

    public static void printVerificationReviewCard(FoundItem item, Claim claim, LostItem linkedLostItem) {
        System.out.println("\n====================== PRIVACY-VERIFIED CLAIM REVIEW ======================");
        System.out.println(" Claim ID          : #" + claim.getId() + " | Status: " + claim.getStatus());
        if (claim.getLostReportId() != null) {
            System.out.println(" Linked Lost Report: #" + claim.getLostReportId() + (linkedLostItem != null ? " (" + linkedLostItem.getItemName() + ")" : ""));
        }
        System.out.println(" Target Item ID    : #" + item.getId() + " (" + item.getItemName() + ")");
        System.out.println(" Item Category     : " + item.getCategory());
        System.out.println(" Location Found    : " + item.getLocationFound());
        System.out.println(" Date Found        : " + DateUtil.formatDate(item.getDateFound()));
        System.out.println(" Reported By       : " + item.getReportedBy());
        System.out.println(" Public Description: " + item.getDescription());
        System.out.println("----------------------------------------------------------------------------");
        System.out.println(" Claimant Name     : " + claim.getClaimantName());
        System.out.println(" Claimant Contact  : " + claim.getClaimantContact());
        System.out.println(" Claimant Story    : " + claim.getClaimDescription());
        System.out.println(" Submitted At      : " + DateUtil.formatDateTime(claim.getSubmittedAt()));
        System.out.println("------------------------- CONFIDENTIAL COMPARISON -------------------------");
        System.out.println(" [!] Finder's Secret Clue : " + item.getPrivateVerificationClue());
        System.out.println(" [?] Claimant's Answer    : " + claim.getVerificationResponse());
        if (linkedLostItem != null) {
            System.out.println(" [*] Lost Report Details  : " + linkedLostItem.getIdentifyingDetails());
        }
        System.out.println("============================================================================");
    }

    public static void printDashboard(DashboardStatistics stats) {
        System.out.println("\n======================= CAMPUSFIND LIVE STATISTICS =======================");
        System.out.println(" [Found Items]");
        System.out.println("   Total Items Reported        : " + stats.getTotalItemsReported());
        System.out.println("   * Currently Available       : " + stats.getAvailableItems());
        System.out.println("   * Pending Claims Underway   : " + stats.getClaimPendingItems());
        System.out.println("   * Returned to Owners        : " + stats.getReturnedItems());
        System.out.println("--------------------------------------------------------------------------");
        System.out.println(" [Lost Item Reports]");
        System.out.println("   Total Lost Reports Filed    : " + stats.getTotalLostItems());
        System.out.println("   * Open Reports Searching    : " + stats.getOpenLostItems());
        System.out.println("   * Matched Reports (Claimed) : " + stats.getMatchedLostItems());
        System.out.println("   * Resolved Reports (Closed) : " + stats.getResolvedLostItems());
        System.out.println("--------------------------------------------------------------------------");
        System.out.println(" [Claims & Verifications]");
        System.out.println("   Total Claims Filed          : " + stats.getTotalClaims());
        System.out.println("   * Pending Review            : " + stats.getPendingClaims());
        System.out.println("   * Approved                  : " + stats.getApprovedClaims());
        System.out.println("   * Rejected                  : " + stats.getRejectedClaims());
        System.out.println("--------------------------------------------------------------------------");
        System.out.println(" Most Frequent Found Location  : " + stats.getTopLocation());
        System.out.println("==========================================================================");
    }

    public static void printSuccess(String message) {
        System.out.println("\n[SUCCESS] " + message);
    }

    public static void printError(String message) {
        System.out.println("\n[ERROR] " + message);
    }

    public static void printWarning(String message) {
        System.out.println("\n[WARNING] " + message);
    }

    private static String truncate(String text, int maxLen) {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen - 3) + "...";
    }
}
