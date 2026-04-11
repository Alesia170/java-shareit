package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserClient userClient;

    @Test
    void shouldCreateUser() throws Exception {
        UserDtoRequest dto = new UserDtoRequest("Name", "user@email.com");

        when(userClient.saveUser(any(UserDtoRequest.class)))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(dto))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userClient).saveUser(any(UserDtoRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenEmailInvalid() throws Exception {
        UserDtoRequest dto = new UserDtoRequest("name", "wrong-email");

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(dto))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetUserById() throws Exception {
        when(userClient.getById(1L))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(get("/users/1"))
                .andExpect(status().isOk());

        verify(userClient).getById(1L);
    }

    @Test
    void shouldGetAllUsers() throws Exception {
        when(userClient.getAllUsers())
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(get("/users"))
                .andExpect(status().isOk());

        verify(userClient).getAllUsers();
    }

    @Test
    void shouldUpdateUser() throws Exception {
        UserDtoUpdate dto = new UserDtoUpdate("New Name", "new@email.com");

        when(userClient.updateUser(eq(1L), any(UserDtoUpdate.class)))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(dto))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userClient).updateUser(eq(1L), any(UserDtoUpdate.class));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        when(userClient.deleteUser(1L))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        verify(userClient).deleteUser(1L);
    }
}
