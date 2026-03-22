package ru.practicum.shareit.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDtoRequest;
import ru.practicum.shareit.user.dto.UserDtoResponse;
import ru.practicum.shareit.user.dto.UserDtoUpdate;
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

    @Test
    void shouldReturnUserById() {
        User user = new User();
        user.setName("Alesya");
        user.setEmail("alesya@email.com");
        em.persist(user);
        em.flush();
        em.clear();

        UserDtoResponse response = userService.getById(user.getId());

        assertThat(response, notNullValue());
        assertThat(response.getId(), equalTo(user.getId()));
        assertThat(response.getName(), equalTo("Alesya"));
        assertThat(response.getEmail(), equalTo("alesya@email.com"));
    }

    @Test
    void shouldThrowWhenUserByIdNotFound() {
        assertThrows(NotFoundException.class, () -> userService.getById(9999L));
    }

    @Test
    void shouldUpdateUserNameAndEmail() {
        User user = new User();
        user.setName("Old Name");
        user.setEmail("old@email.com");
        em.persist(user);
        em.flush();
        em.clear();

        UserDtoUpdate update = new UserDtoUpdate("New Name", "new@email.com");

        UserDtoResponse response = userService.updateUser(user.getId(), update);

        em.flush();
        em.clear();

        User updatedUser = em.find(User.class, user.getId());

        assertThat(response, notNullValue());
        assertThat(response.getId(), equalTo(user.getId()));
        assertThat(response.getName(), equalTo("New Name"));
        assertThat(response.getEmail(), equalTo("new@email.com"));

        assertThat(updatedUser.getName(), equalTo("New Name"));
        assertThat(updatedUser.getEmail(), equalTo("new@email.com"));
    }

    @Test
    void shouldUpdateOnlyUserName() {
        User user = new User();
        user.setName("Old Name");
        user.setEmail("old@email.com");
        em.persist(user);
        em.flush();
        em.clear();

        UserDtoUpdate update = new UserDtoUpdate("New Name", null);

        UserDtoResponse response = userService.updateUser(user.getId(), update);

        em.flush();
        em.clear();

        User updatedUser = em.find(User.class, user.getId());

        assertThat(response.getName(), equalTo("New Name"));
        assertThat(response.getEmail(), equalTo("old@email.com"));

        assertThat(updatedUser.getName(), equalTo("New Name"));
        assertThat(updatedUser.getEmail(), equalTo("old@email.com"));
    }

    @Test
    void shouldUpdateOnlyUserEmail() {
        User user = new User();
        user.setName("Old Name");
        user.setEmail("old@email.com");
        em.persist(user);
        em.flush();
        em.clear();

        UserDtoUpdate update = new UserDtoUpdate(null, "new@email.com");

        UserDtoResponse response = userService.updateUser(user.getId(), update);

        em.flush();
        em.clear();

        User updatedUser = em.find(User.class, user.getId());

        assertThat(response.getName(), equalTo("Old Name"));
        assertThat(response.getEmail(), equalTo("new@email.com"));

        assertThat(updatedUser.getName(), equalTo("Old Name"));
        assertThat(updatedUser.getEmail(), equalTo("new@email.com"));
    }

    @Test
    void shouldThrowWhenUpdatingUserWithDuplicateEmail() {
        User firstUser = new User();
        firstUser.setName("Ivan");
        firstUser.setEmail("ivan@email.com");
        em.persist(firstUser);

        User secondUser = new User();
        secondUser.setName("Petr");
        secondUser.setEmail("petr@email.com");
        em.persist(secondUser);

        em.flush();
        em.clear();

        UserDtoUpdate update = new UserDtoUpdate("Petr Updated", "ivan@email.com");

        assertThrows(DuplicatedDataException.class,
                () -> userService.updateUser(secondUser.getId(), update));
    }

    @Test
    void shouldThrowWhenUpdatingUnknownUser() {
        UserDtoUpdate update = new UserDtoUpdate("Name", "name@email.com");

        assertThrows(NotFoundException.class, () -> userService.updateUser(9999L, update));
    }

    @Test
    void shouldDeleteUser() {
        User user = new User();
        user.setName("Delete Me");
        user.setEmail("delete@email.com");
        em.persist(user);
        em.flush();
        em.clear();

        userService.deleteUser(user.getId());

        em.flush();
        em.clear();

        TypedQuery<User> query = em.createQuery("select u from User u where u.id = :id", User.class);
        query.setParameter("id", user.getId());

        assertThrows(NoResultException.class, query::getSingleResult);
    }
}