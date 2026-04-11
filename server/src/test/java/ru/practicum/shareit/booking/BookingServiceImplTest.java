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
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    void shouldThrowWhenBookerNotFound() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner-notfound@email.com");
        em.persist(owner);

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

        assertThrows(NotFoundException.class, () -> bookingService.saveNewBooking(9999L, request));
    }

    @Test
    void shouldThrowWhenItemNotFound() {
        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker-notfound@email.com");
        em.persist(booker);

        em.flush();
        em.clear();

        BookingDtoRequest request = new BookingDtoRequest();
        request.setItemId(9999L);
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));

        assertThrows(NotFoundException.class, () -> bookingService.saveNewBooking(booker.getId(), request));
    }

    @Test
    void shouldThrowWhenItemIsNotAvailable() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner-unavailable@email.com");
        em.persist(owner);

        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker-unavailable@email.com");
        em.persist(booker);

        Item item = new Item();
        item.setName("Drill");
        item.setDescription("Power drill");
        item.setAvailable(false);
        item.setOwner(owner);
        em.persist(item);

        em.flush();
        em.clear();

        BookingDtoRequest request = new BookingDtoRequest();
        request.setItemId(item.getId());
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));

        assertThrows(ValidationException.class, () -> bookingService.saveNewBooking(booker.getId(), request));
    }

    @Test
    void shouldThrowWhenOwnerBooksOwnItem() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner-self@email.com");
        em.persist(owner);

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

        assertThrows(ValidationException.class, () -> bookingService.saveNewBooking(owner.getId(), request));
    }

    @Test
    void shouldThrowWhenApproveByNotOwner() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner-approve@email.com");
        em.persist(owner);

        User otherUser = new User();
        otherUser.setName("Other");
        otherUser.setEmail("other-approve@email.com");
        em.persist(otherUser);

        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker-approve@email.com");
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

        assertThrows(ForbiddenException.class, () -> bookingService.approveBooking(otherUser.getId(), booking.getId(), true));
    }

    @Test
    void shouldRejectBooking() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner-reject@email.com");
        em.persist(owner);

        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker-reject@email.com");
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

        BookingDtoResponse response = bookingService.approveBooking(owner.getId(), booking.getId(), false);

        em.flush();
        em.clear();

        Booking updatedBooking = em.find(Booking.class, booking.getId());

        assertThat(response.getStatus(), equalTo(Status.REJECTED));
        assertThat(updatedBooking.getStatus(), equalTo(Status.REJECTED));
    }

    @Test
    void shouldReturnEmptyUserBookings() {
        User user = new User();
        user.setName("User");
        user.setEmail("user-empty-bookings@email.com");
        em.persist(user);

        em.flush();
        em.clear();

        List<BookingDtoResponse> result = bookingService.getUserBookings(user.getId(), BookingState.ALL);

        assertThat(result, empty());
    }

    @Test
    void shouldReturnEmptyOwnerBookings() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner-empty-bookings@email.com");
        em.persist(owner);

        em.flush();
        em.clear();

        List<BookingDtoResponse> result = bookingService.getOwnerBookings(owner.getId(), BookingState.ALL);

        assertThat(result, empty());
    }

    @Test
    void shouldThrowWhenBookingEndIsNotAfterStart() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner-date@email.com");
        em.persist(owner);

        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker-date@email.com");
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
        request.setStart(LocalDateTime.now().plusDays(2));
        request.setEnd(LocalDateTime.now().plusDays(1));

        assertThrows(ValidationException.class, () -> bookingService.saveNewBooking(booker.getId(), request));
    }

    @Test
    void shouldThrowWhenBookingIntersectsWithApprovedBooking() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner-intersection@email.com");
        em.persist(owner);

        User firstBooker = new User();
        firstBooker.setName("First");
        firstBooker.setEmail("first-intersection@email.com");
        em.persist(firstBooker);

        User secondBooker = new User();
        secondBooker.setName("Second");
        secondBooker.setEmail("second-intersection@email.com");
        em.persist(secondBooker);

        Item item = new Item();
        item.setName("Drill");
        item.setDescription("Power drill");
        item.setAvailable(true);
        item.setOwner(owner);
        em.persist(item);

        Booking existingBooking = new Booking();
        existingBooking.setItem(item);
        existingBooking.setBooker(firstBooker);
        existingBooking.setStart(LocalDateTime.now().plusDays(5));
        existingBooking.setEnd(LocalDateTime.now().plusDays(10));
        existingBooking.setStatus(Status.APPROVED);
        em.persist(existingBooking);

        em.flush();
        em.clear();

        BookingDtoRequest request = new BookingDtoRequest();
        request.setItemId(item.getId());
        request.setStart(LocalDateTime.now().plusDays(7));
        request.setEnd(LocalDateTime.now().plusDays(9));

        assertThrows(ValidationException.class, () -> bookingService.saveNewBooking(secondBooker.getId(), request));
    }

    @Test
    void shouldThrowWhenApprovingUnknownBooking() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner-approve-unknown@email.com");
        em.persist(owner);

        em.flush();
        em.clear();

        assertThrows(NotFoundException.class, () -> bookingService.approveBooking(owner.getId(), 9999L, true));
    }

    @Test
    void shouldThrowWhenApprovingBookingWithNonWaitingStatus() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner-approve-status@email.com");
        em.persist(owner);

        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker-approve-status@email.com");
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
        booking.setStatus(Status.APPROVED);
        em.persist(booking);

        em.flush();
        em.clear();

        assertThrows(ValidationException.class,
                () -> bookingService.approveBooking(owner.getId(), booking.getId(), true));
    }

    @Test
    void shouldReturnBookingByIdForOwner() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner-get-booking@email.com");
        em.persist(owner);

        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker-get-booking@email.com");
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

        BookingDtoResponse response = bookingService.getBookingById(owner.getId(), booking.getId());

        assertThat(response.getId(), equalTo(booking.getId()));
        assertThat(response.getItem().getId(), equalTo(item.getId()));
        assertThat(response.getBooker().getId(), equalTo(booker.getId()));
    }

    @Test
    void shouldReturnBookingByIdForBooker() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner-get-booking-booker@email.com");
        em.persist(owner);

        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker-get-booking-booker@email.com");
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

        BookingDtoResponse response = bookingService.getBookingById(booker.getId(), booking.getId());

        assertThat(response.getId(), equalTo(booking.getId()));
        assertThat(response.getBooker().getId(), equalTo(booker.getId()));
    }

    @Test
    void shouldThrowWhenGettingBookingByIdForUnauthorizedUser() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner-get-booking-denied@email.com");
        em.persist(owner);

        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker-get-booking-denied@email.com");
        em.persist(booker);

        User stranger = new User();
        stranger.setName("Stranger");
        stranger.setEmail("stranger-get-booking@email.com");
        em.persist(stranger);

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

        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(stranger.getId(), booking.getId()));
    }

    @Test
    void shouldThrowWhenGettingBookingByIdForUnknownUser() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner-get-booking-unknown-user@email.com");
        em.persist(owner);

        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker-get-booking-unknown-user@email.com");
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

        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(9999L, booking.getId()));
    }

    @Test
    void shouldThrowWhenGettingUnknownBookingById() {
        User user = new User();
        user.setName("User");
        user.setEmail("user-get-booking-unknown@email.com");
        em.persist(user);

        em.flush();
        em.clear();

        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(user.getId(), 9999L));
    }

    @Test
    void shouldReturnFutureUserBookings() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner-future@email.com");
        em.persist(owner);

        User user = new User();
        user.setName("User");
        user.setEmail("user-future@email.com");
        em.persist(user);

        Item item = new Item();
        item.setName("Drill");
        item.setDescription("Power drill");
        item.setAvailable(true);
        item.setOwner(owner);
        em.persist(item);

        Booking futureBooking = new Booking();
        futureBooking.setItem(item);
        futureBooking.setBooker(user);
        futureBooking.setStart(LocalDateTime.now().plusDays(3));
        futureBooking.setEnd(LocalDateTime.now().plusDays(4));
        futureBooking.setStatus(Status.WAITING);
        em.persist(futureBooking);

        Booking pastBooking = new Booking();
        pastBooking.setItem(item);
        pastBooking.setBooker(user);
        pastBooking.setStart(LocalDateTime.now().minusDays(4));
        pastBooking.setEnd(LocalDateTime.now().minusDays(3));
        pastBooking.setStatus(Status.APPROVED);
        em.persist(pastBooking);

        em.flush();
        em.clear();

        List<BookingDtoResponse> result = bookingService.getUserBookings(user.getId(), BookingState.FUTURE);

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), equalTo(futureBooking.getId()));
    }

    @Test
    void shouldReturnPastUserBookings() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner-past@email.com");
        em.persist(owner);

        User user = new User();
        user.setName("User");
        user.setEmail("user-past@email.com");
        em.persist(user);

        Item item = new Item();
        item.setName("Drill");
        item.setDescription("Power drill");
        item.setAvailable(true);
        item.setOwner(owner);
        em.persist(item);

        Booking pastBooking = new Booking();
        pastBooking.setItem(item);
        pastBooking.setBooker(user);
        pastBooking.setStart(LocalDateTime.now().minusDays(4));
        pastBooking.setEnd(LocalDateTime.now().minusDays(3));
        pastBooking.setStatus(Status.APPROVED);
        em.persist(pastBooking);

        Booking futureBooking = new Booking();
        futureBooking.setItem(item);
        futureBooking.setBooker(user);
        futureBooking.setStart(LocalDateTime.now().plusDays(3));
        futureBooking.setEnd(LocalDateTime.now().plusDays(4));
        futureBooking.setStatus(Status.WAITING);
        em.persist(futureBooking);

        em.flush();
        em.clear();

        List<BookingDtoResponse> result = bookingService.getUserBookings(user.getId(), BookingState.PAST);

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), equalTo(pastBooking.getId()));
    }

    @Test
    void shouldReturnCurrentUserBookings() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner-current@email.com");
        em.persist(owner);

        User user = new User();
        user.setName("User");
        user.setEmail("user-current@email.com");
        em.persist(user);

        Item item = new Item();
        item.setName("Drill");
        item.setDescription("Power drill");
        item.setAvailable(true);
        item.setOwner(owner);
        em.persist(item);

        Booking currentBooking = new Booking();
        currentBooking.setItem(item);
        currentBooking.setBooker(user);
        currentBooking.setStart(LocalDateTime.now().minusHours(1));
        currentBooking.setEnd(LocalDateTime.now().plusHours(1));
        currentBooking.setStatus(Status.APPROVED);
        em.persist(currentBooking);

        Booking futureBooking = new Booking();
        futureBooking.setItem(item);
        futureBooking.setBooker(user);
        futureBooking.setStart(LocalDateTime.now().plusDays(1));
        futureBooking.setEnd(LocalDateTime.now().plusDays(2));
        futureBooking.setStatus(Status.WAITING);
        em.persist(futureBooking);

        em.flush();
        em.clear();

        List<BookingDtoResponse> result = bookingService.getUserBookings(user.getId(), BookingState.CURRENT);

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), equalTo(currentBooking.getId()));
    }

    @Test
    void shouldReturnWaitingUserBookings() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner-waiting@email.com");
        em.persist(owner);

        User user = new User();
        user.setName("User");
        user.setEmail("user-waiting@email.com");
        em.persist(user);

        Item item = new Item();
        item.setName("Drill");
        item.setDescription("Power drill");
        item.setAvailable(true);
        item.setOwner(owner);
        em.persist(item);

        Booking waitingBooking = new Booking();
        waitingBooking.setItem(item);
        waitingBooking.setBooker(user);
        waitingBooking.setStart(LocalDateTime.now().plusDays(1));
        waitingBooking.setEnd(LocalDateTime.now().plusDays(2));
        waitingBooking.setStatus(Status.WAITING);
        em.persist(waitingBooking);

        Booking rejectedBooking = new Booking();
        rejectedBooking.setItem(item);
        rejectedBooking.setBooker(user);
        rejectedBooking.setStart(LocalDateTime.now().plusDays(3));
        rejectedBooking.setEnd(LocalDateTime.now().plusDays(4));
        rejectedBooking.setStatus(Status.REJECTED);
        em.persist(rejectedBooking);

        em.flush();
        em.clear();

        List<BookingDtoResponse> result = bookingService.getUserBookings(user.getId(), BookingState.WAITING);

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), equalTo(waitingBooking.getId()));
    }

    @Test
    void shouldReturnRejectedUserBookings() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner-rejected@email.com");
        em.persist(owner);

        User user = new User();
        user.setName("User");
        user.setEmail("user-rejected@email.com");
        em.persist(user);

        Item item = new Item();
        item.setName("Drill");
        item.setDescription("Power drill");
        item.setAvailable(true);
        item.setOwner(owner);
        em.persist(item);

        Booking rejectedBooking = new Booking();
        rejectedBooking.setItem(item);
        rejectedBooking.setBooker(user);
        rejectedBooking.setStart(LocalDateTime.now().plusDays(1));
        rejectedBooking.setEnd(LocalDateTime.now().plusDays(2));
        rejectedBooking.setStatus(Status.REJECTED);
        em.persist(rejectedBooking);

        Booking waitingBooking = new Booking();
        waitingBooking.setItem(item);
        waitingBooking.setBooker(user);
        waitingBooking.setStart(LocalDateTime.now().plusDays(3));
        waitingBooking.setEnd(LocalDateTime.now().plusDays(4));
        waitingBooking.setStatus(Status.WAITING);
        em.persist(waitingBooking);

        em.flush();
        em.clear();

        List<BookingDtoResponse> result = bookingService.getUserBookings(user.getId(), BookingState.REJECTED);

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), equalTo(rejectedBooking.getId()));
    }

    @Test
    void shouldThrowWhenGettingUserBookingsForUnknownUser() {
        assertThrows(NotFoundException.class, () -> bookingService.getUserBookings(9999L, BookingState.ALL));
    }

    @Test
    void shouldReturnWaitingOwnerBookings() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner-owner-waiting@email.com");
        em.persist(owner);

        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker-owner-waiting@email.com");
        em.persist(booker);

        Item item = new Item();
        item.setName("Drill");
        item.setDescription("Power drill");
        item.setAvailable(true);
        item.setOwner(owner);
        em.persist(item);

        Booking waitingBooking = new Booking();
        waitingBooking.setItem(item);
        waitingBooking.setBooker(booker);
        waitingBooking.setStart(LocalDateTime.now().plusDays(1));
        waitingBooking.setEnd(LocalDateTime.now().plusDays(2));
        waitingBooking.setStatus(Status.WAITING);
        em.persist(waitingBooking);

        Booking rejectedBooking = new Booking();
        rejectedBooking.setItem(item);
        rejectedBooking.setBooker(booker);
        rejectedBooking.setStart(LocalDateTime.now().plusDays(3));
        rejectedBooking.setEnd(LocalDateTime.now().plusDays(4));
        rejectedBooking.setStatus(Status.REJECTED);
        em.persist(rejectedBooking);

        em.flush();
        em.clear();

        List<BookingDtoResponse> result = bookingService.getOwnerBookings(owner.getId(), BookingState.WAITING);

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), equalTo(waitingBooking.getId()));
    }

    @Test
    void shouldReturnRejectedOwnerBookings() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner-owner-rejected@email.com");
        em.persist(owner);

        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker-owner-rejected@email.com");
        em.persist(booker);

        Item item = new Item();
        item.setName("Drill");
        item.setDescription("Power drill");
        item.setAvailable(true);
        item.setOwner(owner);
        em.persist(item);

        Booking rejectedBooking = new Booking();
        rejectedBooking.setItem(item);
        rejectedBooking.setBooker(booker);
        rejectedBooking.setStart(LocalDateTime.now().plusDays(1));
        rejectedBooking.setEnd(LocalDateTime.now().plusDays(2));
        rejectedBooking.setStatus(Status.REJECTED);
        em.persist(rejectedBooking);

        Booking waitingBooking = new Booking();
        waitingBooking.setItem(item);
        waitingBooking.setBooker(booker);
        waitingBooking.setStart(LocalDateTime.now().plusDays(3));
        waitingBooking.setEnd(LocalDateTime.now().plusDays(4));
        waitingBooking.setStatus(Status.WAITING);
        em.persist(waitingBooking);

        em.flush();
        em.clear();

        List<BookingDtoResponse> result = bookingService.getOwnerBookings(owner.getId(), BookingState.REJECTED);

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), equalTo(rejectedBooking.getId()));
    }

    @Test
    void shouldThrowWhenGettingOwnerBookingsForUnknownUser() {
        assertThrows(NotFoundException.class, () -> bookingService.getOwnerBookings(9999L, BookingState.ALL));
    }
}
