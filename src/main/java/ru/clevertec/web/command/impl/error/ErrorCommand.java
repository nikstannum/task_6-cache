package ru.clevertec.web.command.impl.error;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import ru.clevertec.exception.AppException;
import ru.clevertec.exception.NotFoundException;
import ru.clevertec.exception.ValidationException;
import ru.clevertec.service.dto.ErrorDto;
import ru.clevertec.web.command.Command;
import ru.clevertec.web.util.json_parser.JsonParser;

@Log4j2
@RequiredArgsConstructor
public class ErrorCommand implements Command {

    private static final int CODE_BAD_REQUEST = 400;
    private static final int CODE_NOT_FOUND = 404;
    private static final int CODE_INTERNAL_SERVER_ERROR = 500;
    private static final String CONTENT_TYPE_APP_JSON = "application/json";
    private static final String MSG_SERVER_ERROR = "Server error";
    private static final String MSG_CLIENT_ERROR = "Client error";
    private static final String DEFAULT_MSG = "Unknown error";
    private final JsonParser jsonParser;

    @Override
    public String execute(HttpServletRequest req, HttpServletResponse res) {
        Throwable e = (Throwable) req.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        log.error(e);
        res.setContentType(CONTENT_TYPE_APP_JSON);
        if (e instanceof NotFoundException) {
            ErrorDto errorDto = new ErrorDto(MSG_CLIENT_ERROR, e.getMessage());
            res.setStatus(CODE_NOT_FOUND);
            return getString(errorDto);
        } else if (e instanceof ValidationException) {
            ErrorDto errorDto = new ErrorDto(MSG_CLIENT_ERROR, e.getMessage());
            res.setStatus(CODE_BAD_REQUEST);
            return getString(errorDto);
        } else if (e instanceof AppException) {
            ErrorDto errorDto = new ErrorDto(MSG_SERVER_ERROR, e.getMessage());
            res.setStatus(CODE_INTERNAL_SERVER_ERROR);
            return getString(errorDto);
        } else {
            ErrorDto errorDto = new ErrorDto(MSG_SERVER_ERROR, DEFAULT_MSG);
            res.setStatus(CODE_INTERNAL_SERVER_ERROR);
            return getString(errorDto);
        }
    }

    private String getString(ErrorDto errorDto) {
        return jsonParser.write(errorDto);
    }
}
