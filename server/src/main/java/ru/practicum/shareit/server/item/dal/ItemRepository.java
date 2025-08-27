package ru.practicum.shareit.server.item.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.server.item.model.Item;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface ItemRepository extends JpaRepository<Item, Long> {

    Optional<Item> getItemById(Long itemId);

    Collection<Item> findByOwner(Long ownerId);

    @Query("SELECT i FROM Item i WHERE " +
           "(LOWER(i.name) LIKE LOWER(CONCAT('%', :text, '%')) OR " +
           "LOWER(i.description) LIKE LOWER(CONCAT('%', :text, '%'))) " +
           "AND i.available = true")
    Collection<Item> searchItems(@Param("text") String text);

    Collection<Item> findAllByRequestIn(Collection<Long> requests);

    Collection<Item> findAllByRequestIn(Set<Long> collect);
}
