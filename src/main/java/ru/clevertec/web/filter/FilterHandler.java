package ru.clevertec.web.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.clevertec.service.dto.ErrorDto;

@Component
@Order(2)
public class FilterHandler extends HttpFilter {
    private static final String MSG_SERVER_ERROR = "Server error";
    private static final String DEFAULT_MSG = "Unknown error";

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException {
        try {
            chain.doFilter(req, res);
        } catch (Exception e) {
            ErrorDto dto = new ErrorDto(MSG_SERVER_ERROR, DEFAULT_MSG);
            writeMsg(res, 500, dto);
        }
    }

    private void writeMsg(HttpServletResponse res, int sc, ErrorDto dto) throws IOException {
        res.setStatus(sc);
        res.getWriter().write(new ObjectMapper().writeValueAsString(dto));
    }
}