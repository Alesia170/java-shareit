package ru.practicum.shareit.user;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository {

    Collection<User> getAllUsers();

    Optional<User> getById(Long id);

    User saveUser(User user);

    void deleteUser(Long id);
}
