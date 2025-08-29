package ru.practicum.shareit.server.user.service;

import ru.practicum.shareit.server.user.dto.UserRequestDto;
import ru.practicum.shareit.server.user.dto.UserResponseDto;

public interface UserService {

    UserResponseDto createUser(UserRequestDto newUser);


    UserResponseDto getUserById(Long userId);


    UserResponseDto updateUser(UserRequestDto userDataToUpdate, Long userId);

    void deleteUser(Long userId);
}
