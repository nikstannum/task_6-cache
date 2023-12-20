package ru.clevertec.web.validator;

import java.util.List;
import java.util.Map;

/**
 * Validator for validation input data
 *
 * @param <T> validated type
 */
public interface Validator<T> {
    /**
     * @param t validate object
     * @return result of validation and list of messages
     */
    Map<Result, List<String>> validate(T t);

    enum Result {
        OK, FAIL
    }
}
