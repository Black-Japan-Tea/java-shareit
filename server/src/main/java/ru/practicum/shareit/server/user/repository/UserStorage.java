package ru.practicum.shareit.server.user.repository;

import ru.practicum.shareit.server.user.model.User;

import java.util.Optional;

public interface UserStorage {

    Optional<User> getUserById(Long userId);

    Optional<User> getUserByEmail(String email);

    User addUser(User newUser);

    User updateUser(User userToUpdate);

    void deleteUser(Long itemId);

}
