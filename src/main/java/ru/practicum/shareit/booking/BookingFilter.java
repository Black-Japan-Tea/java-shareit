package ru.practicum.shareit.booking;

// временной фильтр бронирований
public enum BookingFilter {
    ALL,        // Все бронирования
    CURRENT,    // Текущие (начались, но не закончились)
    PAST,       // Завершенные
    FUTURE,     // Будущие
    WAITING,    // Ожидающие подтверждения
    REJECTED,   // Отклоненные
    CANCELED    // Отмененные
}