package ru.practicum.shareit.booking;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingDtoResponse saveNewBooking(Long userId, BookingDtoRequest bookingDtoRequest) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        Item item = itemRepository.findById(bookingDtoRequest.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + bookingDtoRequest.getItemId() + " не найден"));

        if (!item.getAvailable()) {
            throw new ValidationException("Вещь недоступна");
        }

        if (item.getOwner().getId().equals(userId)) {
            throw new ValidationException("Владелец не может бронировать свою вещь");
        }

        Booking booking = BookingMapper.toBooking(bookingDtoRequest);

        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(Status.WAITING);

        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDtoResponse approveBooking(Long userId, Long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Подтверждение или отклонение доступно только владельцу вещи");
        }

        if (booking.getStatus() != Status.WAITING) {
            throw new ValidationException("Статус бронирования уже изменен");
        }

        booking.setStatus(approved ? Status.APPROVED : Status.REJECTED);

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDtoResponse getBookingById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getItem().getOwner().getId().equals(userId) && !booking.getBooker().getId().equals(userId)) {
            throw new NotFoundException("Бронирование не найдено");
        }

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDtoResponse> getUserBookings(Long userId, String state) {
        checkUserExists(userId);

        return getBookings(userId, BookingState.from(state), false);
    }

    @Override
    public List<BookingDtoResponse> getOwnerBookings(Long userId, String state) {
        checkUserExists(userId);

        return getBookings(userId, BookingState.from(state), true);
    }

    private void checkUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }

    private List<BookingDtoResponse> getBookings(Long userId, BookingState state, boolean owner) {
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state) {
            case ALL -> owner
                    ? bookingRepository.findByItemOwnerIdOrderByStartDesc(userId)
                    : bookingRepository.findByBookerIdOrderByStartDesc(userId);
            case FUTURE -> owner
                    ? bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(userId, now)
                    : bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(userId, now);
            case PAST -> owner
                    ? bookingRepository.findByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, now)
                    : bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(userId, now);
            case CURRENT -> owner
                    ? bookingRepository.findCurrentByOwner(userId, now)
                    : bookingRepository.findCurrentByBooker(userId, now);
            case WAITING -> owner
                    ? bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.WAITING)
                    : bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, Status.WAITING);
            case REJECTED -> owner
                    ? bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.REJECTED)
                    : bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, Status.REJECTED);
        };

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }
}
