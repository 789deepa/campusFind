package com.campusfind.testutil;

import com.campusfind.dao.LostItemDao;
import com.campusfind.model.LostItem;
import com.campusfind.model.LostItemStatus;

import java.sql.Connection;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * In-memory test double for {@link LostItemDao}.
 */
public class InMemoryLostItemDao implements LostItemDao {

    private final Map<Integer, LostItem> items = new LinkedHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    @Override
    public LostItem save(LostItem item) {
        if (item.getId() == null) {
            item.setId(idGenerator.getAndIncrement());
        }
        items.put(item.getId(), copy(item));
        return item;
    }

    @Override
    public Optional<LostItem> findById(Integer id) {
        return Optional.ofNullable(items.get(id)).map(this::copy);
    }

    @Override
    public List<LostItem> findAllOpen() {
        return items.values().stream()
                .filter(i -> i.getStatus() == LostItemStatus.OPEN || i.getStatus() == LostItemStatus.MATCHED)
                .map(this::copy)
                .collect(Collectors.toList());
    }

    @Override
    public List<LostItem> searchByKeyword(String keyword) {
        String lower = keyword.toLowerCase();
        return items.values().stream()
                .filter(i -> i.getItemName().toLowerCase().contains(lower) ||
                             i.getDescription().toLowerCase().contains(lower))
                .map(this::copy)
                .collect(Collectors.toList());
    }

    @Override
    public List<LostItem> findByCategory(String category) {
        return items.values().stream()
                .filter(i -> i.getCategory().equalsIgnoreCase(category))
                .map(this::copy)
                .collect(Collectors.toList());
    }

    @Override
    public List<LostItem> findByLocation(String location) {
        String lower = location.toLowerCase();
        return items.values().stream()
                .filter(i -> i.getLocationLost().toLowerCase().contains(lower))
                .map(this::copy)
                .collect(Collectors.toList());
    }

    @Override
    public List<LostItem> findAll() {
        return items.values().stream().map(this::copy).collect(Collectors.toList());
    }

    @Override
    public boolean updateStatus(Integer id, LostItemStatus newStatus) {
        LostItem item = items.get(id);
        if (item != null) {
            item.setStatus(newStatus);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateStatus(Connection conn, Integer id, LostItemStatus newStatus) {
        return updateStatus(id, newStatus);
    }

    private LostItem copy(LostItem o) {
        return new LostItem(o.getId(), o.getItemName(), o.getCategory(), o.getDescription(),
                o.getLocationLost(), o.getDateLost(), o.getDateReported(), o.getStudentName(),
                o.getStudentContact(), o.getIdentifyingDetails(), o.getStatus());
    }
}
