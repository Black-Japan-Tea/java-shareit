package ru.practicum.shareit.server.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.server.booking.model.Booking;
import ru.practicum.shareit.server.booking.model.BookingStatus;
import ru.practicum.shareit.server.user.model.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Collection<Booking> findBookingsByBookerAndStatusOrderByStartDesc(User booker, BookingStatus status);

    Collection<Booking> findBookingsByBookerOrderByStartDesc(User booker);

    @Query("SELECT b FROM Booking b " +
           "WHERE b.booker = :user AND b.status = 'APPROVED' " +
           "AND b.start <= CURRENT_TIMESTAMP AND b.end >= CURRENT_TIMESTAMP " +
           "ORDER BY b.start DESC")
    Collection<Booking> findCurrentBookings(@Param("user") User user);

    @Query("SELECT b FROM Booking b " +
           "WHERE b.booker = :user AND b.status = 'APPROVED' " +
           "AND b.end < CURRENT_TIMESTAMP " +
           "ORDER BY b.start DESC")
    Collection<Booking> findPastBookings(@Param("user") User user);

    @Query("SELECT b FROM Booking b " +
           "WHERE b.booker = :user AND b.status = 'APPROVED' " +
           "AND b.start > CURRENT_TIMESTAMP " +
           "ORDER BY b.start DESC")
    Collection<Booking> findFutureBookings(@Param("user") User user);

    @Query("SELECT b FROM Booking b " +
           "WHERE b.item.owner = :ownerId AND b.status = 'APPROVED' " +
           "AND b.start <= CURRENT_TIMESTAMP AND b.end >= CURRENT_TIMESTAMP " +
           "ORDER BY b.start DESC")
    Collection<Booking> findCurrentBookingsByOwner(@Param("ownerId") Long ownerId);

    @Query("SELECT b FROM Booking b " +
           "WHERE b.item.owner = :ownerId AND b.status = 'APPROVED' " +
           "AND b.end < CURRENT_TIMESTAMP " +
           "ORDER BY b.start DESC")
    Collection<Booking> findPastBookingsByOwner(@Param("ownerId") Long ownerId);

    @Query("SELECT b FROM Booking b " +
           "WHERE b.item.owner = :ownerId AND b.status = 'APPROVED' " +
           "AND b.start > CURRENT_TIMESTAMP " +
           "ORDER BY b.start DESC")
    Collection<Booking> findFutureBookingsByOwner(@Param("ownerId") Long ownerId);

    @Query("SELECT b FROM Booking b " +
           "WHERE b.item.owner = :ownerId AND b.status = :status " +
           "ORDER BY b.start DESC")
    Collection<Booking> findBookingsByOwnerAndStatus(@Param("ownerId") Long ownerId, @Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b " +
           "WHERE b.item.owner = :ownerId " +
           "ORDER BY b.start DESC")
    Collection<Booking> findAllBookingsByOwner(@Param("ownerId") Long ownerId);

    Optional<Booking> findTopByItemIdAndStartAfterOrderByStartAsc(Long id, LocalDateTime now);

    Optional<Booking> findTopByItemIdAndEndBeforeOrderByEndDesc(Long id, LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.id IN :itemIds AND b.status = 'APPROVED' ORDER BY b.start DESC")
    List<Booking> findApprovedBookingsByItemIdsOrderByDesc(@Param("itemIds") List<Long> itemIds);
}