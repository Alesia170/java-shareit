package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.getAllUsers()
                .stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public UserDto getById(Long id) {
        User user = userRepository.getById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));

        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto saveUser(UserDto userDto) {
        User user = UserMapper.toUser(userDto);

        checkEmail(user);

        User savedUser = userRepository.saveUser(user);

        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.getById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
        userRepository.deleteUser(id);
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {

        User user = userRepository.getById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));

        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }

        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
            checkEmail(user);
        }

        User savedUser = userRepository.saveUser(user);

        return UserMapper.toUserDto(savedUser);
    }

    private void checkEmail(User user) {
        boolean emailExists = userRepository.getAllUsers()
                .stream()
                .anyMatch(existingUser -> existingUser.getEmail().equals(user.getEmail())
                        && !Objects.equals(existingUser.getId(), user.getId()));
        if (emailExists) {
            throw new DuplicatedDataException("Эта электронная почта уже используется");
        }
    }
}
