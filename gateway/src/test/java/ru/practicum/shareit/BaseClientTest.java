package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BaseClientTest {

    private RestTemplate restTemplate;
    private TestBaseClient baseClient;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        baseClient = new TestBaseClient(restTemplate);
    }

    @Test
    void shouldGetWithoutUserIdAndWithoutParameters() {
        ResponseEntity<Object> serverResponse = ResponseEntity.ok("ok");

        when(restTemplate.exchange(
                eq("/test"),
                eq(HttpMethod.GET),
                org.mockito.ArgumentMatchers.<HttpEntity<?>>any(),
                eq(Object.class)
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = baseClient.callGet("/test");

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo("ok"));

        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq("/test"), eq(HttpMethod.GET), captor.capture(), eq(Object.class));

        HttpHeaders headers = captor.getValue().getHeaders();
        assertThat(headers.getContentType(), equalTo(MediaType.APPLICATION_JSON));
        assertThat(headers.getAccept(), hasItem(MediaType.APPLICATION_JSON));
        assertThat(headers.containsKey("X-Sharer-User-Id"), equalTo(false));
    }

    @Test
    void shouldGetWithUserId() {
        ResponseEntity<Object> serverResponse = ResponseEntity.ok("ok");

        when(restTemplate.exchange(
                eq("/test"),
                eq(HttpMethod.GET),
                org.mockito.ArgumentMatchers.<HttpEntity<?>>any(),
                eq(Object.class)
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = baseClient.callGet("/test", 7L);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo("ok"));

        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq("/test"), eq(HttpMethod.GET), captor.capture(), eq(Object.class));

        HttpHeaders headers = captor.getValue().getHeaders();
        assertThat(headers.getFirst("X-Sharer-User-Id"), equalTo("7"));
    }

    @Test
    void shouldGetWithParameters() {
        ResponseEntity<Object> serverResponse = ResponseEntity.ok("ok");

        when(restTemplate.exchange(
                eq("/{id}"),
                eq(HttpMethod.GET),
                org.mockito.ArgumentMatchers.<HttpEntity<?>>any(),
                eq(Object.class),
                anyMap()
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = baseClient.callGet("/{id}", 3L, Map.of("id", 10));

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo("ok"));

        ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);

        verify(restTemplate).exchange(eq("/{id}"), eq(HttpMethod.GET), entityCaptor.capture(), eq(Object.class), mapCaptor.capture());

        HttpHeaders headers = entityCaptor.getValue().getHeaders();
        assertThat(headers.getFirst("X-Sharer-User-Id"), equalTo("3"));
        assertThat(mapCaptor.getValue(), hasEntry("id", 10));
    }

    @Test
    void shouldPostWithoutUserId() {
        ResponseEntity<Object> serverResponse = ResponseEntity.status(HttpStatus.CREATED).body("created");

        when(restTemplate.exchange(
                eq("/test"),
                eq(HttpMethod.POST),
                org.mockito.ArgumentMatchers.<HttpEntity<?>>any(),
                eq(Object.class)
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = baseClient.callPost("/test", "body");

        assertThat(response.getStatusCode(), equalTo(HttpStatus.CREATED));
        assertThat(response.getBody(), equalTo("created"));

        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq("/test"), eq(HttpMethod.POST), captor.capture(), eq(Object.class));

        assertThat(captor.getValue().getBody(), equalTo("body"));
        assertThat(captor.getValue().getHeaders().containsKey("X-Sharer-User-Id"), equalTo(false));
    }

    @Test
    void shouldPostWithUserId() {
        ResponseEntity<Object> serverResponse = ResponseEntity.status(HttpStatus.CREATED).body("created");

        when(restTemplate.exchange(
                eq("/test"),
                eq(HttpMethod.POST),
                org.mockito.ArgumentMatchers.<HttpEntity<?>>any(),
                eq(Object.class)
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = baseClient.callPost("/test", 5L, "body");

        assertThat(response.getStatusCode(), equalTo(HttpStatus.CREATED));
        assertThat(response.getBody(), equalTo("created"));

        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq("/test"), eq(HttpMethod.POST), captor.capture(), eq(Object.class));

        assertThat(captor.getValue().getBody(), equalTo("body"));
        assertThat(captor.getValue().getHeaders().getFirst("X-Sharer-User-Id"), equalTo("5"));
    }

    @Test
    void shouldPostWithParameters() {
        ResponseEntity<Object> serverResponse = ResponseEntity.status(HttpStatus.CREATED).body("created");

        when(restTemplate.exchange(
                eq("/{id}"),
                eq(HttpMethod.POST),
                org.mockito.ArgumentMatchers.<HttpEntity<?>>any(),
                eq(Object.class),
                anyMap()
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = baseClient.callPost("/{id}", 8L, Map.of("id", 11), "body");

        assertThat(response.getStatusCode(), equalTo(HttpStatus.CREATED));
        assertThat(response.getBody(), equalTo("created"));

        ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);

        verify(restTemplate).exchange(eq("/{id}"), eq(HttpMethod.POST), entityCaptor.capture(), eq(Object.class), mapCaptor.capture());

        assertThat(entityCaptor.getValue().getBody(), equalTo("body"));
        assertThat(entityCaptor.getValue().getHeaders().getFirst("X-Sharer-User-Id"), equalTo("8"));
        assertThat(mapCaptor.getValue(), hasEntry("id", 11));
    }

    @Test
    void shouldPatch() {
        ResponseEntity<Object> serverResponse = ResponseEntity.ok("patched");

        when(restTemplate.exchange(
                eq("/{id}"),
                eq(HttpMethod.PATCH),
                org.mockito.ArgumentMatchers.<HttpEntity<?>>any(),
                eq(Object.class),
                anyMap()
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = baseClient.callPatch("/{id}", 9L, Map.of("id", 1), "patch-body");

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo("patched"));

        ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq("/{id}"), eq(HttpMethod.PATCH), entityCaptor.capture(), eq(Object.class), anyMap());

        assertThat(entityCaptor.getValue().getBody(), equalTo("patch-body"));
        assertThat(entityCaptor.getValue().getHeaders().getFirst("X-Sharer-User-Id"), equalTo("9"));
    }

    @Test
    void shouldDelete() {
        ResponseEntity<Object> serverResponse = ResponseEntity.noContent().build();

        when(restTemplate.exchange(
                eq("/{id}"),
                eq(HttpMethod.DELETE),
                org.mockito.ArgumentMatchers.<HttpEntity<?>>any(),
                eq(Object.class),
                anyMap()
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = baseClient.callDelete("/{id}", 10L, Map.of("id", 2));

        assertThat(response.getStatusCode(), equalTo(HttpStatus.NO_CONTENT));
        assertThat(response.hasBody(), equalTo(false));

        ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq("/{id}"), eq(HttpMethod.DELETE), entityCaptor.capture(), eq(Object.class), anyMap());

        assertThat(entityCaptor.getValue().getHeaders().getFirst("X-Sharer-User-Id"), equalTo("10"));
    }

    @Test
    void shouldReturnResponseFromException() {
        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                HttpHeaders.EMPTY,
                "error-body".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );

        when(restTemplate.exchange(
                eq("/test"),
                eq(HttpMethod.GET),
                org.mockito.ArgumentMatchers.<HttpEntity<?>>any(),
                eq(Object.class)
        )).thenThrow(exception);

        ResponseEntity<Object> response = baseClient.callGet("/test");

        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
        assertThat((byte[]) response.getBody(), equalTo("error-body".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void shouldReturnNon2xxResponseWithBody() {
        ResponseEntity<Object> serverResponse = ResponseEntity.status(HttpStatus.BAD_REQUEST).body("bad");

        when(restTemplate.exchange(
                eq("/test"),
                eq(HttpMethod.GET),
                org.mockito.ArgumentMatchers.<HttpEntity<?>>any(),
                eq(Object.class)
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = baseClient.callGet("/test");

        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
        assertThat(response.getBody(), equalTo("bad"));
    }

    @Test
    void shouldReturnNon2xxResponseWithoutBody() {
        ResponseEntity<Object> serverResponse = ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        when(restTemplate.exchange(
                eq("/test"),
                eq(HttpMethod.GET),
                org.mockito.ArgumentMatchers.<HttpEntity<?>>any(),
                eq(Object.class)
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = baseClient.callGet("/test");

        assertThat(response.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
        assertThat(response.hasBody(), equalTo(false));
    }

    private static class TestBaseClient extends BaseClient {

        TestBaseClient(RestTemplate rest) {
            super(rest);
        }

        ResponseEntity<Object> callGet(String path) {
            return get(path);
        }

        ResponseEntity<Object> callGet(String path, long userId) {
            return get(path, userId);
        }

        ResponseEntity<Object> callGet(String path, Long userId, Map<String, Object> parameters) {
            return get(path, userId, parameters);
        }

        <T> ResponseEntity<Object> callPost(String path, T body) {
            return post(path, body);
        }

        <T> ResponseEntity<Object> callPost(String path, long userId, T body) {
            return post(path, userId, body);
        }

        <T> ResponseEntity<Object> callPost(String path, Long userId, Map<String, Object> parameters, T body) {
            return post(path, userId, parameters, body);
        }

        <T> ResponseEntity<Object> callPatch(String path, Long userId, Map<String, Object> parameters, T body) {
            return patch(path, userId, parameters, body);
        }

        ResponseEntity<Object> callDelete(String path, Long userId, Map<String, Object> parameters) {
            return delete(path, userId, parameters);
        }
    }
}
