package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDtoRequest;
import ru.practicum.shareit.user.dto.UserDtoResponse;
import ru.practicum.shareit.user.dto.UserDtoUpdate;

import java.util.List;

public interface UserService {

    List<UserDtoResponse> getAllUsers();

    UserDtoResponse getById(Long id);

    UserDtoResponse saveUser(UserDtoRequest userDtoRequest);

    void deleteUser(Long id);

    UserDtoResponse updateUser(Long id, UserDtoUpdate userDtoUpdate);
}
