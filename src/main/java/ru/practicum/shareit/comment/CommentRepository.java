package ru.practicum.shareit.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.comment.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT c FROM Comment c " +
           "JOIN FETCH c.author " +
           "WHERE c.item.id IN :itemIds " +
           "ORDER BY c.created DESC")
    List<Comment> findByItemIdIn(@Param("itemIds") List<Long> itemIds);

    @Query("SELECT c FROM Comment c " +
           "JOIN FETCH c.author " +
           "WHERE c.item.id = :itemId " +
           "ORDER BY c.created DESC")
    List<Comment> findByItemId(@Param("itemId") Long itemId);
}