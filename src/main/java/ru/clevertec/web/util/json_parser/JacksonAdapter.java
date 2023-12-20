package ru.clevertec.web.util.json_parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Adapter to Jackson JSON parser
 */
public class JacksonAdapter implements JsonParser {
    private final ObjectMapper objectMapper;

    public JacksonAdapter() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModules(new JavaTimeModule());
    }

    @Override
    public String write(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T read(String content, Class<T> clazz) {
        try {
            return objectMapper.readValue(content, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
