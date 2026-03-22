package ru.practicum.shareit.item;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentRequestDto;
import ru.practicum.shareit.item.comment.CommentResponseDto;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceImplTest {

    private final EntityManager em;
    private final ItemService itemService;

    @Test
    void shouldReturnAllItemsByUser() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@email.com");
        em.persist(owner);

        User anotherUser = new User();
        anotherUser.setName("Another");
        anotherUser.setEmail("another@email.com");
        em.persist(anotherUser);

        Item firstItem = new Item();
        firstItem.setName("Drill");
        firstItem.setDescription("Power drill");
        firstItem.setAvailable(true);
        firstItem.setOwner(owner);
        em.persist(firstItem);

        Item secondItem = new Item();
        secondItem.setName("Hammer");
        secondItem.setDescription("Steel hammer");
        secondItem.setAvailable(true);
        secondItem.setOwner(owner);
        em.persist(secondItem);

        Item otherItem = new Item();
        otherItem.setName("Saw");
        otherItem.setDescription("Hand saw");
        otherItem.setAvailable(true);
        otherItem.setOwner(anotherUser);
        em.persist(otherItem);

        em.flush();
        em.clear();

        List<ItemBookingDto> result = itemService.getAllItemsByUser(owner.getId());

        assertThat(result, hasSize(2));
        assertThat(result, hasItem(allOf(
                hasProperty("name", equalTo("Drill")),
                hasProperty("description", equalTo("Power drill")))));
        assertThat(result, hasItem(allOf(
                hasProperty("name", equalTo("Hammer")),
                hasProperty("description", equalTo("Steel hammer")))));
    }

    @Test
    void shouldAddCommentWhenUserHadApprovedFinishedBooking() {
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
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        booking.setStatus(Status.APPROVED);
        em.persist(booking);

        em.flush();
        em.clear();

        CommentRequestDto request = new CommentRequestDto();
        request.setText("Very good item");

        CommentResponseDto response = itemService.addComment(booker.getId(), item.getId(), request);

        em.flush();
        em.clear();

        TypedQuery<Comment> query = em.createQuery(
                "select c from Comment c where c.item.id = :itemId and c.author.id = :authorId", Comment.class);
        Comment savedComment = query
                .setParameter("itemId", item.getId())
                .setParameter("authorId", booker.getId())
                .getSingleResult();

        assertThat(response, notNullValue());
        assertThat(response.getText(), equalTo("Very good item"));

        assertThat(savedComment.getId(), notNullValue());
        assertThat(savedComment.getText(), equalTo("Very good item"));
        assertThat(savedComment.getAuthor().getId(), equalTo(booker.getId()));
        assertThat(savedComment.getItem().getId(), equalTo(item.getId()));
    }

    @Test
    void shouldThrowWhenUserDidNotHaveFinishedBooking() {
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

        CommentRequestDto request = new CommentRequestDto();
        request.setText("Very good item");

        assertThrows(ValidationException.class, () -> itemService.addComment(booker.getId(), item.getId(), request));
    }

    @Test
    void shouldReturnEmptyItemsWhenUserHasNoItems() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner-empty@email.com");
        em.persist(owner);

        em.flush();
        em.clear();

        List<ItemBookingDto> result = itemService.getAllItemsByUser(owner.getId());

        assertThat(result, empty());
    }

    @Test
    void shouldThrowWhenAddingCommentForUnknownUser() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner2@email.com");
        em.persist(owner);

        Item item = new Item();
        item.setName("Drill");
        item.setDescription("Power drill");
        item.setAvailable(true);
        item.setOwner(owner);
        em.persist(item);

        em.flush();
        em.clear();

        CommentRequestDto request = new CommentRequestDto();
        request.setText("Very good item");

        assertThrows(RuntimeException.class,
                () -> itemService.addComment(9999L, item.getId(), request));
    }

    @Test
    void shouldThrowWhenAddingCommentForUnknownItem() {
        User user = new User();
        user.setName("User");
        user.setEmail("user@email.com");
        em.persist(user);

        em.flush();
        em.clear();

        CommentRequestDto request = new CommentRequestDto();
        request.setText("Very good item");

        assertThrows(RuntimeException.class,
                () -> itemService.addComment(user.getId(), 9999L, request));
    }

    @Test
    void shouldThrowWhenBookingNotApproved() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner3@email.com");
        em.persist(owner);

        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker3@email.com");
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
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        booking.setStatus(Status.WAITING);
        em.persist(booking);

        em.flush();
        em.clear();

        CommentRequestDto request = new CommentRequestDto();
        request.setText("Very good item");

        assertThrows(ValidationException.class,
                () -> itemService.addComment(booker.getId(), item.getId(), request));
    }

    @Test
    void shouldThrowWhenBookingNotFinishedYet() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner4@email.com");
        em.persist(owner);

        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker4@email.com");
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
        booking.setStart(LocalDateTime.now().minusHours(2));
        booking.setEnd(LocalDateTime.now().plusHours(2));
        booking.setStatus(Status.APPROVED);
        em.persist(booking);

        em.flush();
        em.clear();

        CommentRequestDto request = new CommentRequestDto();
        request.setText("Very good item");

        assertThrows(ValidationException.class,
                () -> itemService.addComment(booker.getId(), item.getId(), request));
    }

    @Test
    void shouldSaveCommentWithCorrectAuthorAndItem() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner5@email.com");
        em.persist(owner);

        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker5@email.com");
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
        booking.setStart(LocalDateTime.now().minusDays(3));
        booking.setEnd(LocalDateTime.now().minusDays(2));
        booking.setStatus(Status.APPROVED);
        em.persist(booking);

        em.flush();
        em.clear();

        CommentRequestDto request = new CommentRequestDto();
        request.setText("Excellent");

        CommentResponseDto response = itemService.addComment(booker.getId(), item.getId(), request);

        assertThat(response.getAuthorName(), equalTo("Booker"));
        assertThat(response.getText(), equalTo("Excellent"));
        assertThat(response.getCreated(), notNullValue());
    }

    @Test
    void shouldSaveItemWithoutRequest() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner-save@email.com");
        em.persist(owner);

        em.flush();
        em.clear();

        ItemDtoRequest request = new ItemDtoRequest();
        request.setName("Drill");
        request.setDescription("Power drill");
        request.setAvailable(true);

        ItemDtoResponse response = itemService.save(owner.getId(), request);

        assertThat(response.getId(), notNullValue());
        assertThat(response.getName(), equalTo("Drill"));
        assertThat(response.getDescription(), equalTo("Power drill"));
        assertThat(response.getAvailable(), equalTo(true));
        assertThat(response.getRequestId(), nullValue());
    }

    @Test
    void shouldSaveItemWithRequest() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner-save-request@email.com");
        em.persist(owner);

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription("Need a drill");
        itemRequest.setRequestor(owner);
        itemRequest.setCreated(LocalDateTime.now());
        em.persist(itemRequest);

        em.flush();
        em.clear();

        ItemDtoRequest request = new ItemDtoRequest();
        request.setName("Drill");
        request.setDescription("Power drill");
        request.setAvailable(true);
        request.setRequestId(itemRequest.getId());

        ItemDtoResponse response = itemService.save(owner.getId(), request);

        assertThat(response.getId(), notNullValue());
        assertThat(response.getName(), equalTo("Drill"));
        assertThat(response.getRequestId(), equalTo(itemRequest.getId()));
    }

    @Test
    void shouldThrowWhenSavingItemForUnknownUser() {
        ItemDtoRequest request = new ItemDtoRequest();
        request.setName("Drill");
        request.setDescription("Power drill");
        request.setAvailable(true);

        assertThrows(NotFoundException.class, () -> itemService.save(9999L, request));
    }

    @Test
    void shouldThrowWhenSavingItemWithUnknownRequest() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner-save-unknown-request@email.com");
        em.persist(owner);

        em.flush();
        em.clear();

        ItemDtoRequest request = new ItemDtoRequest();
        request.setName("Drill");
        request.setDescription("Power drill");
        request.setAvailable(true);
        request.setRequestId(9999L);

        assertThrows(NotFoundException.class, () -> itemService.save(owner.getId(), request));
    }

    @Test
    void shouldUpdateItem() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner-update@email.com");
        em.persist(owner);

        Item item = new Item();
        item.setName("Old name");
        item.setDescription("Old description");
        item.setAvailable(true);
        item.setOwner(owner);
        em.persist(item);

        em.flush();
        em.clear();

        ItemDtoUpdate update = new ItemDtoUpdate();
        update.setName("New name");
        update.setDescription("New description");
        update.setAvailable(false);

        ItemDtoResponse response = itemService.updateItem(owner.getId(), item.getId(), update);

        assertThat(response.getId(), equalTo(item.getId()));
        assertThat(response.getName(), equalTo("New name"));
        assertThat(response.getDescription(), equalTo("New description"));
        assertThat(response.getAvailable(), equalTo(false));
    }

    @Test
    void shouldUpdateOnlyNameWhenOtherFieldsAreNull() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner-update-partial@email.com");
        em.persist(owner);

        Item item = new Item();
        item.setName("Old name");
        item.setDescription("Old description");
        item.setAvailable(true);
        item.setOwner(owner);
        em.persist(item);

        em.flush();
        em.clear();

        ItemDtoUpdate update = new ItemDtoUpdate();
        update.setName("Only new name");

        ItemDtoResponse response = itemService.updateItem(owner.getId(), item.getId(), update);

        assertThat(response.getName(), equalTo("Only new name"));
        assertThat(response.getDescription(), equalTo("Old description"));
        assertThat(response.getAvailable(), equalTo(true));
    }

    @Test
    void shouldThrowWhenUpdatingItemForUnknownUser() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner-update-unknown-user@email.com");
        em.persist(owner);

        Item item = new Item();
        item.setName("Drill");
        item.setDescription("Power drill");
        item.setAvailable(true);
        item.setOwner(owner);
        em.persist(item);

        em.flush();
        em.clear();

        ItemDtoUpdate update = new ItemDtoUpdate();
        update.setName("Updated");

        assertThrows(NotFoundException.class, () -> itemService.updateItem(9999L, item.getId(), update));
    }

    @Test
    void shouldThrowWhenUpdatingUnknownItem() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner-update-unknown-item@email.com");
        em.persist(owner);

        em.flush();
        em.clear();

        ItemDtoUpdate update = new ItemDtoUpdate();
        update.setName("Updated");

        assertThrows(NotFoundException.class, () -> itemService.updateItem(owner.getId(), 9999L, update));
    }

    @Test
    void shouldThrowWhenUpdatingItemByNotOwner() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner-update-owner@email.com");
        em.persist(owner);

        User anotherUser = new User();
        anotherUser.setName("Another");
        anotherUser.setEmail("another-update@email.com");
        em.persist(anotherUser);

        Item item = new Item();
        item.setName("Drill");
        item.setDescription("Power drill");
        item.setAvailable(true);
        item.setOwner(owner);
        em.persist(item);

        em.flush();
        em.clear();

        ItemDtoUpdate update = new ItemDtoUpdate();
        update.setName("Updated by stranger");

        assertThrows(ForbiddenException.class, () -> itemService.updateItem(anotherUser.getId(), item.getId(), update));
    }

    @Test
    void shouldReturnItemWithBookingsForOwner() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner-getbyid@email.com");
        em.persist(owner);

        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker-getbyid@email.com");
        em.persist(booker);

        Item item = new Item();
        item.setName("Drill");
        item.setDescription("Power drill");
        item.setAvailable(true);
        item.setOwner(owner);
        em.persist(item);

        Booking lastBooking = new Booking();
        lastBooking.setItem(item);
        lastBooking.setBooker(booker);
        lastBooking.setStart(LocalDateTime.now().minusDays(3));
        lastBooking.setEnd(LocalDateTime.now().minusDays(2));
        lastBooking.setStatus(Status.APPROVED);
        em.persist(lastBooking);

        Booking nextBooking = new Booking();
        nextBooking.setItem(item);
        nextBooking.setBooker(booker);
        nextBooking.setStart(LocalDateTime.now().plusDays(1));
        nextBooking.setEnd(LocalDateTime.now().plusDays(2));
        nextBooking.setStatus(Status.APPROVED);
        em.persist(nextBooking);

        em.flush();
        em.clear();

        ItemOwnerDto response = itemService.getById(owner.getId(), item.getId());

        assertThat(response.getId(), equalTo(item.getId()));
        assertThat(response.getLastBooking(), notNullValue());
        assertThat(response.getNextBooking(), notNullValue());
        assertThat(response.getLastBooking().getId(), equalTo(lastBooking.getId()));
        assertThat(response.getNextBooking().getId(), equalTo(nextBooking.getId()));
    }

    @Test
    void shouldReturnItemWithoutBookingsForNotOwner() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner-getbyid-not-owner@email.com");
        em.persist(owner);

        User anotherUser = new User();
        anotherUser.setName("Another");
        anotherUser.setEmail("another-getbyid@email.com");
        em.persist(anotherUser);

        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker-getbyid-not-owner@email.com");
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
        booking.setStart(LocalDateTime.now().minusDays(3));
        booking.setEnd(LocalDateTime.now().minusDays(2));
        booking.setStatus(Status.APPROVED);
        em.persist(booking);

        em.flush();
        em.clear();

        ItemOwnerDto response = itemService.getById(anotherUser.getId(), item.getId());

        assertThat(response.getId(), equalTo(item.getId()));
        assertThat(response.getLastBooking(), nullValue());
        assertThat(response.getNextBooking(), nullValue());
    }

    @Test
    void shouldThrowWhenGettingUnknownItemById() {
        User user = new User();
        user.setName("User");
        user.setEmail("user-getbyid-unknown-item@email.com");
        em.persist(user);

        em.flush();
        em.clear();

        assertThrows(NotFoundException.class, () -> itemService.getById(user.getId(), 9999L));
    }

    @Test
    void shouldThrowWhenGettingItemByUnknownUser() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner-getbyid-unknown-user@email.com");
        em.persist(owner);

        Item item = new Item();
        item.setName("Drill");
        item.setDescription("Power drill");
        item.setAvailable(true);
        item.setOwner(owner);
        em.persist(item);

        em.flush();
        em.clear();

        assertThrows(NotFoundException.class, () -> itemService.getById(9999L, item.getId()));
    }

    @Test
    void shouldReturnEmptyListWhenSearchTextIsBlank() {
        List<ItemDtoResponse> result = itemService.getItemBySearch("   ");

        assertThat(result, empty());
    }

    @Test
    void shouldReturnEmptyListWhenSearchTextIsNull() {
        List<ItemDtoResponse> result = itemService.getItemBySearch(null);

        assertThat(result, empty());
    }

    @Test
    void shouldReturnItemsBySearch() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner-search@email.com");
        em.persist(owner);

        Item matchingItem = new Item();
        matchingItem.setName("Drill");
        matchingItem.setDescription("Power drill for home");
        matchingItem.setAvailable(true);
        matchingItem.setOwner(owner);
        em.persist(matchingItem);

        Item anotherItem = new Item();
        anotherItem.setName("Hammer");
        anotherItem.setDescription("Steel hammer");
        anotherItem.setAvailable(true);
        anotherItem.setOwner(owner);
        em.persist(anotherItem);

        em.flush();
        em.clear();

        List<ItemDtoResponse> result = itemService.getItemBySearch("drill");

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getName(), equalTo("Drill"));
    }
}
