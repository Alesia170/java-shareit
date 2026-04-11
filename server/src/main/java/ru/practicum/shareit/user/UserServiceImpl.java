package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDtoRequest;
import ru.practicum.shareit.user.dto.UserDtoResponse;
import ru.practicum.shareit.user.dto.UserDtoUpdate;
import ru.practicum.shareit.user.dto.UserMapper;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<UserDtoResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public UserDtoResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));

        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDtoResponse saveUser(UserDtoRequest userDtoRequest) {
        User user = UserMapper.toUser(userDtoRequest);

        checkEmail(user.getEmail(), null);

        User savedUser = userRepository.save(user);

        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
        userRepository.deleteById(id);
    }

    @Override
    public UserDtoResponse updateUser(Long id, UserDtoUpdate userDtoUpdate) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));

        if (userDtoUpdate.getName() != null) {
            user.setName(userDtoUpdate.getName());
        }

        if (userDtoUpdate.getEmail() != null) {
            checkEmail(userDtoUpdate.getEmail(), user.getId());
            user.setEmail(userDtoUpdate.getEmail());
        }

        User savedUser = userRepository.save(user);

        return UserMapper.toUserDto(savedUser);
    }

    private void checkEmail(String email, Long userId) {
        boolean emailExists = userRepository.findAll()
                .stream()
                .anyMatch(existingUser -> Objects.equals(existingUser.getEmail(), email)
                                          && !Objects.equals(existingUser.getId(), userId));
        if (emailExists) {
            throw new DuplicatedDataException("Эта электронная почта уже используется");
        }
    }
}
