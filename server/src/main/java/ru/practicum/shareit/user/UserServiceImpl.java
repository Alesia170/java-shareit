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

        checkEmail(user);

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
            user.setEmail(userDtoUpdate.getEmail());
            checkEmail(user);
        }

        User savedUser = userRepository.save(user);

        return UserMapper.toUserDto(savedUser);
    }

    private void checkEmail(User user) {
        boolean emailExists = userRepository.findAll()
                .stream()
                .anyMatch(existingUser -> existingUser.getEmail().equals(user.getEmail())
                        && !Objects.equals(existingUser.getId(), user.getId()));
        if (emailExists) {
            throw new DuplicatedDataException("Эта электронная почта уже используется");
        }
    }
}
