package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RequestClientTest {

    private TestRequestClient client;

    @BeforeEach
    void setUp() {
        client = new TestRequestClient();
    }

    @Test
    void saveNewRequest_shouldCallPostWithCorrectArguments() {
        ItemRequestRequestDto dto = new ItemRequestRequestDto();
        ResponseEntity<Object> response = client.saveNewRequest(1L, dto);

        assertEquals(ResponseEntity.ok().build(), response);
        assertEquals("", client.path);
        assertEquals(1L, client.userId);
        assertSame(dto, client.body);
    }

    @Test
    void getOwnRequests_shouldCallGetWithEmptyPath() {
        ResponseEntity<Object> response = client.getOwnRequests(2L);

        assertEquals(ResponseEntity.ok().build(), response);
        assertEquals("", client.path);
        assertEquals(2L, client.userId);
        assertNull(client.parameters);
    }

    @Test
    void getRequestsCreatedByOtherUsers_shouldCallGetAllPath() {
        ResponseEntity<Object> response = client.getRequestsCreatedByOtherUsers(3L);

        assertEquals(ResponseEntity.ok().build(), response);
        assertEquals("/all", client.path);
        assertEquals(3L, client.userId);
        assertNull(client.parameters);
    }

    @Test
    void getRequestById_shouldCallGetWithRequestIdParameter() {
        ResponseEntity<Object> response = client.getRequestById(4L, 10L);

        assertEquals(ResponseEntity.ok().build(), response);
        assertEquals("/{requestId}", client.path);
        assertEquals(4L, client.userId);
        assertEquals(Map.of("requestId", 10L), client.parameters);
    }

    private static class TestRequestClient extends RequestClient {
        private String path;
        private Long userId;
        private Object body;
        private Map<String, Object> parameters;

        TestRequestClient() {
            super("http://localhost:9090", new RestTemplateBuilder());
        }

        @Override
        protected <T> ResponseEntity<Object> post(String path, long userId, T body) {
            this.path = path;
            this.userId = userId;
            this.body = body;
            this.parameters = null;
            return ResponseEntity.ok().build();
        }

        @Override
        protected ResponseEntity<Object> get(String path, long userId) {
            this.path = path;
            this.userId = userId;
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
    }
}
