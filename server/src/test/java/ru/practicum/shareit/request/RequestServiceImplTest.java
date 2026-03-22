package ru.practicum.shareit.request;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class RequestServiceImplTest {

    private final EntityManager em;
    private final ItemRequestService itemRequestService;

    @Test
    void shouldSaveNewRequest() {
        User user = new User();
        user.setName("User");
        user.setEmail("user@email.com");
        em.persist(user);
        em.flush();

        ItemRequestRequestDto requestDto = new ItemRequestRequestDto();
        requestDto.setDescription("Need a drill");

        ItemRequestResponseDto response = itemRequestService.saveNewRequest(user.getId(), requestDto);

        em.flush();
        em.clear();

        TypedQuery<ItemRequest> query = em.createQuery(
                "select r from ItemRequest r where r.requestor.id = :userId and r.description = :description",
                ItemRequest.class);

        ItemRequest savedRequest = query
                .setParameter("userId", user.getId())
                .setParameter("description", requestDto.getDescription())
                .getSingleResult();

        assertThat(response, notNullValue());
        assertThat(response.getId(), notNullValue());
        assertThat(response.getDescription(), equalTo(requestDto.getDescription()));

        assertThat(savedRequest.getId(), notNullValue());
        assertThat(savedRequest.getDescription(), equalTo(requestDto.getDescription()));
        assertThat(savedRequest.getRequestor().getId(), equalTo(user.getId()));
    }

    @Test
    void shouldReturnOwnRequests() {
        User user1 = new User();
        user1.setName("User1");
        user1.setEmail("user1@email.com");
        em.persist(user1);

        User user2 = new User();
        user2.setName("User2");
        user2.setEmail("user2@email.com");
        em.persist(user2);

        ItemRequest request1 = new ItemRequest();
        request1.setDescription("Need a drill");
        request1.setRequestor(user1);
        request1.setCreated(LocalDateTime.now().minusHours(3));
        em.persist(request1);

        ItemRequest request2 = new ItemRequest();
        request2.setDescription("Need a ladder");
        request2.setRequestor(user1);
        request2.setCreated(LocalDateTime.now().minusHours(2));
        em.persist(request2);

        ItemRequest request3 = new ItemRequest();
        request3.setDescription("Need a saw");
        request3.setRequestor(user2);
        request3.setCreated(LocalDateTime.now().minusHours(1));
        em.persist(request3);

        em.flush();
        em.clear();

        List<ItemRequestResponseDto> result = itemRequestService.getOwnRequests(user1.getId());

        assertThat(result, hasSize(2));
        assertThat(result, hasItem(allOf(
                hasProperty("id", equalTo(request1.getId())),
                hasProperty("description", equalTo(request1.getDescription())))));
        assertThat(result, hasItem(allOf(
                hasProperty("id", equalTo(request2.getId())),
                hasProperty("description", equalTo(request2.getDescription())))));
        assertThat(result, not(hasItem(hasProperty("id", equalTo(request3.getId())))));
    }

    @Test
    void shouldReturnRequestsCreatedByOtherUsers() {
        User user1 = new User();
        user1.setName("User1");
        user1.setEmail("user1@email.com");
        em.persist(user1);

        User user2 = new User();
        user2.setName("User2");
        user2.setEmail("user2@email.com");
        em.persist(user2);

        User user3 = new User();
        user3.setName("User3");
        user3.setEmail("user3@email.com");
        em.persist(user3);

        ItemRequest request1 = new ItemRequest();
        request1.setDescription("Need a drill");
        request1.setRequestor(user1);
        request1.setCreated(LocalDateTime.now().minusHours(3));
        em.persist(request1);

        ItemRequest request2 = new ItemRequest();
        request2.setDescription("Need a ladder");
        request2.setRequestor(user2);
        request2.setCreated(LocalDateTime.now().minusHours(2));
        em.persist(request2);

        ItemRequest request3 = new ItemRequest();
        request3.setDescription("Need a saw");
        request3.setRequestor(user3);
        request3.setCreated(LocalDateTime.now().minusHours(1));
        em.persist(request3);

        em.flush();
        em.clear();

        List<ItemRequestResponseDto> result = itemRequestService.getRequestsCreatedByOtherUsers(user1.getId());

        assertThat(result, hasSize(2));
        assertThat(result, hasItem(hasProperty("id", equalTo(request2.getId()))));
        assertThat(result, hasItem(hasProperty("id", equalTo(request3.getId()))));
        assertThat(result, not(hasItem(hasProperty("id", equalTo(request1.getId())))));
    }

    @Test
    void shouldReturnRequestById() {
        User user = new User();
        user.setName("User");
        user.setEmail("user@email.com");
        em.persist(user);

        ItemRequest request = new ItemRequest();
        request.setDescription("Need a drill");
        request.setRequestor(user);
        request.setCreated(LocalDateTime.now().minusHours(1));
        em.persist(request);

        em.flush();
        em.clear();

        ItemRequestResponseDto response = itemRequestService.getRequestById(user.getId(), request.getId());

        assertThat(response, notNullValue());
        assertThat(response.getId(), equalTo(request.getId()));
        assertThat(response.getDescription(), equalTo(request.getDescription()));
    }
}