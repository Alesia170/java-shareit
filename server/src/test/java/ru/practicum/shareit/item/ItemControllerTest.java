package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import ru.practicum.shareit.item.comment.CommentRequestDto;
import ru.practicum.shareit.item.comment.CommentResponseDto;
import ru.practicum.shareit.item.dto.*;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ItemControllerTest {

    @Mock
    private ItemService itemService;

    @InjectMocks
    private ItemController itemController;

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private MockMvc mvc;

    private ItemDtoRequest itemDtoRequest;
    private ItemDtoResponse itemDtoResponse;
    private ItemDtoUpdate itemDtoUpdate;
    private ItemOwnerDto itemOwnerDto;
    private ItemBookingDto itemBookingDto;
    private CommentRequestDto commentRequestDto;
    private CommentResponseDto commentResponseDto;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(itemController)
                .build();

        itemDtoRequest = new ItemDtoRequest("name", "description", true, 1L);
        itemDtoResponse = new ItemDtoResponse(1L, "name", "description", true, 1L, List.of());
        itemDtoUpdate = new ItemDtoUpdate("changed", "changed description", true);
        itemOwnerDto = new ItemOwnerDto();
        itemOwnerDto.setId(1L);
        itemOwnerDto.setName("name");
        itemOwnerDto.setDescription("description");
        itemOwnerDto.setAvailable(true);

        itemBookingDto = new ItemBookingDto();
        itemBookingDto.setId(1L);
        itemBookingDto.setName("name");
        itemBookingDto.setDescription("description");
        itemBookingDto.setAvailable(true);

        commentRequestDto = new CommentRequestDto("good item");
        commentResponseDto = new CommentResponseDto();
        commentResponseDto.setId(1L);
        commentResponseDto.setText("good item");
    }

    @Test
    void shouldGetAllItemsByUser() throws Exception {
        when(itemService.getAllItemsByUser(1L))
                .thenReturn(List.of(itemBookingDto));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("name")))
                .andExpect(jsonPath("$[0].description", is("description")))
                .andExpect(jsonPath("$[0].available", is(true)));

        verify(itemService).getAllItemsByUser(1L);
    }

    @Test
    void shouldSaveNewItem() throws Exception {
        when(itemService.save(eq(1L), any(ItemDtoRequest.class)))
                .thenReturn(itemDtoResponse);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemDtoRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("name")))
                .andExpect(jsonPath("$.description", is("description")))
                .andExpect(jsonPath("$.available", is(true)));

        verify(itemService).save(eq(1L), any(ItemDtoRequest.class));
    }

    @Test
    void shouldUpdateItem() throws Exception {
        ItemDtoResponse updatedResponse = new ItemDtoResponse(1L, "changed", "changed description", true, 1L, List.of());

        when(itemService.updateItem(eq(1L), eq(1L), any(ItemDtoUpdate.class)))
                .thenReturn(updatedResponse);

        mvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemDtoUpdate))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("changed")))
                .andExpect(jsonPath("$.description", is("changed description")))
                .andExpect(jsonPath("$.available", is(true)));

        verify(itemService).updateItem(eq(1L), eq(1L), any(ItemDtoUpdate.class));
    }

    @Test
    void shouldGetItemById() throws Exception {
        when(itemService.getById(1L, 1L))
                .thenReturn(itemOwnerDto);

        mvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("name")))
                .andExpect(jsonPath("$.description", is("description")))
                .andExpect(jsonPath("$.available", is(true)));

        verify(itemService).getById(1L, 1L);
    }

    @Test
    void shouldGetItemBySearch() throws Exception {
        when(itemService.getItemBySearch("text"))
                .thenReturn(List.of(itemDtoResponse));

        mvc.perform(get("/items/search")
                        .param("text", "text")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("name")))
                .andExpect(jsonPath("$[0].description", is("description")))
                .andExpect(jsonPath("$[0].available", is(true)));

        verify(itemService).getItemBySearch("text");
    }

    @Test
    void shouldAddComment() throws Exception {
        when(itemService.addComment(eq(1L), eq(1L), any(CommentRequestDto.class)))
                .thenReturn(commentResponseDto);

        mvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(commentRequestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.text", is("good item")));

        verify(itemService).addComment(eq(1L), eq(1L), any(CommentRequestDto.class));
    }

    @Test
    void shouldReturnNotFoundWhenItemDoesNotExist() throws Exception {
        when(itemService.getById(1L, 999L))
                .thenThrow(new NotFoundException("Вещь с id=999 не найдена"));

        mvc.perform(get("/items/999")
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(itemService).getById(1L, 999L);
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonexistentItem() throws Exception {
        when(itemService.updateItem(eq(1L), eq(999L), any(ItemDtoUpdate.class)))
                .thenThrow(new NotFoundException("Вещь с id=999 не найдена"));

        mvc.perform(patch("/items/999")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemDtoUpdate))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(itemService).updateItem(eq(1L), eq(999L), any(ItemDtoUpdate.class));
    }
}
