package ru.clevertec.service.util.converter;

import java.io.OutputStream;

public interface Converter {
    void convert(String content, OutputStream outputStream);
}
