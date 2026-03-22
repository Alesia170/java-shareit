package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BookingClientTest {

    private RestTemplateBuilder builder;
    private RestTemplate restTemplate;
    private BookingClient bookingClient;

    @BeforeEach
    void setUp() {
        builder = mock(RestTemplateBuilder.class);
        restTemplate = mock(RestTemplate.class);

        when(builder.uriTemplateHandler(any(DefaultUriBuilderFactory.class))).thenReturn(builder);
        when(builder.requestFactory(any(Supplier.class))).thenReturn(builder);
        when(builder.build()).thenReturn(restTemplate);

        bookingClient = new BookingClient("http://localhost:9090", builder);
    }

    @Test
    void shouldSaveNewBooking() {
        BookingDtoRequest request = new BookingDtoRequest();
        request.setItemId(1L);
        request.setStart(LocalDateTime.of(2026, 3, 23, 12, 0));
        request.setEnd(LocalDateTime.of(2026, 3, 24, 12, 0));

        ResponseEntity<Object> serverResponse = ResponseEntity.ok("created");

        when(restTemplate.exchange(
                eq(""),
                eq(HttpMethod.POST),
                org.mockito.ArgumentMatchers.<HttpEntity<?>>any(),
                eq(Object.class)
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = bookingClient.saveNewBooking(7L, request);

        assertThat(response.getBody(), equalTo("created"));

        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq(""), eq(HttpMethod.POST), captor.capture(), eq(Object.class));

        assertThat(captor.getValue().getBody(), equalTo(request));
        assertThat(captor.getValue().getHeaders().getFirst("X-Sharer-User-Id"), equalTo("7"));
    }

    @Test
    void shouldApproveBooking() {
        ResponseEntity<Object> serverResponse = ResponseEntity.ok("approved");

        when(restTemplate.exchange(
                eq("/{bookingId}?approved={approved}"),
                eq(HttpMethod.PATCH),
                org.mockito.ArgumentMatchers.<HttpEntity<?>>any(),
                eq(Object.class),
                anyMap()
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = bookingClient.approveBooking(5L, 11L, true);

        assertThat(response.getBody(), equalTo("approved"));

        ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);

        verify(restTemplate).exchange(
                eq("/{bookingId}?approved={approved}"),
                eq(HttpMethod.PATCH),
                entityCaptor.capture(),
                eq(Object.class),
                mapCaptor.capture()
        );

        assertThat(entityCaptor.getValue().getHeaders().getFirst("X-Sharer-User-Id"), equalTo("5"));
        assertThat(entityCaptor.getValue().getBody(), equalTo(null));
        assertThat(mapCaptor.getValue(), hasEntry("bookingId", 11L));
        assertThat(mapCaptor.getValue(), hasEntry("approved", true));
    }

    @Test
    void shouldGetBookingById() {
        ResponseEntity<Object> serverResponse = ResponseEntity.ok("booking");

        when(restTemplate.exchange(
                eq("/15"),
                eq(HttpMethod.GET),
                org.mockito.ArgumentMatchers.<HttpEntity<?>>any(),
                eq(Object.class)
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = bookingClient.getBookingById(3L, 15L);

        assertThat(response.getBody(), equalTo("booking"));

        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq("/15"), eq(HttpMethod.GET), captor.capture(), eq(Object.class));

        assertThat(captor.getValue().getHeaders().getFirst("X-Sharer-User-Id"), equalTo("3"));
    }

    @Test
    void shouldGetUserBookings() {
        ResponseEntity<Object> serverResponse = ResponseEntity.ok("user-bookings");

        when(restTemplate.exchange(
                eq("?state={state}"),
                eq(HttpMethod.GET),
                org.mockito.ArgumentMatchers.<HttpEntity<?>>any(),
                eq(Object.class),
                anyMap()
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = bookingClient.getUserBookings(9L, BookingState.ALL);

        assertThat(response.getBody(), equalTo("user-bookings"));

        ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);

        verify(restTemplate).exchange(
                eq("?state={state}"),
                eq(HttpMethod.GET),
                entityCaptor.capture(),
                eq(Object.class),
                mapCaptor.capture()
        );

        assertThat(entityCaptor.getValue().getHeaders().getFirst("X-Sharer-User-Id"), equalTo("9"));
        assertThat(mapCaptor.getValue(), hasEntry("state", BookingState.ALL));
    }

    @Test
    void shouldGetOwnerBookings() {
        ResponseEntity<Object> serverResponse = ResponseEntity.ok("owner-bookings");

        when(restTemplate.exchange(
                eq("/owner?state={state}"),
                eq(HttpMethod.GET),
                org.mockito.ArgumentMatchers.<HttpEntity<?>>any(),
                eq(Object.class),
                anyMap()
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = bookingClient.getOwnerBookings(12L, BookingState.REJECTED);

        assertThat(response.getBody(), equalTo("owner-bookings"));

        ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);

        verify(restTemplate).exchange(
                eq("/owner?state={state}"),
                eq(HttpMethod.GET),
                entityCaptor.capture(),
                eq(Object.class),
                mapCaptor.capture()
        );

        assertThat(entityCaptor.getValue().getHeaders().getFirst("X-Sharer-User-Id"), equalTo("12"));
        assertThat(mapCaptor.getValue(), hasEntry("state", BookingState.REJECTED));
    }
}
