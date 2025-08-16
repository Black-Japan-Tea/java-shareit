package ru.practicum.shareit.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.comment.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByItemId(Long itemId);

    @Query("SELECT c FROM Comment c " +
           "WHERE c.item.owner.id = :ownerId " +
           "ORDER BY c.created DESC")
    List<Comment> findAllByItemOwner(@Param("ownerId") Long ownerId);
}