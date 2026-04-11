package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UserClientTest {

    private TestUserClient client;

    @BeforeEach
    void setUp() {
        client = new TestUserClient();
    }

    @Test
    void getAllUsers_shouldCallGetWithEmptyPath() {
        ResponseEntity<Object> response = client.getAllUsers();

        assertEquals(ResponseEntity.ok().build(), response);
        assertEquals("", client.path);
        assertNull(client.userId);
        assertNull(client.parameters);
        assertNull(client.body);
    }

    @Test
    void getById_shouldCallGetWithUserIdParameter() {
        ResponseEntity<Object> response = client.getById(1L);

        assertEquals(ResponseEntity.ok().build(), response);
        assertEquals("/{userId}", client.path);
        assertNull(client.userId);
        assertEquals(Map.of("userId", 1L), client.parameters);
        assertNull(client.body);
    }

    @Test
    void saveUser_shouldCallPostWithCorrectBody() {
        UserDtoRequest dto = new UserDtoRequest();
        ResponseEntity<Object> response = client.saveUser(dto);

        assertEquals(ResponseEntity.ok().build(), response);
        assertEquals("", client.path);
        assertNull(client.userId);
        assertSame(dto, client.body);
        assertNull(client.parameters);
    }

    @Test
    void deleteUser_shouldCallDeleteWithUserIdParameter() {
        ResponseEntity<Object> response = client.deleteUser(2L);

        assertEquals(ResponseEntity.ok().build(), response);
        assertEquals("/{userId}", client.path);
        assertNull(client.userId);
        assertEquals(Map.of("userId", 2L), client.parameters);
        assertNull(client.body);
    }

    @Test
    void updateUser_shouldCallPatchWithCorrectArguments() {
        UserDtoUpdate dto = new UserDtoUpdate();
        ResponseEntity<Object> response = client.updateUser(3L, dto);

        assertEquals(ResponseEntity.ok().build(), response);
        assertEquals("/{userId}", client.path);
        assertNull(client.userId);
        assertEquals(Map.of("userId", 3L), client.parameters);
        assertSame(dto, client.body);
    }

    private static class TestUserClient extends UserClient {
        private String path;
        private Long userId;
        private Object body;
        private Map<String, Object> parameters;

        TestUserClient() {
            super("http://localhost:9090", new RestTemplateBuilder());
        }

        @Override
        protected ResponseEntity<Object> get(String path) {
            this.path = path;
            this.userId = null;
            this.parameters = null;
            this.body = null;
            return ResponseEntity.ok().build();
        }

        @Override
        protected ResponseEntity<Object> get(String path, Long userId, Map<String, Object> parameters) {
            this.path = path;
            this.userId = userId;
            this.parameters = parameters;
            this.body = null;
            return ResponseEntity.ok().build();
        }

        @Override
        protected <T> ResponseEntity<Object> post(String path, T body) {
            this.path = path;
            this.userId = null;
            this.parameters = null;
            this.body = body;
            return ResponseEntity.ok().build();
        }

        @Override
        protected ResponseEntity<Object> delete(String path, Long userId, Map<String, Object> parameters) {
            this.path = path;
            this.userId = userId;
            this.parameters = parameters;
            this.body = null;
            return ResponseEntity.ok().build();
        }

        @Override
        protected <T> ResponseEntity<Object> patch(String path, Long userId, Map<String, Object> parameters, T body) {
            this.path = path;
            this.userId = userId;
            this.parameters = parameters;
            this.body = body;
            return ResponseEntity.ok().build();
        }
    }
}
