package ru.clevertec.controller.util.json_parser;

import ru.clevertec.deserializer.Deserializer;
import ru.clevertec.deserializer.impl.DeserializerImpl;
import ru.clevertec.serializer.Serializer;
import ru.clevertec.serializer.impl.SerializerImpl;

public class CustomParserAdapter implements JsonParser {
    private final Deserializer deserializer;
    private final Serializer serializer;

    public CustomParserAdapter() {
        this.deserializer = new DeserializerImpl();
        this.serializer = new SerializerImpl();
    }

    @Override
    public String write(Object o) {
        try {
            return serializer.serialize(o);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T read(String content, Class<T> clazz) {
        try {
            return deserializer.deserialize(content, clazz);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
