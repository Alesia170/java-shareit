package ru.practicum.shareit.booking;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceImplTest {

    private final EntityManager em;
    private final BookingService bookingService;

    @Test
    void shouldSaveNewBooking() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@email.com");
        em.persist(owner);

        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker@email.com");
        em.persist(booker);

        Item item = new Item();
        item.setName("Drill");
        item.setDescription("Power drill");
        item.setAvailable(true);
        item.setOwner(owner);
        em.persist(item);

        em.flush();
        em.clear();

        BookingDtoRequest request = new BookingDtoRequest();
        request.setItemId(item.getId());
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));

        BookingDtoResponse response = bookingService.saveNewBooking(booker.getId(), request);

        em.flush();
        em.clear();

        TypedQuery<Booking> query = em.createQuery(
                "select b from Booking b where b.booker.id = :bookerId and b.item.id = :itemId", Booking.class);
        Booking savedBooking = query
                .setParameter("bookerId", booker.getId())
                .setParameter("itemId", item.getId())
                .getSingleResult();

        assertThat(response, notNullValue());
        assertThat(response.getId(), notNullValue());
        assertThat(response.getItem().getId(), equalTo(item.getId()));
        assertThat(response.getBooker().getId(), equalTo(booker.getId()));
        assertThat(response.getStatus(), equalTo(Status.WAITING));

        assertThat(savedBooking.getId(), notNullValue());
        assertThat(savedBooking.getItem().getId(), equalTo(item.getId()));
        assertThat(savedBooking.getBooker().getId(), equalTo(booker.getId()));
        assertThat(savedBooking.getStatus(), equalTo(Status.WAITING));
    }

    @Test
    void shouldApproveBooking() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@email.com");
        em.persist(owner);

        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker@email.com");
        em.persist(booker);

        Item item = new Item();
        item.setName("Drill");
        item.setDescription("Power drill");
        item.setAvailable(true);
        item.setOwner(owner);
        em.persist(item);

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setStatus(Status.WAITING);
        em.persist(booking);

        em.flush();
        em.clear();

        BookingDtoResponse response = bookingService.approveBooking(owner.getId(), booking.getId(), true);

        em.flush();
        em.clear();

        Booking updatedBooking = em.find(Booking.class, booking.getId());

        assertThat(response, notNullValue());
        assertThat(response.getId(), equalTo(booking.getId()));
        assertThat(response.getStatus(), equalTo(Status.APPROVED));

        assertThat(updatedBooking.getStatus(), equalTo(Status.APPROVED));
    }

    @Test
    void shouldUserBookings() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@email.com");
        em.persist(owner);

        User user1 = new User();
        user1.setName("User1");
        user1.setEmail("user1@email.com");
        em.persist(user1);

        User user2 = new User();
        user2.setName("User2");
        user2.setEmail("user2@email.com");
        em.persist(user2);

        Item item1 = new Item();
        item1.setName("Drill");
        item1.setDescription("Power drill");
        item1.setAvailable(true);
        item1.setOwner(owner);
        em.persist(item1);

        Item item2 = new Item();
        item2.setName("Saw");
        item2.setDescription("Hand saw");
        item2.setAvailable(true);
        item2.setOwner(owner);
        em.persist(item2);

        Booking booking1 = new Booking();
        booking1.setItem(item1);
        booking1.setBooker(user1);
        booking1.setStart(LocalDateTime.now().plusDays(1));
        booking1.setEnd(LocalDateTime.now().plusDays(2));
        booking1.setStatus(Status.WAITING);
        em.persist(booking1);

        Booking booking2 = new Booking();
        booking2.setItem(item2);
        booking2.setBooker(user1);
        booking2.setStart(LocalDateTime.now().plusDays(3));
        booking2.setEnd(LocalDateTime.now().plusDays(4));
        booking2.setStatus(Status.APPROVED);
        em.persist(booking2);

        Booking booking3 = new Booking();
        booking3.setItem(item1);
        booking3.setBooker(user2);
        booking3.setStart(LocalDateTime.now().plusDays(5));
        booking3.setEnd(LocalDateTime.now().plusDays(6));
        booking3.setStatus(Status.WAITING);
        em.persist(booking3);

        em.flush();
        em.clear();

        List<BookingDtoResponse> result = bookingService.getUserBookings(user1.getId(), BookingState.ALL);

        assertThat(result, hasSize(2));
        assertThat(result, hasItem(allOf(
                hasProperty("id", equalTo(booking1.getId())),
                hasProperty("status", equalTo(Status.WAITING))
        )));
        assertThat(result, hasItem(allOf(
                hasProperty("id", equalTo(booking2.getId())),
                hasProperty("status", equalTo(Status.APPROVED))
        )));
    }

    @Test
    void shouldOwnerBookings() {
        User owner1 = new User();
        owner1.setName("Owner1");
        owner1.setEmail("owner1@email.com");
        em.persist(owner1);

        User owner2 = new User();
        owner2.setName("Owner2");
        owner2.setEmail("owner2@email.com");
        em.persist(owner2);

        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker@email.com");
        em.persist(booker);

        Item owner1Item = new Item();
        owner1Item.setName("Drill");
        owner1Item.setDescription("Power drill");
        owner1Item.setAvailable(true);
        owner1Item.setOwner(owner1);
        em.persist(owner1Item);

        Item owner2Item = new Item();
        owner2Item.setName("Saw");
        owner2Item.setDescription("Hand saw");
        owner2Item.setAvailable(true);
        owner2Item.setOwner(owner2);
        em.persist(owner2Item);

        Booking booking1 = new Booking();
        booking1.setItem(owner1Item);
        booking1.setBooker(booker);
        booking1.setStart(LocalDateTime.now().plusDays(1));
        booking1.setEnd(LocalDateTime.now().plusDays(2));
        booking1.setStatus(Status.WAITING);
        em.persist(booking1);

        Booking booking2 = new Booking();
        booking2.setItem(owner2Item);
        booking2.setBooker(booker);
        booking2.setStart(LocalDateTime.now().plusDays(3));
        booking2.setEnd(LocalDateTime.now().plusDays(4));
        booking2.setStatus(Status.APPROVED);
        em.persist(booking2);

        em.flush();
        em.clear();

        List<BookingDtoResponse> result = bookingService.getOwnerBookings(owner1.getId(), BookingState.ALL);

        assertThat(result, hasSize(1));
        assertThat(result, hasItem(allOf(
                hasProperty("id", equalTo(booking1.getId())),
                hasProperty("status", equalTo(Status.WAITING))
        )));
    }
}
