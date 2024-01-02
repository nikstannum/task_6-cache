package ru.clevertec.service.util.formatter.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Component;
import ru.clevertec.service.util.formatter.Formatter;

/**
 * Formatter for formatting a JSON string into a human-readable form
 */
@Component
public class PrettyJSONFormatter implements Formatter {

    private final ObjectMapper mapper;

    public PrettyJSONFormatter() {
        mapper = new ObjectMapper();
    }

    @Override
    public String format(String content) {
        try {
            JsonNode jsonNode = mapper.readTree(content);
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            return mapper.writeValueAsString(jsonNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
