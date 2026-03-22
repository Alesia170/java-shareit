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
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentRequestDto;
import ru.practicum.shareit.item.comment.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.model.Item;
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
}
