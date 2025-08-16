package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    List<User> getAllUsers();

    Optional<User> getUserById(Long userId);

    User createUser(User user);

    User updateUser(User user);

    void deleteUser(Long userId);

    boolean existsByEmail(String email);
}