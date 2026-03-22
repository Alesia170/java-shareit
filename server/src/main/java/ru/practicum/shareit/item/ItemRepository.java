package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query(" select i " +
            "from Item i " +
            "left join fetch i.request " +
            "where i.available = true " +
            "and (lower(i.name) like lower(concat('%', :text, '%')) " +
            "or lower(i.description) like lower(concat('%', :text, '%')))")
    List<Item> findItemBySearch(@Param("text") String text);

    @Query("""
            select
                i.id as id,
                i.name as name,
                i.description as description,
                i.available as available,
                lb.id as lastBookingId,
                lb.booker.id as lastBookerId,
                lb.start as lastStart,
                lb.end as lastEnd,
                nb.id as nextBookingId,
                nb.booker.id as nextBookerId,
                nb.start as nextStart,
                nb.end as nextEnd
            from Item i
            left join Booking lb
                on lb.item.id = i.id
                and lb.status = :status
                and lb.start = (
                    select max(b.start)
                    from Booking b
                    where b.item.id = i.id
                      and b.status = :status
                      and b.start <= :now
                )
            left join Booking nb
                on nb.item.id = i.id
                and nb.status = :status
                and nb.start = (
                    select min(b.start)
                    from Booking b
                    where b.item.id = i.id
                      and b.status = :status
                      and b.start > :now
                )
            where i.owner.id = :userId
            """)
    List<ItemBookingProjection> findItemsWithBookings(@Param("userId") Long userId,
                                                      @Param("status") Status status,
                                                      @Param("now") LocalDateTime now);

    List<Item> findByRequestId(Long requestId);
}