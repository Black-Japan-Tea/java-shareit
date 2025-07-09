package ru.practicum.shareit.request.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryItemRequestStorage implements ItemRequestStorage {
    private final Map<Long, ItemRequest> requests = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public ItemRequest createRequest(ItemRequest request) {
        request.setId(idGenerator.getAndIncrement());
        requests.put(request.getId(), request);
        return request;
    }

    @Override
    public List<ItemRequest> getAllByRequestor(Long requestorId) {
        return requests.values().stream()
                .filter(r -> r.getRequestor().getId().equals(requestorId))
                .toList();
    }

    @Override
    public List<ItemRequest> getAll(Long userId, Integer from, Integer size) {
        return requests.values().stream()
                .filter(r -> !r.getRequestor().getId().equals(userId))
                .skip(from)
                .limit(size)
                .toList();
    }

    @Override
    public Optional<ItemRequest> getById(Long requestId) {
        return Optional.ofNullable(requests.get(requestId));
    }
}