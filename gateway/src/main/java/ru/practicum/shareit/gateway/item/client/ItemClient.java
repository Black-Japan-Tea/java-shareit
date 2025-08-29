package ru.practicum.shareit.gateway.item.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.gateway.item.dto.*;

import java.util.Collection;

@FeignClient(
        url = "${shareit-server.url}",
        name = "item-client",
        path = "/items")
public interface ItemClient {

    @PostMapping
    ItemResponseDto createItem(
            @RequestBody ItemRequestDto itemDto,
            @RequestHeader("X-Sharer-User-Id") Long userId);

    @PatchMapping("/{itemId}")
    ItemResponseDto updateItem(
            @PathVariable("itemId") Long itemId,
            @RequestBody ItemUpdateRequestDto itemUpdateDto,
            @RequestHeader("X-Sharer-User-Id") Long userId);

    @GetMapping("/{itemId}")
    ItemWithCommentsResponseDto getItem(
            @PathVariable("itemId") Long itemId,
            @RequestHeader("X-Sharer-User-Id") Long userId);

    @GetMapping
    Collection<ItemWithCommentsResponseDto> getItemsByUser(
            @RequestHeader("X-Sharer-User-Id") Long userId);

    @GetMapping("/search")
    Collection<ItemResponseDto> searchItems(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam("text") String text);

    @PostMapping("/{itemId}/comment")
    CommentResponseDto addComment(
            @RequestBody CommentRequestDto commentDto,
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable("itemId") Long itemId);
}