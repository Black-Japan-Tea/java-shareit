package ru.practicum.shareit.server.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.server.user.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> getUserById(Long userId);

    Optional<User> getUserByEmail(String email);

    List<User> findByIdIn(List<Long> userIds);
}
