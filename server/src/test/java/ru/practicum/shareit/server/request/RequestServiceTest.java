package ru.practicum.shareit.server.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.item.dal.ItemRepository;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.request.dto.RequestRequestDto;
import ru.practicum.shareit.server.request.dto.RequestResponseDto;
import ru.practicum.shareit.server.request.dto.RequestWithItemsResponseDto;
import ru.practicum.shareit.server.request.model.Request;
import ru.practicum.shareit.server.request.repository.RequestRepository;
import ru.practicum.shareit.server.request.service.RequestServiceImpl;
import ru.practicum.shareit.server.user.model.User;
import ru.practicum.shareit.server.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RequestServiceImpl requestService;

    private User user;
    private Request request;
    private Item item;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("user@example.com");

        item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(user.getId());
        item.setRequest(1L);

        request = new Request();
        request.setId(1L);
        request.setDescription("Need a test item");
        request.setRequestor(user.getId());
        request.setCreated(LocalDateTime.now());


    }


    @Test
    void createRequest_success() {
        RequestRequestDto dto = new RequestRequestDto();
        dto.setDescription("Need a test item");

        when(userRepository.getUserById(anyLong())).thenReturn(Optional.of(user));
        when(requestRepository.save(any(Request.class))).thenReturn(request);

        RequestResponseDto result = requestService.createRequest(dto, user.getId());

        assertNotNull(result);
        assertEquals(request.getId(), result.getId());
        assertEquals(request.getDescription(), result.getDescription());
    }

    @Test
    void createRequest_userNotFound_throwsNotFoundException() {
        RequestRequestDto dto = new RequestRequestDto();
        dto.setDescription("Need a test item");

        when(userRepository.getUserById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                requestService.createRequest(dto, 999L));
    }


    @Test
    void getUserRequests_success() {
        when(userRepository.getUserById(anyLong())).thenReturn(Optional.of(user));
        when(requestRepository.findAllByRequestorOrderByCreated(anyLong())).thenReturn(List.of(request));

        Set<Request> requestList = new HashSet<>();
        requestList.add(request);

        Collection<Long> requestsIds = requestList.stream()
                .map(Request::getId)
                .collect(Collectors.toSet());

        Collection<Item> items = new ArrayList<>();
        items.add(item);
        when(itemRepository.findAllByRequestIn(requestsIds))
                .thenReturn(items);


        Collection<RequestWithItemsResponseDto> result = requestService.getUserRequests(user.getId());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(request.getId(), result.iterator().next().getId());
    }

    @Test
    void getUserRequests_userNotFound_throwsNotFoundException() {
        when(userRepository.getUserById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                requestService.getUserRequests(999L));
    }


    @Test
    void getOtherUsersRequests_success() {


        Long requestId = request.getId();
        Set<Long> idList = new HashSet<>();

        idList.add(requestId);


        Long userId = user.getId();
        when(userRepository.getUserById(userId))
                .thenReturn(Optional.of(user));

        Set<Request> requestList = new HashSet<>();
        requestList.add(request);
        when(requestRepository.findAllByRequestorIsNot(userId))
                .thenReturn(requestList);

        Collection<Long> requestsIds = requestList.stream()
                .map(Request::getId)
                .collect(Collectors.toSet());

        Collection<Item> items = new ArrayList<>();
        items.add(item);
        when(itemRepository.findAllByRequestIn(requestsIds))
                .thenReturn(items);


        Collection<RequestWithItemsResponseDto> result = requestService.getOtherUsersRequests(userId);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(requestId, result.iterator().next().getId());
    }


    @Test
    void getOtherUsersRequests_userNotFound_throwsNotFoundException() {
        when(userRepository.getUserById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                requestService.getOtherUsersRequests(999L));
    }

    @Test
    void getRequestById_success() {
        when(requestRepository.getRequestById(anyLong())).thenReturn(Optional.of(request));
        when(itemRepository.findAllByRequestIn(anySet()))
                .thenReturn(List.of(item));

        RequestWithItemsResponseDto result = requestService.getRequestById(request.getId());

        assertNotNull(result);
        assertEquals(request.getId(), result.getId());
        assertFalse(result.getItems().isEmpty());
    }

    @Test
    void getRequestById_requestNotFound_throwsNotFoundException() {
        when(requestRepository.getRequestById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                requestService.getRequestById(999L));
    }
}