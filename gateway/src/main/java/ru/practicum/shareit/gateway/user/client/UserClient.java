package ru.practicum.shareit.gateway.user.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.gateway.user.dto.UserRequestDto;
import ru.practicum.shareit.gateway.user.dto.UserResponseDto;
import ru.practicum.shareit.gateway.user.dto.UserUpdateRequestDto;

@FeignClient(
        url = "${shareit-server.url}",
        name = "user-client",
        path = "/users")
public interface UserClient {

    @PostMapping
    UserResponseDto createUser(@RequestBody UserRequestDto userRequestDto);

    @GetMapping("/{userId}")
    UserResponseDto getUserById(@PathVariable("userId") Long userId);

    @PatchMapping("/{userId}")
    UserResponseDto updateUser(@RequestBody UserUpdateRequestDto userUpdateRequestDto,
                               @PathVariable("userId") Long userId);

    @DeleteMapping("/{userId}")
    void deleteUser(@PathVariable("userId") Long userId);
}