package ru.practicum.shareit.item.service;

import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.comment.CommentRepository;
import ru.practicum.shareit.comment.dto.CommentRequestDto;
import ru.practicum.shareit.comment.dto.CommentResponseDto;
import ru.practicum.shareit.comment.mapper.CommentMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.exception.ItemAccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final CommentMapper commentMapper;

    @Override
    public List<ItemDto> getAllItemsByOwner(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь " + userId + " не найден");
        }

        List<Item> items = itemRepository.findByOwnerIdOrderById(userId);
        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());

        Map<Long, List<Booking>> bookingsMap = bookingRepository
                .findApprovedBookingsForItems(itemIds)
                .stream()
                .collect(Collectors.groupingBy(b -> b.getItem().getId()));

        Map<Long, List<Comment>> commentsMap = commentRepository
                .findByItemIdIn(itemIds)
                .stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId()));

        LocalDateTime now = LocalDateTime.now();

        return items.stream().map(item -> {
            ItemDto itemDto = itemMapper.toItemDto(item);

            processBookings(itemDto, bookingsMap.getOrDefault(item.getId(), Collections.emptyList()), now);

            itemDto.setComments(commentsMap.getOrDefault(item.getId(), Collections.emptyList())
                    .stream()
                    .map(commentMapper::toCommentResponseDto)
                    .collect(Collectors.toList()));

            return itemDto;
        }).collect(Collectors.toList());
    }

    private void processBookings(ItemDto itemDto, List<Booking> bookings, LocalDateTime now) {
        bookings.stream()
                .filter(b -> b.getEnd().isBefore(now))
                .max(Comparator.comparing(Booking::getEnd))
                .ifPresent(last -> itemDto.setLastBooking(
                        BookingShortDto.builder()
                                .id(last.getId())
                                .bookerId(last.getBooker().getId())
                                .build()));

        bookings.stream()
                .filter(b -> b.getStart().isAfter(now))
                .min(Comparator.comparing(Booking::getStart))
                .ifPresent(next -> itemDto.setNextBooking(
                        BookingShortDto.builder()
                                .id(next.getId())
                                .bookerId(next.getBooker().getId())
                                .build()));
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        List<Item> items = itemRepository.searchAvailableItems(text.toLowerCase());
        return itemMapper.toItemDtoList(items);
    }

    @Override
    @Transactional
    public CommentResponseDto addComment(Long userId, Long itemId, CommentRequestDto commentRequestDto) {
        boolean hasBooked = bookingRepository.existsByBookerIdAndItemIdAndEndBefore(
                userId, itemId, LocalDateTime.now());

        if (!hasBooked) {
            throw new ValidationException("Вы можете оставлять комментарий только к тем вещам, которые бронировали");
        }

        Comment comment = commentMapper.toComment(commentRequestDto);
        comment.setItem(getItem(itemId));
        comment.setAuthor(getUser(userId));
        comment.setCreated(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toCommentResponseDto(savedComment);
    }

    @Override
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        Item item = itemMapper.toItem(itemDto);
        item.setOwner(getUser(userId));
        item.setAvailable(itemDto.getAvailable() != null ? itemDto.getAvailable() : true);

        Item savedItem = itemRepository.save(item);
        return itemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        if (!getItem(itemId).getOwner().getId().equals(userId)) {
            throw new ItemAccessDeniedException("Пользователь не является владельцем вещи");
        }

        if (itemDto.getName() != null) {
            getItem(itemId).setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            getItem(itemId).setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            getItem(itemId).setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(getItem(itemId));
        return itemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto getItemById(Long userId, Long itemId) {
        ItemDto itemDto = itemMapper.toItemDto(getItem(itemId));

        itemDto.setComments(commentRepository.findByItemId(itemId)
                .stream()
                .map(commentMapper::toCommentResponseDto)
                .collect(Collectors.toList()));

        if (getItem(itemId).getOwner().getId().equals(userId)) {
            List<Booking> bookings = bookingRepository.findByItemId(itemId);
            processBookings(itemDto, bookings, LocalDateTime.now());
        }

        return itemDto;
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь " + userId + " не найден"));
    }

    private Item getItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь " + itemId + " не найдена"));
    }
}