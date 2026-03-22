package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId);

    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime now);

    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime now);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, Status status);

    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId);

    List<Booking> findByItemOwnerIdAndEndBeforeOrderByStartDesc(Long ownerId, LocalDateTime now);

    List<Booking> findByItemOwnerIdAndStartAfterOrderByStartDesc(Long ownerId, LocalDateTime now);

    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(Long ownerId, Status status);

    @Query("""
            select b
            from Booking b
            join fetch b.item
            join fetch b.booker
            where b.booker.id = :userId
                and b.start <= :now
                and b.end >= :now
            order by b.start desc
            """)
    List<Booking> findCurrentByBooker(@Param("userId") Long userId,
                                      @Param("now") LocalDateTime now);

    @Query("""
            select b
            from Booking b
            join fetch b.item
            join fetch b.booker
            where b.item.owner.id = :userId
                and b.start <= :now
                and b.end >= :now
            order by b.start desc
            """)
    List<Booking> findCurrentByOwner(@Param("userId") Long userId,
                                     @Param("now") LocalDateTime now);

    Optional<Booking> findFirstByItemIdAndStatusAndStartLessThanEqualOrderByStartDesc(
            Long itemId, Status status, LocalDateTime now
    );

    Optional<Booking> findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(
            Long itemId, Status status, LocalDateTime now
    );

    @Query("""
            select b
            from Booking b
            where b.item.id = :itemId
              and b.booker.id = :userId
              and b.status = :status
              and b.end < CURRENT_TIMESTAMP
            order by b.end desc
            """)
    List<Booking> findBookingForComment(@Param("itemId") Long itemId,
                                        @Param("userId") Long userId,
                                        @Param("status") Status status,
                                        Pageable pageable);

    boolean existsByItemIdAndStatusAndStartLessThanAndEndGreaterThan(Long itemId, Status status,
                                                                     LocalDateTime end, LocalDateTime start);
}
