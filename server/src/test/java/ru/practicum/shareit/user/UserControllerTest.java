package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDtoRequest;
import ru.practicum.shareit.user.dto.UserDtoResponse;
import ru.practicum.shareit.user.dto.UserDtoUpdate;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
public class UserControllerTest {
    @Mock
    private UserService userService;

    @InjectMocks
    private UserController controller;

    private final ObjectMapper mapper = new ObjectMapper();

    private MockMvc mvc;

    private UserDtoRequest userDtoRequest;
    private UserDtoResponse userDtoResponse;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        userDtoRequest = new UserDtoRequest("name", "email@email.com");
        userDtoResponse = new UserDtoResponse(1L, "name", "email@email.com");
    }

    @Test
    void shouldSaveNewUser() throws Exception {
        when(userService.saveUser(any(UserDtoRequest.class)))
                .thenReturn(userDtoResponse);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDtoRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDtoResponse.getId().intValue())))
                .andExpect(jsonPath("$.name", is(userDtoResponse.getName())))
                .andExpect(jsonPath("$.email", is(userDtoResponse.getEmail())));
    }

    @Test
    void shouldGetUserById() throws Exception {
        when(userService.getById(anyLong()))
                .thenReturn(userDtoResponse);

        mvc.perform(get("/users/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDtoResponse.getId().intValue())))
                .andExpect(jsonPath("$.name", is(userDtoResponse.getName())))
                .andExpect(jsonPath("$.email", is(userDtoResponse.getEmail())));

        verify(userService).getById(1L);
    }

    @Test
    void shouldGetAllUsers() throws Exception {
        List<UserDtoResponse> users = List.of(userDtoResponse,
                new UserDtoResponse(2L, "name2", "email2@email.com"));

        when(userService.getAllUsers())
                .thenReturn(users);

        mvc.perform(get("/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(userDtoResponse.getId().intValue())))
                .andExpect(jsonPath("$[0].name", is(userDtoResponse.getName())))
                .andExpect(jsonPath("$[0].email", is(userDtoResponse.getEmail())))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("name2")))
                .andExpect(jsonPath("$[1].email", is("email2@email.com")));

        verify(userService).getAllUsers();
    }

    @Test
    void shouldDeleteUser() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        verify(userService).deleteUser(1L);
    }

    @Test
    void shouldUpdateUser() throws Exception {
        UserDtoUpdate update = new UserDtoUpdate("changed", "email@emai.com");
        UserDtoResponse updatedResponse = new UserDtoResponse(1L, "changed", "email@emai.com");

        when(userService.updateUser(eq(1L), any(UserDtoUpdate.class)))
                .thenReturn(updatedResponse);

        mvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(update))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updatedResponse.getId().intValue())))
                .andExpect(jsonPath("$.name", is(updatedResponse.getName())))
                .andExpect(jsonPath("$.email", is(updatedResponse.getEmail())));

        verify(userService).updateUser(eq(1L), any(UserDtoUpdate.class));
    }

    @Test
    void shouldReturnBadRequestWhenEmailAlreadyExists() throws Exception {
        UserDtoUpdate duplicateEmailRequest = new UserDtoUpdate("name", "email@email.com");

        when(userService.saveUser(any(UserDtoRequest.class)))
                .thenThrow(new ValidationException("Пользователь с таким email уже существует"));

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(duplicateEmailRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userService).saveUser(any(UserDtoRequest.class));
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        when(userService.getById(999L))
                .thenThrow(new NotFoundException("Пользователь с id=999 не найден"));

        mvc.perform(get("/users/999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(userService).getById(999L);
    }
}
