package com.campusfind.service;

import com.campusfind.dao.ClaimDao;
import com.campusfind.dao.FoundItemDao;
import com.campusfind.exception.InvalidStatusTransitionException;
import com.campusfind.exception.ItemNotFoundException;
import com.campusfind.model.Claim;
import com.campusfind.model.ClaimStatus;
import com.campusfind.model.DashboardStatistics;
import com.campusfind.model.FoundItem;
import com.campusfind.model.ItemStatus;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Service encapsulating business logic for found items.
 * 
 * <p>Key Responsibilities:
 * <ul>
 *   <li>Validating items before storage</li>
 *   <li>Enforcing lifecycle status rules (e.g. items can only be marked RETURNED if an approved claim exists)</li>
 *   <li>Querying items with privacy preservation</li>
 * </ul>
 * </p>
 */
public class ItemService {
    private static final Logger logger = Logger.getLogger(ItemService.class.getName());

    private final FoundItemDao foundItemDao;
    private final ClaimDao claimDao;
    private final com.campusfind.dao.LostItemDao lostItemDao;
    private final ValidationService validationService;

    public ItemService(FoundItemDao foundItemDao, ClaimDao claimDao, ValidationService validationService) {
        this(foundItemDao, claimDao, null, validationService);
    }

    public ItemService(FoundItemDao foundItemDao, ClaimDao claimDao, com.campusfind.dao.LostItemDao lostItemDao, ValidationService validationService) {
        this.foundItemDao = foundItemDao;
        this.claimDao = claimDao;
        this.lostItemDao = lostItemDao;
        this.validationService = validationService;
    }

    /**
     * Reports a newly found item on campus.
     *
     * @param item the item to register
     * @return saved item with generated ID
     */
    public FoundItem reportItem(FoundItem item) {
        validationService.validateFoundItem(item);
        item.setStatus(ItemStatus.AVAILABLE);
        FoundItem saved = foundItemDao.save(item);
        logger.info("New found item registered successfully with ID: " + saved.getId());
        return saved;
    }

    public List<FoundItem> getAvailableItems() {
        return foundItemDao.findAllAvailable();
    }

    public Optional<FoundItem> getItemById(Integer id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return foundItemDao.findById(id);
    }

    public List<FoundItem> searchItems(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAvailableItems();
        }
        return foundItemDao.searchByKeyword(keyword.trim());
    }

    public List<FoundItem> filterByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return getAvailableItems();
        }
        return foundItemDao.findByCategory(category.trim());
    }

    public List<FoundItem> filterByLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            return getAvailableItems();
        }
        return foundItemDao.findByLocation(location.trim());
    }

    public List<FoundItem> getAllItemsHistory() {
        return foundItemDao.findAll();
    }

    public DashboardStatistics getDashboardStatistics() {
        return foundItemDao.getStatistics();
    }

    /**
     * Marks an item as RETURNED after verifying that an approved claim exists.
     *
     * @param itemId ID of the item to return
     * @return true if updated successfully
     * @throws ItemNotFoundException if item does not exist
     * @throws InvalidStatusTransitionException if item is already returned or has no approved claim
     */
    public boolean markItemAsReturned(Integer itemId) {
        FoundItem item = foundItemDao.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));

        if (item.getStatus() == ItemStatus.RETURNED) {
            throw new InvalidStatusTransitionException("Item ID " + itemId + " has already been returned to its owner.");
        }

        // Enforce business rule: An item may be marked RETURNED only after an approved claim
        Optional<Claim> approvedClaimOpt = claimDao.findByItemId(itemId).stream()
                .filter(c -> c.getStatus() == ClaimStatus.APPROVED)
                .findFirst();

        if (approvedClaimOpt.isEmpty()) {
            throw new InvalidStatusTransitionException(
                    "Cannot mark item #" + itemId + " as RETURNED. No APPROVED claim was found for this item. " +
                    "A claim must first be verified and approved before marking the physical item as returned."
            );
        }

        boolean updated = foundItemDao.updateStatus(itemId, ItemStatus.RETURNED);
        if (updated) {
            logger.info("Item #" + itemId + " successfully marked as RETURNED.");
            Claim approvedClaim = approvedClaimOpt.get();
            if (lostItemDao != null && approvedClaim.getLostReportId() != null) {
                lostItemDao.updateStatus(approvedClaim.getLostReportId(), com.campusfind.model.LostItemStatus.RESOLVED);
                logger.info("Associated lost item #" + approvedClaim.getLostReportId() + " marked as RESOLVED.");
            }
        }
        return updated;
    }
}
