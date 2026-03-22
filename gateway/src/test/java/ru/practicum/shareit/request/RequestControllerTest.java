package ru.practicum.shareit.request;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class RequestControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private RequestClient requestClient;

    @Test
    void shouldCreateRequest() throws Exception {
        ItemRequestRequestDto dto = new ItemRequestRequestDto("Нужна дрель");

        when(requestClient.saveNewRequest(eq(1L), any(ItemRequestRequestDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(post("/requests")
                        .header(USER_HEADER, 1)
                        .content(mapper.writeValueAsString(dto))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(requestClient).saveNewRequest(eq(1L), any(ItemRequestRequestDto.class));
    }

    @Test
    void shouldReturnBadRequestWhenDescriptionBlank() throws Exception {
        ItemRequestRequestDto dto = new ItemRequestRequestDto("");

        mvc.perform(post("/requests")
                        .header(USER_HEADER, 1)
                        .content(mapper.writeValueAsString(dto))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetOwnRequests() throws Exception {
        when(requestClient.getOwnRequests(1L))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(get("/requests")
                        .header(USER_HEADER, 1))
                .andExpect(status().isOk());

        verify(requestClient).getOwnRequests(1L);
    }

    @Test
    void shouldGetAllRequests() throws Exception {
        when(requestClient.getRequestsCreatedByOtherUsers(1L))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(get("/requests/all")
                        .header(USER_HEADER, 1))
                .andExpect(status().isOk());

        verify(requestClient).getRequestsCreatedByOtherUsers(1L);
    }

    @Test
    void shouldGetRequestById() throws Exception {
        when(requestClient.getRequestById(1L, 2L))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(get("/requests/2")
                        .header(USER_HEADER, 1))
                .andExpect(status().isOk());

        verify(requestClient).getRequestById(1L, 2L);
    }
}