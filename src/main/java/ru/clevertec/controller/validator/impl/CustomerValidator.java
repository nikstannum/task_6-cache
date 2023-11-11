package ru.clevertec.controller.validator.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import ru.clevertec.controller.validator.Validator;
import ru.clevertec.service.dto.CustomerDto;

public class CustomerValidator implements Validator<CustomerDto> {

    private static final String FIRST_NAME = "first name";
    private static final String LAST_NAME = "last name";
    private static final String EMAIL = "email";
    private static final String BIRTH_DATE = "date of birth";
    private static final String FIRST_LAST_NAME_CONSTRAINT = "should be less than 50 chars and contain only letters";
    private static final String EMAIL_CONSTRAINT = "should be email and contain less than 50 symbols";
    private static final String BIRTH_DATE_CONSTRAINT = "at least you have to birth";

    @Override
    public Map<Result, List<String>> validate(CustomerDto customerDto) {
        Map<String, Boolean> resultSet = new HashMap<>();
        String firstNameLastNameRegex = "^[a-zA-Z]{1,50}$";
        String firstName = customerDto.getFirstName();
        boolean isValidFirstName = firstName.matches(firstNameLastNameRegex);
        resultSet.put(FIRST_NAME, isValidFirstName);
        String lastName = customerDto.getLastName();
        boolean isValidLastName = lastName.matches(firstNameLastNameRegex);
        resultSet.put(LAST_NAME, isValidLastName);
        String emailRegex = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
        String email = customerDto.getEmail();
        boolean isValidEmail = email.matches(emailRegex) && email.length() <= 50;
        resultSet.put(EMAIL, isValidEmail);
        LocalDate now = LocalDate.now();
        LocalDate customerBirthDate = customerDto.getDateBirth();
        boolean isValidBirthDate = customerBirthDate.minusDays(1).isBefore(now);
        resultSet.put(BIRTH_DATE, isValidBirthDate);
        return validateFields(resultSet);
    }

    private Map<Result, List<String>> validateFields(Map<String, Boolean> resultSet) {
        Map<Result, List<String>> result = new HashMap<>();
        List<String> messages = new ArrayList<>();
        for (Entry<String, Boolean> stringBooleanEntry : resultSet.entrySet()) {
            boolean isValid = stringBooleanEntry.getValue();
            if (!isValid) {
                String field = stringBooleanEntry.getKey();
                String constraint = resolveMessage(field);
                String message = String.format("invalid %s: %s\n", field, constraint);
                messages.add(message);
            }
        }
        Result validationResult;
        if (messages.isEmpty()) {
            validationResult = Result.OK;
        } else {
            validationResult = Result.FAIL;
        }
        result.put(validationResult, messages);
        return result;
    }

    private String resolveMessage(String field) {
        String message = "";
        switch (field) {
            case FIRST_NAME, LAST_NAME -> message = FIRST_LAST_NAME_CONSTRAINT;
            case EMAIL -> message = EMAIL_CONSTRAINT;
            case BIRTH_DATE -> message = BIRTH_DATE_CONSTRAINT;
        }
        return message;
    }
}
