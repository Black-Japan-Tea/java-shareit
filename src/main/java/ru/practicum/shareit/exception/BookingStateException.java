package ru.practicum.shareit.exception;

public class BookingStateException extends RuntimeException {
    public BookingStateException(String message) {
        super(message);
    }
}