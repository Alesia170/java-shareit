package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingClient bookingClient;

    @Test
    void shouldCreateBooking() throws Exception {
        BookingDtoRequest dto = new BookingDtoRequest(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                1L
        );

        when(bookingClient.saveNewBooking(eq(1L), any(BookingDtoRequest.class)))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(post("/bookings")
                        .header(USER_HEADER, 1)
                        .content(mapper.writeValueAsString(dto))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(bookingClient).saveNewBooking(eq(1L), any(BookingDtoRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenStartInPast() throws Exception {
        BookingDtoRequest dto = new BookingDtoRequest(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                1L
        );

        mvc.perform(post("/bookings")
                        .header(USER_HEADER, 1)
                        .content(mapper.writeValueAsString(dto))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenEndNotInFuture() throws Exception {
        BookingDtoRequest dto = new BookingDtoRequest(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now(),
                1L
        );

        mvc.perform(post("/bookings")
                        .header(USER_HEADER, 1)
                        .content(mapper.writeValueAsString(dto))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenItemIdIsNull() throws Exception {
        BookingDtoRequest dto = new BookingDtoRequest(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                null
        );

        mvc.perform(post("/bookings")
                        .header(USER_HEADER, 1)
                        .content(mapper.writeValueAsString(dto))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldApproveBooking() throws Exception {
        when(bookingClient.approveBooking(1L, 2L, true))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(patch("/bookings/2")
                        .header(USER_HEADER, 1)
                        .param("approved", "true"))
                .andExpect(status().isOk());

        verify(bookingClient).approveBooking(1L, 2L, true);
    }

    @Test
    void shouldGetBookingById() throws Exception {
        when(bookingClient.getBookingById(1L, 2L))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(get("/bookings/2")
                        .header(USER_HEADER, 1))
                .andExpect(status().isOk());

        verify(bookingClient).getBookingById(1L, 2L);
    }

    @Test
    void shouldGetUserBookings() throws Exception {
        when(bookingClient.getUserBookings(1L, BookingState.ALL))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(get("/bookings")
                        .header(USER_HEADER, 1)
                        .param("state", "ALL"))
                .andExpect(status().isOk());

        verify(bookingClient).getUserBookings(1L, BookingState.ALL);
    }

    @Test
    void shouldReturnBadRequestWhenStateInvalid() throws Exception {
        mvc.perform(get("/bookings")
                        .header(USER_HEADER, 1)
                        .param("state", "WRONG"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetBookingsByOwner() throws Exception {
        when(bookingClient.getOwnerBookings(1L, BookingState.ALL))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, 1)
                        .param("state", "ALL"))
                .andExpect(status().isOk());

        verify(bookingClient).getOwnerBookings(1L, BookingState.ALL);
    }
}