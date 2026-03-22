package ru.practicum.shareit.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.user.dto.UserDtoRequest;
import ru.practicum.shareit.user.dto.UserDtoResponse;
import ru.practicum.shareit.user.dto.UserMapper;

import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceImplTest {

    private final EntityManager em;
    private final UserService userService;

    @Test
    void shouldSaveUserToDatabase() {
        UserDtoRequest request = new UserDtoRequest("Name", "some@email.com");

        UserDtoResponse response = userService.saveUser(request);

        em.flush();
        em.clear();

        TypedQuery<User> query = em.createQuery("select u from User u where u.email = :email", User.class);
        User savedUser = query.setParameter("email", request.getEmail()).getSingleResult();

        assertThat(response, notNullValue());
        assertThat(response.getId(), notNullValue());
        assertThat(response.getName(), equalTo(request.getName()));
        assertThat(response.getEmail(), equalTo(request.getEmail()));

        assertThat(savedUser.getId(), notNullValue());
        assertThat(savedUser.getName(), equalTo(request.getName()));
        assertThat(savedUser.getEmail(), equalTo(request.getEmail()));
    }

    @Test
    void shouldReturnAllUsers() {
        List<UserDtoRequest> sourceUsers = List.of(
                new UserDtoRequest("name", "name@email.com"),
                new UserDtoRequest("petr", "petr@email.com"),
                new UserDtoRequest("vasilii", "vasilii@email.com")
        );

        for (UserDtoRequest user : sourceUsers) {
            User entity = UserMapper.toUser(user);
            em.persist(entity);
        }

        em.flush();
        em.clear();

        List<UserDtoResponse> targetUsers = userService.getAllUsers();

        assertThat(targetUsers, hasSize(sourceUsers.size()));

        for (UserDtoRequest sourceUser : sourceUsers) {
            assertThat(targetUsers, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(sourceUser.getName())),
                    hasProperty("email", equalTo(sourceUser.getEmail()))
            )));
        }
    }

    @Test
    void shouldThrowWhenEmailAlreadyExists() {
        User firstUser = new User();
        firstUser.setName("Ivan");
        firstUser.setEmail("ivan@email.com");
        em.persist(firstUser);
        em.flush();
        em.clear();

        UserDtoRequest request = new UserDtoRequest("Petr", "ivan@email.com");

        assertThrows(DuplicatedDataException.class, () -> userService.saveUser(request));
    }
}