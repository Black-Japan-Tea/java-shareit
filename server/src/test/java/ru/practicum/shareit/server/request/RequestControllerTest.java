package ru.practicum.shareit.server.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.server.item.dto.ItemInRequestResponseDto;
import ru.practicum.shareit.server.request.controller.RequestController;
import ru.practicum.shareit.server.request.dto.RequestRequestDto;
import ru.practicum.shareit.server.request.dto.RequestResponseDto;
import ru.practicum.shareit.server.request.dto.RequestWithItemsResponseDto;
import ru.practicum.shareit.server.request.service.RequestService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RequestController.class)
class RequestControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private RequestService requestService;

    @Autowired
    private ObjectMapper mapper;

    private RequestResponseDto requestResponseDto;
    private RequestWithItemsResponseDto requestWithItems;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        ItemInRequestResponseDto itemInRequest = new ItemInRequestResponseDto();
        itemInRequest.setId(1L);
        itemInRequest.setName("Book");
        itemInRequest.setOwnerId(2L);

        requestResponseDto = new RequestResponseDto();
        requestResponseDto.setId(100L);
        requestResponseDto.setDescription("Need a good book");
        requestResponseDto.setCreated(now);

        requestWithItems = new RequestWithItemsResponseDto();
        requestWithItems.setId(100L);
        requestWithItems.setDescription("Need a good book");
        requestWithItems.setCreated(now);
        requestWithItems.setItems(List.of(itemInRequest));
    }

    @Test
    void createRequest_ReturnsCreatedRequest() throws Exception {
        RequestRequestDto dto = new RequestRequestDto();
        dto.setDescription("Looking for a book");

        when(requestService.createRequest(any(RequestRequestDto.class), eq(1L)))
                .thenReturn(requestResponseDto);

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(mapper.writeValueAsString(dto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestResponseDto.getId()))
                .andExpect(jsonPath("$.description").value(requestResponseDto.getDescription()));
    }

    @Test
    void getUserRequests_ReturnsListOfRequests() throws Exception {
        when(requestService.getUserRequests(eq(1L))).thenReturn(List.of(requestWithItems));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].items.size()").value(1))
                .andExpect(jsonPath("$[0].items[0].id").value(1L))
                .andExpect(jsonPath("$[0].items[0].name").value("Book"))
                .andExpect(jsonPath("$[0].items[0].ownerId").value(2L));
    }

    @Test
    void getOtherUsersRequests_ReturnsListOfRequests() throws Exception {
        when(requestService.getOtherUsersRequests(eq(1L))).thenReturn(List.of(requestWithItems));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].items.size()").value(1))
                .andExpect(jsonPath("$[0].items[0].id").value(1L))
                .andExpect(jsonPath("$[0].items[0].name").value("Book"))
                .andExpect(jsonPath("$[0].items[0].ownerId").value(2L));
    }

    @Test
    void getRequestById_ReturnsRequestWithItems() throws Exception {
        when(requestService.getRequestById(eq(100L))).thenReturn(requestWithItems);

        mvc.perform(get("/requests/{requestId}", 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.description").value("Need a good book"))
                .andExpect(jsonPath("$.items[0].id").value(1L))
                .andExpect(jsonPath("$.items[0].name").value("Book"))
                .andExpect(jsonPath("$.items[0].ownerId").value(2L));
    }
}