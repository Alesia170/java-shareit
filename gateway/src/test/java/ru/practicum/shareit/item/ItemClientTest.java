package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.item.comment.CommentRequestDto;

import java.util.Map;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ItemClientTest {

    private RestTemplateBuilder builder;
    private RestTemplate restTemplate;
    private ItemClient itemClient;

    @BeforeEach
    void setUp() {
        builder = mock(RestTemplateBuilder.class);
        restTemplate = mock(RestTemplate.class);

        when(builder.uriTemplateHandler(any(DefaultUriBuilderFactory.class))).thenReturn(builder);
        when(builder.requestFactory(any(Supplier.class))).thenReturn(builder);
        when(builder.build()).thenReturn(restTemplate);

        itemClient = new ItemClient("http://localhost:9090", builder);
    }

    @Test
    void shouldSaveItem() {
        ItemDtoRequest request = new ItemDtoRequest();
        request.setName("Drill");
        request.setDescription("Power drill");
        request.setAvailable(true);

        ResponseEntity<Object> serverResponse = ResponseEntity.ok("saved");

        when(restTemplate.exchange(
                eq(""),
                eq(HttpMethod.POST),
                org.mockito.ArgumentMatchers.<HttpEntity<?>>any(),
                eq(Object.class)
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = itemClient.save(7L, request);

        assertThat(response.getBody(), equalTo("saved"));

        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq(""), eq(HttpMethod.POST), captor.capture(), eq(Object.class));

        assertThat(captor.getValue().getBody(), equalTo(request));
        assertThat(captor.getValue().getHeaders().getFirst("X-Sharer-User-Id"), equalTo("7"));
    }

    @Test
    void shouldUpdateItem() {
        ItemDtoUpdate request = new ItemDtoUpdate();
        request.setName("Updated");
        request.setDescription("Updated description");
        request.setAvailable(false);

        ResponseEntity<Object> serverResponse = ResponseEntity.ok("updated");

        when(restTemplate.exchange(
                eq("/{itemId}"),
                eq(HttpMethod.PATCH),
                org.mockito.ArgumentMatchers.<HttpEntity<?>>any(),
                eq(Object.class),
                anyMap()
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = itemClient.updateItem(5L, 11L, request);

        assertThat(response.getBody(), equalTo("updated"));

        ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);

        verify(restTemplate).exchange(
                eq("/{itemId}"),
                eq(HttpMethod.PATCH),
                entityCaptor.capture(),
                eq(Object.class),
                mapCaptor.capture()
        );

        assertThat(entityCaptor.getValue().getBody(), equalTo(request));
        assertThat(entityCaptor.getValue().getHeaders().getFirst("X-Sharer-User-Id"), equalTo("5"));
        assertThat(mapCaptor.getValue(), hasEntry("itemId", 11L));
    }

    @Test
    void shouldGetById() {
        ResponseEntity<Object> serverResponse = ResponseEntity.ok("item");

        when(restTemplate.exchange(
                eq("/{itemId}"),
                eq(HttpMethod.GET),
                org.mockito.ArgumentMatchers.<HttpEntity<?>>any(),
                eq(Object.class),
                anyMap()
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = itemClient.getById(3L, 15L);

        assertThat(response.getBody(), equalTo("item"));

        ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);

        verify(restTemplate).exchange(
                eq("/{itemId}"),
                eq(HttpMethod.GET),
                entityCaptor.capture(),
                eq(Object.class),
                mapCaptor.capture()
        );

        assertThat(entityCaptor.getValue().getHeaders().getFirst("X-Sharer-User-Id"), equalTo("3"));
        assertThat(mapCaptor.getValue(), hasEntry("itemId", 15L));
    }

    @Test
    void shouldGetAllItemsByUser() {
        ResponseEntity<Object> serverResponse = ResponseEntity.ok("all-items");

        when(restTemplate.exchange(
                eq(""),
                eq(HttpMethod.GET),
                org.mockito.ArgumentMatchers.<HttpEntity<?>>any(),
                eq(Object.class)
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = itemClient.getAllItemsByUser(9L);

        assertThat(response.getBody(), equalTo("all-items"));

        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq(""), eq(HttpMethod.GET), captor.capture(), eq(Object.class));

        assertThat(captor.getValue().getHeaders().getFirst("X-Sharer-User-Id"), equalTo("9"));
    }

    @Test
    void shouldGetItemBySearch() {
        ResponseEntity<Object> serverResponse = ResponseEntity.ok("search-result");

        when(restTemplate.exchange(
                eq("/search?text={text}"),
                eq(HttpMethod.GET),
                org.mockito.ArgumentMatchers.<HttpEntity<?>>any(),
                eq(Object.class),
                anyMap()
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = itemClient.getItemBySearch(4L, "drill");

        assertThat(response.getBody(), equalTo("search-result"));

        ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);

        verify(restTemplate).exchange(
                eq("/search?text={text}"),
                eq(HttpMethod.GET),
                entityCaptor.capture(),
                eq(Object.class),
                mapCaptor.capture()
        );

        assertThat(entityCaptor.getValue().getHeaders().getFirst("X-Sharer-User-Id"), equalTo("4"));
        assertThat(mapCaptor.getValue(), hasEntry("text", "drill"));
    }

    @Test
    void shouldAddComment() {
        CommentRequestDto request = new CommentRequestDto();
        request.setText("Very good item");

        ResponseEntity<Object> serverResponse = ResponseEntity.ok("comment-added");

        when(restTemplate.exchange(
                eq("/{itemId}/comment"),
                eq(HttpMethod.POST),
                org.mockito.ArgumentMatchers.<HttpEntity<?>>any(),
                eq(Object.class),
                anyMap()
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = itemClient.addComment(8L, 21L, request);

        assertThat(response.getBody(), equalTo("comment-added"));

        ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);

        verify(restTemplate).exchange(
                eq("/{itemId}/comment"),
                eq(HttpMethod.POST),
                entityCaptor.capture(),
                eq(Object.class),
                mapCaptor.capture()
        );

        assertThat(entityCaptor.getValue().getBody(), equalTo(request));
        assertThat(entityCaptor.getValue().getHeaders().getFirst("X-Sharer-User-Id"), equalTo("8"));
        assertThat(mapCaptor.getValue(), hasEntry("itemId", 21L));
    }
}
