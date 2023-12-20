package ru.clevertec.web.util.json_parser;

/**
 * Common interface for JSON parsing
 */
public interface JsonParser {
    String write(Object o);

    <T> T read(String content, Class<T> clazz);

}
