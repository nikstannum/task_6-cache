package ru.clevertec.exception.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import ru.clevertec.exception.AppException;
import ru.clevertec.exception.NotFoundException;
import ru.clevertec.exception.ValidationException;

@Log4j2
@RequiredArgsConstructor
public class ExceptionHandler {
    private static final String MSG_APP_ERROR = "Application error";
    private static final String MSG_CLIENT_ERROR = "Client error";
    private static final String DEFAULT_MESSAGE = "Unknown error";

    public void handle(Exception e) {
        if (e instanceof NotFoundException) {
            log.error(e);
            System.err.println(MSG_CLIENT_ERROR + " " + e.getMessage());
        } else if (e instanceof ValidationException) {
            log.error(e);
            System.err.println(MSG_CLIENT_ERROR + " " + e.getMessage());
        } else if (e instanceof AppException) {
            log.error(e);
            System.err.println(MSG_APP_ERROR + " " + e.getMessage());
        } else {
            log.error(e);
            System.err.println(MSG_APP_ERROR + " " + DEFAULT_MESSAGE);
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }
}
