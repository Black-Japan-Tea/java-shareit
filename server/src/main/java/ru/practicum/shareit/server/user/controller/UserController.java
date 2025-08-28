package ru.practicum.shareit.server.user.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.server.user.dto.UserRequestDto;
import ru.practicum.shareit.server.user.dto.UserResponseDto;
import ru.practicum.shareit.server.user.service.UserService;


@RestController
@RequestMapping(path = "/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public UserResponseDto createUser(@RequestBody
                                      UserRequestDto newUser) {
        return userService.createUser(newUser);
    }

    @GetMapping("/{userId}")
    public UserResponseDto getUserById(@PathVariable long userId) {
        return userService.getUserById(userId);
    }


    @PatchMapping("/{userId}")
    public UserResponseDto updateUser(@RequestBody
                                      UserRequestDto userDataToUpdate,
                                      @PathVariable
                                      Long userId) {
        return userService.updateUser(userDataToUpdate, userId);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable
                           long userId) {
        userService.deleteUser(userId);
    }
}