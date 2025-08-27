package ru.practicum.shareit.gateway.item;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.gateway.item.client.ItemClient;
import ru.practicum.shareit.gateway.item.dto.*;

import java.util.Collection;
import java.util.Set;


@RestController
@RequestMapping("/items")
@AllArgsConstructor
public class ItemGatewayController {

    private final ItemClient itemClient;

    @PostMapping
    public ItemResponseDto createItem(
            @RequestBody
            @Valid
            ItemRequestDto newItem,
            @RequestHeader("X-Sharer-User-Id")
            Long userId) {
        return itemClient.createItem(newItem, userId);

    }

    @PatchMapping("/{itemId}")
    public ItemResponseDto updateItem(
            @PathVariable long itemId,
            @RequestBody
            @Valid
            ItemUpdateRequestDto itemDataToUpdate,
            @RequestHeader("X-Sharer-User-Id")
            Long userId) {
        return itemClient.updateItem(itemId, itemDataToUpdate, userId);
    }

    @GetMapping("/{itemId}")
    public ItemWithCommentsResponseDto getItem(
            @PathVariable
            Long itemId,
            @RequestHeader("X-Sharer-User-Id")
            Long userId) {
        return itemClient.getItem(itemId, userId);
    }

    @GetMapping
    public Collection<ItemWithCommentsResponseDto> getItemsByUser(@RequestHeader("X-Sharer-User-Id")
                                                                  Long userId) {
        return itemClient.getItemsByUser(userId);
    }

    @GetMapping("/search")
    public Collection<ItemResponseDto> searchItems(
            @RequestHeader("X-Sharer-User-Id")
            Long userId,
            @RequestParam("text")
            String searchString) {

        if (searchString.isEmpty()) {
            return Set.of();
        }

        return itemClient.searchItems(userId, searchString);
    }

    @PostMapping("/{itemId}/comment")
    public CommentResponseDto addComment(
            @Valid
            @RequestBody
            CommentRequestDto dto,
            @RequestHeader("X-Sharer-User-Id")
            Long userId,
            @PathVariable Long itemId) {
        return itemClient.addComment(dto, userId, itemId);
    }
}