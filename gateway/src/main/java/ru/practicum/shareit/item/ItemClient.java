package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.BaseClient;
import ru.practicum.shareit.item.comment.CommentRequestDto;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> save(Long userId, ItemDtoRequest itemDtoRequest) {
        return post("", userId, itemDtoRequest);
    }


    public ResponseEntity<Object> updateItem(Long userId, Long itemId, ItemDtoUpdate itemDtoUpdate) {
        return patch("/{itemId}", userId, Map.of("itemId", itemId), itemDtoUpdate);
    }

    public ResponseEntity<Object> getById(Long userId, Long itemId) {
        return get("/{itemId}", userId, Map.of("itemId", itemId));
    }

    public ResponseEntity<Object> getAllItemsByUser(Long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> getItemBySearch(Long userId, String text) {
        return get("/search?text={text}", userId, Map.of("text", text));
    }

    public ResponseEntity<Object> addComment(Long userId, Long itemId, CommentRequestDto commentRequestDto) {
        return post("/{itemId}/comment", userId, Map.of("itemId", itemId), commentRequestDto);
    }
}
