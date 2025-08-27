package ru.practicum.shareit.gateway.exception;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class ErrorHandlerTest {

    private ErrorHandler errorHandler;

    @BeforeEach
    void setUp() {
        errorHandler = new ErrorHandler();
        WebRequest webRequest = new ServletWebRequest(new MockHttpServletRequest());
    }


    @Test
    void handleFeignException_returnsCorrectResponseEntity_withRealFeignException() {
        feign.Response response = feign.Response.builder()
                .status(404)
                .reason("Not Found")
                .request(mock(feign.Request.class))
                .build();

        FeignException exception = FeignException.errorStatus("test", response);

        ResponseEntity<ErrorResponse> result = errorHandler.handleFeignException(exception);

        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        ErrorResponse body = result.getBody();
        assertNotNull(body);
    }

    @Test
    void exceptionHandler_returnsBadRequest() {
        ValidationException ex = new ValidationException("Validation error");

        ErrorResponse response = errorHandler.exceptionHandler(ex);

        assertNotNull(response);
    }

    @Test
    void notFoundExceptionHandler_returnsNotFound() {
        NotFoundException ex = new NotFoundException("Not found");

        ErrorResponse response = errorHandler.notFoundExceptionHandler(ex);

        assertNotNull(response);
    }

    @Test
    void conflictExceptionHandler_returnsConflict() {
        ConflictException ex = new ConflictException("Conflict occurred");

        ErrorResponse response = errorHandler.conflictExceptionHandler(ex);

        assertNotNull(response);
    }

    @Test
    void forbiddenExceptionHandler_returnsForbidden() {
        ForbiddenException ex = new ForbiddenException("Access denied");

        ErrorResponse response = errorHandler.forbiddenExceptionHandler(ex);

        assertNotNull(response);
    }
}