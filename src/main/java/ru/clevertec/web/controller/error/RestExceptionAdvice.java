package ru.clevertec.web.controller.error;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.clevertec.exception.AppException;
import ru.clevertec.exception.BadRequestException;
import ru.clevertec.exception.NotFoundException;
import ru.clevertec.exception.ValidationException;
import ru.clevertec.service.dto.ErrorDto;

@Log4j2
@RequiredArgsConstructor
@RestControllerAdvice("ru.clevertec")
public class RestExceptionAdvice {
    private static final String MSG_SERVER_ERROR = "Server error";
    private static final String MSG_CLIENT_ERROR = "Client error";
    private static final String DEFAULT_MSG = "Unknown error";

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorDto error(NotFoundException e) {
        log.error(e);
        return new ErrorDto(MSG_CLIENT_ERROR, e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorDto error(BadRequestException e) {
        log.error(e);
        return new ErrorDto(MSG_CLIENT_ERROR, e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDto error(ValidationException e) {
        log.error(e);
        return new ErrorDto(MSG_CLIENT_ERROR, e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorDto error(AppException e) {
        log.error(e);
        return new ErrorDto(MSG_SERVER_ERROR, e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorDto error(Exception e) {
        log.error(e);
        return new ErrorDto(MSG_SERVER_ERROR, DEFAULT_MSG);
    }
}
