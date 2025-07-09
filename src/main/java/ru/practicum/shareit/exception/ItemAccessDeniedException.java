package ru.practicum.shareit.exception;

public class ItemAccessDeniedException extends RuntimeException {
    public ItemAccessDeniedException(String message) {
        super(message);
    }
}
