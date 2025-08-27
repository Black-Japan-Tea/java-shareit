package ru.practicum.shareit.gateway.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.gateway.item.dto.ItemInRequestResponseDto;
import ru.practicum.shareit.gateway.request.client.RequestClient;
import ru.practicum.shareit.gateway.request.dto.RequestRequestDto;
import ru.practicum.shareit.gateway.request.dto.RequestResponseDto;
import ru.practicum.shareit.gateway.request.dto.RequestWithItemsResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RequestGatewayControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private RequestClient requestClient;

    @Autowired
    private ObjectMapper mapper;

    private RequestResponseDto requestResponseDto;
    private RequestWithItemsResponseDto requestWithItems;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        ItemInRequestResponseDto itemInRequest = new ItemInRequestResponseDto();
        itemInRequest.setId(1L);
        itemInRequest.setName("Книга");
        itemInRequest.setOwnerId(2L);

        requestResponseDto = new RequestResponseDto();
        requestResponseDto.setId(100L);
        requestResponseDto.setDescription("Нужна книга по ремонту");
        requestResponseDto.setCreated(now);

        requestWithItems = new RequestWithItemsResponseDto();
        requestWithItems.setId(100L);
        requestWithItems.setDescription("Нужна книга по ремонту");
        requestWithItems.setCreated(now);
        requestWithItems.setItems(List.of(itemInRequest));
    }

    @Test
    void createRequest_ValidData_ReturnsCreatedRequest() throws Exception {
        RequestRequestDto dto = new RequestRequestDto();
        dto.setDescription("Ищу книгу");

        when(requestClient.createRequest(any(RequestRequestDto.class), eq(1L))).thenReturn(requestResponseDto);

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.description").value("Нужна книга по ремонту"));
    }

    @Test
    void getUserRequests_ValidUser_ReturnsListOfRequests() throws Exception {
        when(requestClient.getUserRequests(eq(1L))).thenReturn(List.of(requestWithItems));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].items.size()").value(1));
    }

    @Test
    void getOtherUsersRequests_ValidUser_ReturnsListOfRequests() throws Exception {
        when(requestClient.getOtherUsersRequests(eq(1L))).thenReturn(List.of(requestWithItems));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].items.size()").value(1));
    }

    @Test
    void getRequestById_ValidId_ReturnsRequestWithItems() throws Exception {
        when(requestClient.getRequestById(eq(100L))).thenReturn(requestWithItems);

        mvc.perform(get("/requests/{requestId}", 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.description").value("Нужна книга по ремонту"))
                .andExpect(jsonPath("$.items[0].name").value("Книга"));
    }
}