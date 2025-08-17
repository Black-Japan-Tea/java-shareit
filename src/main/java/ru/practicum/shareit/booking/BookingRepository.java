package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus status, Sort sort);

    List<Booking> findByItemOwnerIdAndStatus(Long ownerId, BookingStatus status, Sort sort);

    @Query("SELECT b FROM Booking b " +
           "WHERE b.item.owner.id = :ownerId " +
           "AND b.start <= :now " +
           "AND b.end >= :now " +
           "ORDER BY b.start DESC")
    List<Booking> findCurrentBookingsByOwner(@Param("ownerId") Long ownerId,
                                             @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
           "WHERE b.item.id = :itemId " +
           "AND ((b.start BETWEEN :start AND :end) OR " +
           "(b.end BETWEEN :start AND :end) OR " +
           "(b.start <= :start AND b.end >= :end))")
    List<Booking> findOverlappingBookings(
            @Param("itemId") Long itemId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT b FROM Booking b " +
           "WHERE b.booker.id = :bookerId " +
           "AND b.start <= :now " +
           "AND b.end >= :now " +
           "ORDER BY b.start DESC")
    List<Booking> findCurrentBookingsByBooker(@Param("bookerId") Long bookerId,
                                              @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
           "JOIN FETCH b.item " +
           "JOIN FETCH b.booker " +
           "WHERE b.item.id IN :itemIds " +
           "AND b.status = 'APPROVED' " +
           "ORDER BY b.start ASC")
    List<Booking> findApprovedBookingsForItems(@Param("itemIds") List<Long> itemIds);

    List<Booking> findByItemId(Long itemId);

    List<Booking> findByBookerId(Long bookerId, Sort sort);

    List<Booking> findByItemOwnerId(Long ownerId, Sort sort);

    List<Booking> findByBookerIdAndEndBefore(Long bookerId, LocalDateTime end, Sort sort);

    List<Booking> findByItemOwnerIdAndEndBefore(Long ownerId, LocalDateTime end, Sort sort);

    List<Booking> findByBookerIdAndStartAfter(Long bookerId, LocalDateTime start, Sort sort);

    List<Booking> findByItemOwnerIdAndStartAfter(Long ownerId, LocalDateTime start, Sort sort);

    boolean existsByBookerIdAndItemIdAndEndBefore(Long userId, Long itemId, LocalDateTime now);
}