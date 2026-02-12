package ru.practicum.shareit.user.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserMapper {
    public static UserDtoResponse toUserDto(User user) {
        return new UserDtoResponse(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    public static User toUser(UserDtoRequest userDtoRequest) {
        User user = new User();
        user.setName(userDtoRequest.getName());
        user.setEmail(userDtoRequest.getEmail());
        return user;
    }
}
