package ru.practicum.shareit.gateway.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.gateway.user.client.UserClient;
import ru.practicum.shareit.gateway.user.dto.UserRequestDto;
import ru.practicum.shareit.gateway.user.dto.UserResponseDto;
import ru.practicum.shareit.gateway.user.dto.UserUpdateRequestDto;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserGatewayControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private UserClient userClient;

    @Autowired
    private ObjectMapper mapper;

    private UserResponseDto userResponseDto;

    @BeforeEach
    void setUp() {
        userResponseDto = new UserResponseDto();
        userResponseDto.setId(1L);
        userResponseDto.setName("Георгий Чивчян");
        userResponseDto.setEmail("geo.chv@example.com");
    }

    @Test
    void createUser_ValidData_ReturnsCreatedUser() throws Exception {
        UserRequestDto dto = new UserRequestDto();
        dto.setName("Аркадий");
        dto.setEmail("arc.tsar@example.com");

        when(userClient.createUser(any(UserRequestDto.class))).thenReturn(userResponseDto);

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(mapper.writeValueAsString(dto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Георгий Чивчян"))
                .andExpect(jsonPath("$.email").value("geo.chv@example.com"));

        verify(userClient, times(1)).createUser(any(UserRequestDto.class));
    }

    @Test
    void getUserById_ValidId_ReturnsUser() throws Exception {
        when(userClient.getUserById(1L)).thenReturn(userResponseDto);

        mvc.perform(get("/users/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Георгий Чивчян"))
                .andExpect(jsonPath("$.email").value("geo.chv@example.com"));

        verify(userClient, times(1)).getUserById(1L);
    }

    @Test
    void updateUser_ValidData_ReturnsUpdatedUser() throws Exception {
        UserUpdateRequestDto dto = new UserUpdateRequestDto();
        dto.setName("New Name");
        dto.setEmail("new.email@example.com");

        when(userClient.updateUser(any(UserUpdateRequestDto.class), eq(1L))).thenReturn(userResponseDto);

        mvc.perform(patch("/users/{userId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Георгий Чивчян"));

        verify(userClient, times(1)).updateUser(any(UserUpdateRequestDto.class), eq(1L));
    }

    @Test
    void deleteUser_ValidId_DeletesUser() throws Exception {
        mvc.perform(delete("/users/{userId}", 1L))
                .andExpect(status().isOk());

        verify(userClient, times(1)).deleteUser(1L);
    }
}