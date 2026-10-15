package com.campusfind.testutil;

import com.campusfind.dao.FoundItemDao;
import com.campusfind.model.DashboardStatistics;
import com.campusfind.model.FoundItem;
import com.campusfind.model.ItemStatus;

import java.sql.Connection;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * In-memory test double for {@link FoundItemDao}.
 * Enables fast, isolated unit testing of service logic without needing a running MySQL instance.
 */
public class InMemoryFoundItemDao implements FoundItemDao {

    private final Map<Integer, FoundItem> items = new LinkedHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    @Override
    public FoundItem save(FoundItem item) {
        if (item.getId() == null) {
            item.setId(idGenerator.getAndIncrement());
        }
        items.put(item.getId(), copy(item));
        return item;
    }

    @Override
    public Optional<FoundItem> findById(Integer id) {
        return Optional.ofNullable(items.get(id)).map(this::copy);
    }

    @Override
    public List<FoundItem> findAllAvailable() {
        return items.values().stream()
                .filter(i -> i.getStatus() == ItemStatus.AVAILABLE || i.getStatus() == ItemStatus.CLAIM_PENDING)
                .map(this::copy)
                .collect(Collectors.toList());
    }

    @Override
    public List<FoundItem> searchByKeyword(String keyword) {
        String lower = keyword.toLowerCase();
        return items.values().stream()
                .filter(i -> i.getItemName().toLowerCase().contains(lower) ||
                             i.getDescription().toLowerCase().contains(lower))
                .map(this::copy)
                .collect(Collectors.toList());
    }

    @Override
    public List<FoundItem> findByCategory(String category) {
        return items.values().stream()
                .filter(i -> i.getCategory().equalsIgnoreCase(category))
                .map(this::copy)
                .collect(Collectors.toList());
    }

    @Override
    public List<FoundItem> findByLocation(String location) {
        return items.values().stream()
                .filter(i -> i.getLocationFound().toLowerCase().contains(location.toLowerCase()))
                .map(this::copy)
                .collect(Collectors.toList());
    }

    @Override
    public List<FoundItem> findAll() {
        return items.values().stream().map(this::copy).collect(Collectors.toList());
    }

    @Override
    public boolean updateStatus(Integer id, ItemStatus newStatus) {
        FoundItem item = items.get(id);
        if (item != null) {
            item.setStatus(newStatus);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateStatus(Connection conn, Integer id, ItemStatus newStatus) {
        return updateStatus(id, newStatus);
    }

    @Override
    public DashboardStatistics getStatistics() {
        int total = items.size();
        int available = (int) items.values().stream().filter(i -> i.getStatus() == ItemStatus.AVAILABLE).count();
        int pending = (int) items.values().stream().filter(i -> i.getStatus() == ItemStatus.CLAIM_PENDING).count();
        int returned = (int) items.values().stream().filter(i -> i.getStatus() == ItemStatus.RETURNED).count();

        return new DashboardStatistics(total, available, pending, returned, 0, 0, 0, 0, "Library");
    }

    private FoundItem copy(FoundItem o) {
        return new FoundItem(o.getId(), o.getItemName(), o.getCategory(), o.getDescription(),
                o.getLocationFound(), o.getDateFound(), o.getDateReported(), o.getReportedBy(),
                o.getPrivateVerificationClue(), o.getStatus());
    }
}
