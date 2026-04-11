package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.comment.CommentRequestDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemClient itemClient;

    @Test
    void shouldCreateItem() throws Exception {
        ItemDtoRequest dto = new ItemDtoRequest("Дрель", "Мощная дрель", true, null);

        when(itemClient.save(eq(1L), any(ItemDtoRequest.class)))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(post("/items")
                        .header(USER_HEADER, 1)
                        .content(mapper.writeValueAsString(dto))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(itemClient).save(eq(1L), any(ItemDtoRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenHeaderMissing() throws Exception {
        ItemDtoRequest dto = new ItemDtoRequest("Дрель", "Мощная дрель", true, null);

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(dto))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetItemById() throws Exception {
        when(itemClient.getById(1L, 2L))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(get("/items/2")
                        .header(USER_HEADER, 1))
                .andExpect(status().isOk());

        verify(itemClient).getById(1L, 2L);
    }

    @Test
    void shouldSearchItems() throws Exception {
        when(itemClient.getItemBySearch(1L, "дрель"))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1L)
                        .param("text", "дрель"))
                .andExpect(status().isOk());

        verify(itemClient).getItemBySearch(1L, "дрель");
    }

    @Test
    void shouldReturnBadRequestWhenHeaderMissingForSearch() throws Exception {
        mvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetAllItemsByUser() throws Exception {
        when(itemClient.getAllItemsByUser(1L))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        verify(itemClient).getAllItemsByUser(1L);
    }

    @Test
    void shouldUpdateItem() throws Exception {
        ItemDtoUpdate dto = new ItemDtoUpdate("Дрель 2", "Новая", true);

        when(itemClient.updateItem(eq(1L), eq(2L), any(ItemDtoUpdate.class)))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(patch("/items/2")
                        .header(USER_HEADER, 1)
                        .content(mapper.writeValueAsString(dto))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(itemClient).updateItem(eq(1L), eq(2L), any(ItemDtoUpdate.class));
    }

    @Test
    void shouldAddComment() throws Exception {
        CommentRequestDto dto = new CommentRequestDto("Хорошая вещь");

        when(itemClient.addComment(eq(1L), eq(2L), any(CommentRequestDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(post("/items/2/comment")
                        .header(USER_HEADER, 1)
                        .content(mapper.writeValueAsString(dto))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(itemClient).addComment(eq(1L), eq(2L), any(CommentRequestDto.class));
    }
}