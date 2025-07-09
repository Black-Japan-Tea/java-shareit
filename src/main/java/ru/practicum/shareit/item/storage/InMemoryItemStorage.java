package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryItemStorage implements ItemStorage {
    private final Map<Long, Item> items = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Item createItem(Item item) {
        item.setId(idGenerator.getAndIncrement());
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item updateItem(Item item) {
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> getItemById(Long itemId) {
        return Optional.ofNullable(items.get(itemId));
    }

    @Override
    public List<Item> getAllItemsByOwner(Long ownerId) {
        return items.values().stream()
                .filter(item -> item.getOwner().getId().equals(ownerId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> searchItems(String text) {
        String lowerText = text.toLowerCase();
        return items.values().stream().
                filter(item -> (item.getName().toLowerCase().contains(lowerText) || item.getDescription().toLowerCase().contains(lowerText))).
                collect(Collectors.toList());
    }

    @Override
    public List<Item> findByRequestId(Long requestId) {
        return items.values().stream()
                .filter(item -> item.getRequest() != null && item.getRequest().getId().equals(requestId))
                .collect(Collectors.toList());
    }
}