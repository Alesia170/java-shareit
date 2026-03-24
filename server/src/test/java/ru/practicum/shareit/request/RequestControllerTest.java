package ru.practicum.shareit.request;

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
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.dto.RequestResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class RequestControllerTest {

    @Mock
    private RequestService requestService;

    @InjectMocks
    private RequestController requestController;

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private MockMvc mvc;

    private RequestDto requestDto;
    private RequestResponseDto responseDto;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(requestController)
                .build();

        requestDto = new RequestDto("Нужна дрель");

        responseDto = new RequestResponseDto();
        responseDto.setId(1L);
        responseDto.setDescription("Нужна дрель");
        responseDto.setCreated(LocalDateTime.of(2030, 1, 10, 12, 0));
        responseDto.setItems(List.of());
    }

    @Test
    void shouldSaveNewRequest() throws Exception {
        when(requestService.saveNewRequest(eq(1L), any(RequestDto.class)))
                .thenReturn(responseDto);

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is("Нужна дрель")));

        verify(requestService).saveNewRequest(eq(1L), any(RequestDto.class));
    }

    @Test
    void shouldGetOwnRequests() throws Exception {
        when(requestService.getOwnRequests(1L))
                .thenReturn(List.of(responseDto));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].description", is("Нужна дрель")));

        verify(requestService).getOwnRequests(1L);
    }

    @Test
    void shouldGetRequestsCreatedByOtherUsers() throws Exception {
        when(requestService.getRequestsCreatedByOtherUsers(1L))
                .thenReturn(List.of(responseDto));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].description", is("Нужна дрель")));

        verify(requestService).getRequestsCreatedByOtherUsers(1L);
    }

    @Test
    void shouldGetRequestById() throws Exception {
        when(requestService.getRequestById(1L, 1L))
                .thenReturn(responseDto);

        mvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is("Нужна дрель")));

        verify(requestService).getRequestById(1L, 1L);
    }

    @Test
    void shouldReturnNotFoundWhenRequestDoesNotExist() throws Exception {
        when(requestService.getRequestById(1L, 999L))
                .thenThrow(new NotFoundException("Запрос с id=999 не найден"));

        mvc.perform(get("/requests/999")
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(requestService).getRequestById(1L, 999L);
    }
}
