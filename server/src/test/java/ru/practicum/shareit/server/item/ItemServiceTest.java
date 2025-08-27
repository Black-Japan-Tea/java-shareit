package ru.practicum.shareit.server.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.server.booking.model.Booking;
import ru.practicum.shareit.server.booking.model.BookingStatus;
import ru.practicum.shareit.server.booking.repository.BookingRepository;
import ru.practicum.shareit.server.exception.ForbiddenException;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.exception.ValidationException;
import ru.practicum.shareit.server.item.dal.CommentRepository;
import ru.practicum.shareit.server.item.dal.ItemRepository;
import ru.practicum.shareit.server.item.dto.*;
import ru.practicum.shareit.server.item.model.Comment;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.item.service.ItemServiceImpl;
import ru.practicum.shareit.server.user.dto.UserMapper;
import ru.practicum.shareit.server.user.model.User;
import ru.practicum.shareit.server.user.repository.UserRepository;
import ru.practicum.shareit.server.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private Item item;
    private Booking pastBooking;
    private Booking futureBooking;
    private Comment comment;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setName("Test Owner");
        owner.setEmail("owner@test.ru");

        item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner.getId());

        pastBooking = new Booking();
        pastBooking.setId(1L);
        pastBooking.setItem(item);
        pastBooking.setBooker(owner);
        pastBooking.setStatus(BookingStatus.APPROVED);
        pastBooking.setStart(LocalDateTime.now().minusDays(2));
        pastBooking.setEnd(LocalDateTime.now().minusDays(1));

        futureBooking = new Booking();
        futureBooking.setId(2L);
        futureBooking.setItem(item);
        futureBooking.setBooker(owner);
        futureBooking.setStatus(BookingStatus.APPROVED);
        futureBooking.setStart(LocalDateTime.now().plusDays(1));
        futureBooking.setEnd(LocalDateTime.now().plusDays(2));

        comment = new Comment();
        comment.setId(1L);
        comment.setText("Great item!");
        comment.setItemId(item.getId());
        comment.setAuthorId(owner.getId());
        comment.setCreated(LocalDateTime.now());
    }


    @Test
    void createItemTest() {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setName("New Item");
        dto.setDescription("Brand new");
        dto.setAvailable(true);

        when(userService.getUserById(anyLong())).thenReturn(UserMapper.toUserResponseDto(owner));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemResponseDto result = itemService.createItem(dto, owner.getId());

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
    }


    @Test
    void updateItemTest() {
        ItemUpdateRequestDto dto = new ItemUpdateRequestDto();
        dto.setName("Updated Name");

        when(itemRepository.getItemById(anyLong())).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemResponseDto result = itemService.updateItem(item.getId(), dto, owner.getId());

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
    }

    @Test
    void updateItemForbidden() {
        ItemUpdateRequestDto dto = new ItemUpdateRequestDto();
        dto.setName("Updated Name");

        when(itemRepository.getItemById(anyLong()))
                .thenReturn(Optional.of(item));

        assertThrows(ForbiddenException.class, () ->
                itemService.updateItem(item.getId(), dto, 999L));
    }

    @Test
    void getItemByIdTest() {

        when(itemRepository.getItemById(anyLong()))
                .thenReturn(Optional.of(item));

        when(userRepository.getUserById(anyLong()))
                .thenReturn(Optional.of(owner));

        when(commentRepository.findAllByItemId(anyLong()))
                .thenReturn(List.of(comment));

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(owner));

        ItemWithCommentsResponseDto result = itemService.getItemById(item.getId(), owner.getId());

        assertNotNull(result);
        assertEquals(item.getName(), result.getName());
        assertFalse(result.getComments().isEmpty());
    }

    @Test
    void getItemByIdNotFound() {
        assertThrows(NotFoundException.class, () ->
                itemService.getItemById(999L, owner.getId()));
    }

    @Test
    void getItemsByUserTest() {

        when(userService.getUserById(anyLong()))
                .thenReturn(UserMapper.toUserResponseDto(owner));

        when(itemRepository.findByOwner(anyLong()))
                .thenReturn(List.of(item));

        when(bookingRepository.findApprovedBookingsByItemIdsOrderByDesc(anyList()))
                .thenReturn(List.of(pastBooking, futureBooking));

        when(commentRepository.findAllCommentsByItemIdsOrderByDesc(anyList()))
                .thenReturn(List.of(comment));

        when(userRepository.findByIdIn(anyList()))
                .thenReturn(List.of(owner));

        Collection<ItemWithCommentsResponseDto> result = itemService.getItemsByUser(owner.getId());

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void searchItemsTest() {
        when(itemRepository.searchItems(anyString())).thenReturn(List.of(item));

        Collection<ItemResponseDto> result = itemService.searchItems("test");

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void searchItemsWithEmptyQuery() {
        Collection<ItemResponseDto> result = itemService.searchItems("");

        assertTrue(result.isEmpty());
    }

    @Test
    void addCommentTest() {
        CommentRequestDto dto = new CommentRequestDto();
        dto.setText("Good!");

        when(userService.getUserById(anyLong()))
                .thenReturn(UserMapper.toUserResponseDto(owner));

        when(itemRepository.getItemById(anyLong()))
                .thenReturn(Optional.of(item));

        when(bookingRepository.findTopByItemIdAndEndBeforeOrderByEndDesc(anyLong(), any(LocalDateTime.class)))
                .thenReturn(Optional.ofNullable(pastBooking));

        when(bookingRepository.findPastBookings(any(User.class)))
                .thenReturn(List.of(pastBooking));

        when(commentRepository.save(any(Comment.class)))
                .thenReturn(comment);

        CommentResponseDto result = itemService.addComment(dto, item.getId(), owner.getId());

        assertNotNull(result);
        assertEquals(dto.getText(), result.getText());
    }

    @Test
    void addCommentNoBooking() {

        when(userService.getUserById(anyLong()))
                .thenReturn(UserMapper.toUserResponseDto(owner));

        when(itemRepository
                .getItemById(anyLong())).thenReturn(Optional.of(item));

        when(bookingRepository.findTopByItemIdAndEndBeforeOrderByEndDesc(anyLong(), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () ->
                itemService.addComment(new CommentRequestDto(), item.getId(), owner.getId()));
    }

    @Test
    void addCommentUserDidNotBook() {

        when(userService.getUserById(anyLong()))
                .thenReturn(UserMapper.toUserResponseDto(owner));

        when(itemRepository.getItemById(anyLong()))
                .thenReturn(Optional.of(item));

        when(bookingRepository.findTopByItemIdAndEndBeforeOrderByEndDesc(anyLong(), any(LocalDateTime.class)))
                .thenReturn(Optional.of(pastBooking));

        when(bookingRepository.findPastBookings(any(User.class))).thenReturn(Collections.emptyList());

        assertThrows(ValidationException.class, () ->
                itemService.addComment(new CommentRequestDto(), item.getId(), owner.getId()));
    }

    @Test
    void getItemByIdUserIsNotOwner_noBookings() {

        Item item = new Item();
        item.setId(1L);
        item.setOwner(100L);

        when(itemRepository.getItemById(anyLong()))
                .thenReturn(Optional.of(item));

        User user = new User();
        user.setId(1L);

        when(userRepository.getUserById(anyLong())).thenReturn(Optional.of(user));

        ItemWithCommentsResponseDto result = itemService.getItemById(item.getId(), user.getId());

        assertNotNull(result);
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
    }

    @Test
    void getItemsByUserNoItems() {

        when(itemRepository.findByOwner(anyLong()))
                .thenReturn(Collections.emptyList());

        Collection<ItemWithCommentsResponseDto> result = itemService.getItemsByUser(owner.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void addCommentUserDidNotBookItemException() {

        Item item = new Item();
        item.setId(1L);

        User user = new User();
        user.setId(2L);

        when(itemRepository.getItemById(anyLong()))
                .thenReturn(Optional.of(item));

        when(userService.getUserById(anyLong()))
                .thenReturn(UserMapper.toUserResponseDto(user));

        when(bookingRepository.findTopByItemIdAndEndBeforeOrderByEndDesc(anyLong(), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        CommentRequestDto dto = new CommentRequestDto();
        dto.setText("Nice!");

        assertThrows(ValidationException.class, () ->
                itemService.addComment(dto, item.getId(), user.getId()));
    }

    @Test
    void getItemsByUserWithItemsButNoBookingsOrComments() {
        Item item = new Item();
        item.setId(1L);
        item.setOwner(owner.getId());


        when(itemRepository.findByOwner(anyLong()))
                .thenReturn(List.of(item));

        when(commentRepository.findAllCommentsByItemIdsOrderByDesc(anyList()))
                .thenReturn(Collections.emptyList());

        when(bookingRepository.findApprovedBookingsByItemIdsOrderByDesc(anyList()))
                .thenReturn(Collections.emptyList());

        Collection<ItemWithCommentsResponseDto> result = itemService.getItemsByUser(owner.getId());

        assertFalse(result.isEmpty());
        assertNull(result.iterator().next().getLastBooking());
        assertNull(result.iterator().next().getNextBooking());
        assertTrue(result.iterator().next().getComments().isEmpty());
    }
}