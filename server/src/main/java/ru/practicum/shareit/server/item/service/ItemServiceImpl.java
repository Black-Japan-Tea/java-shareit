package ru.practicum.shareit.server.item.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.server.booking.model.Booking;
import ru.practicum.shareit.server.booking.repository.BookingRepository;
import ru.practicum.shareit.server.exception.ForbiddenException;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.exception.ValidationException;
import ru.practicum.shareit.server.item.dal.CommentRepository;
import ru.practicum.shareit.server.item.dal.ItemRepository;
import ru.practicum.shareit.server.item.dto.*;
import ru.practicum.shareit.server.item.model.Comment;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.request.model.Request;
import ru.practicum.shareit.server.request.repository.RequestRepository;
import ru.practicum.shareit.server.user.dto.UserMapper;
import ru.practicum.shareit.server.user.model.User;
import ru.practicum.shareit.server.user.repository.UserRepository;
import ru.practicum.shareit.server.user.service.UserService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository repository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final RequestRepository requestRepository;

    @Override
    public ItemResponseDto createItem(ItemRequestDto newItemDto, Long ownerId) {

        userService.getUserById(ownerId);

        if (newItemDto.getRequestId() != null) {
            Optional<Request> maybeRequest = requestRepository.getRequestById(newItemDto.getRequestId());
            if (maybeRequest.isEmpty()) {
                throw new NotFoundException("Request with id=" + newItemDto.getRequestId() + " not found");
            }
        }

        Item newItem = ItemMapper.toItem(newItemDto);
        newItem.setOwner(ownerId);

        return ItemMapper.toItemResponseDto(repository.save(newItem));
    }

    @Override
    public ItemResponseDto updateItem(Long itemId, ItemUpdateRequestDto itemDataToUpdate, Long userId) {

        userService.getUserById(userId);

        Item existingItem = checkAndGetItemById(itemId);

        if (!existingItem.getOwner().equals(userId)) {
            throw new ForbiddenException("Only owner can update item id=" + itemId);
        }

        if (itemDataToUpdate.getName() != null) {
            existingItem.setName(itemDataToUpdate.getName());
        }

        if (itemDataToUpdate.getDescription() != null) {
            existingItem.setDescription(itemDataToUpdate.getDescription());
        }

        if (itemDataToUpdate.getAvailable() != null) {
            existingItem.setAvailable(itemDataToUpdate.getAvailable());
        }

        return ItemMapper.toItemResponseDto(repository.save(existingItem));
    }

    @Override
    public ItemWithCommentsResponseDto getItemById(Long itemId, Long userId) {

        Optional<User> maybeUser = userRepository.getUserById(userId);
        if (maybeUser.isEmpty()) {
            throw new NotFoundException("User with id=" + userId + " not found");
        }
        User user = maybeUser.get();

        Item item = checkAndGetItemById(itemId);

        LocalDateTime nextBookingDate = null;
        LocalDateTime lastBookingDate = null;

        if (user.getId().equals(item.getOwner())) {
            Booking lastBooking = getLatestPastBooking(item);
            Booking nextBooking = getUpcomingBooking(item);


            if (lastBooking != null) {
                lastBookingDate = lastBooking.getEnd();
            }
            if (nextBooking != null) {
                nextBookingDate = nextBooking.getStart();
            }
        }

        List<CommentResponseDto> comments = getCommentsWithAuthors(item);

        return ItemMapper.toItemWithCommentsResponseDto(item, lastBookingDate, nextBookingDate, comments);
    }

    private Booking getLatestPastBooking(Item item) {
        return bookingRepository
                .findTopByItemIdAndEndBeforeOrderByEndDesc(item.getId(), LocalDateTime.now())
                .orElse(null);
    }

    private Booking getUpcomingBooking(Item item) {
        return bookingRepository
                .findTopByItemIdAndStartAfterOrderByStartAsc(item.getId(), LocalDateTime.now())
                .orElse(null);
    }

    private List<CommentResponseDto> getCommentsWithAuthors(Item item) {
        return commentRepository
                .findAllByItemId(item.getId())
                .stream()
                .map(comment -> {
                    String authorName = getUserFullName(comment.getAuthorId());
                    return ItemMapper.toCommentResponseDto(comment, authorName);
                })
                .toList();
    }

    private String getUserFullName(Long userId) {
        return userRepository.findById(userId)
                .map(User::getName)
                .orElse("Unknown User");
    }


    @Override
    public Collection<ItemWithCommentsResponseDto> getItemsByUser(Long userId) {
        userService.getUserById(userId);

        Collection<Item> itemsByOwner = repository.findByOwner(userId);

        List<Long> itemIds = itemsByOwner.stream()
                .map(Item::getId)
                .toList();

        List<Booking> bookings = bookingRepository.findApprovedBookingsByItemIdsOrderByDesc(itemIds);

        List<Comment> comments = commentRepository.findAllCommentsByItemIdsOrderByDesc(itemIds);

        List<Long> userIds = comments.stream()
                .map(Comment::getAuthorId)
                .collect(Collectors.toList());
        List<User> users = userRepository.findByIdIn(userIds);

        Map<Long, String> userNamesMap = users.stream()
                .collect(Collectors.toMap(User::getId, User::getName));

        Map<Long, List<Booking>> itemsBooking = bookings.stream()
                .collect(Collectors.groupingBy(b -> b.getItem().getId()));

        Map<Long, List<Comment>> itemsComments = comments.stream()
                .collect(Collectors.groupingBy(Comment::getItemId));

        return itemsByOwner.stream()
                .map(item -> {
                    List<Booking> itemBookings = itemsBooking.getOrDefault(item.getId(), Collections.emptyList());
                    List<Comment> itemComments = itemsComments.getOrDefault(item.getId(), Collections.emptyList());

                    LocalDateTime lastBooking = null;
                    LocalDateTime nextBooking = null;

                    if (!itemBookings.isEmpty()) {
                        LocalDateTime now = LocalDateTime.now();

                        List<Booking> futureBookings = itemBookings.stream()
                                .filter(b -> b.getStart().isAfter(now))
                                .sorted(Comparator.comparing(Booking::getStart))
                                .toList();

                        List<Booking> pastBookings = itemBookings.stream()
                                .filter(b -> b.getEnd().isBefore(now))
                                .sorted(Comparator.comparing(Booking::getEnd).reversed())
                                .toList();

                        if (!pastBookings.isEmpty()) {
                            lastBooking = pastBookings.getFirst().getEnd();
                        }

                        if (!futureBookings.isEmpty()) {
                            nextBooking = futureBookings.getFirst().getStart();
                        }
                    }

                    List<CommentResponseDto> commentsDto = itemComments.stream()
                            .map(comment ->
                                    ItemMapper.toCommentResponseDto(comment, userNamesMap.get(comment.getAuthorId())))
                            .toList();

                    return ItemMapper.toItemWithCommentsResponseDto(
                            item,
                            lastBooking,
                            nextBooking,
                            commentsDto);
                })
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ItemResponseDto> searchItems(String searchString) {


        return repository.searchItems(searchString).stream()
                .map(ItemMapper::toItemResponseDto)
                .collect(Collectors.toSet());
    }

    @Override
    public CommentResponseDto addComment(CommentRequestDto dto, Long itemId, Long userId) {

        User user = UserMapper.toUser(userService.getUserById(userId));

        Item item = checkAndGetItemById(itemId);
        Booking lastBooking = getLatestPastBooking(item);

        if (lastBooking == null) {
            throw new ValidationException("Can't comment without booking ends");
        }

        if (bookingRepository.findPastBookings(user).stream()
                .filter(booking -> booking.getItem().getId().equals(itemId))
                .findFirst().isEmpty()) {
            throw new ValidationException("Only user who booked item can add comment");
        }

        Comment comment = new Comment();
        comment.setText(dto.getText());
        comment.setItemId(itemId);
        comment.setAuthorId(userId);
        comment.setCreated(LocalDateTime.now());

        commentRepository.save(comment);

        return ItemMapper.toCommentResponseDto(comment, user.getName());
    }


    private Item checkAndGetItemById(Long itemId) {

        Optional<Item> maybeItem = repository.getItemById(itemId);

        if (maybeItem.isEmpty()) {
            throw new NotFoundException("Item with id=" + itemId + " not found");
        }
        return maybeItem.get();
    }
}