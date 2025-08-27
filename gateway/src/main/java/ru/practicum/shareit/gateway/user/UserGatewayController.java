package ru.practicum.shareit.gateway.user;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.gateway.user.client.UserClient;
import ru.practicum.shareit.gateway.user.dto.UserRequestDto;
import ru.practicum.shareit.gateway.user.dto.UserResponseDto;
import ru.practicum.shareit.gateway.user.dto.UserUpdateRequestDto;


@RestController
@RequestMapping(path = "/users")
@AllArgsConstructor
public class UserGatewayController {

    private final UserClient userClient;

    @PostMapping
    public UserResponseDto createUser(@RequestBody
                                      @Valid
                                      UserRequestDto newUser) {
        return userClient.createUser(newUser);
    }

    @GetMapping("/{userId}")
    public UserResponseDto getUserById(@PathVariable long userId) {
        return userClient.getUserById(userId);
    }


    @PatchMapping("/{userId}")
    public UserResponseDto updateUser(@RequestBody
                                      @Valid
                                      UserUpdateRequestDto userDataToUpdate,
                                      @PathVariable
                                      Long userId) {
        return userClient.updateUser(userDataToUpdate, userId);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable
                           long userId) {
        userClient.deleteUser(userId);
    }
}