package ru.clevertec.controller;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import ru.clevertec.controller.util.paging.PagingUtil;
import ru.clevertec.controller.util.paging.PagingUtil.Paging;
import ru.clevertec.controller.validator.Validator;
import ru.clevertec.controller.validator.Validator.Result;
import ru.clevertec.exception.ValidationException;
import ru.clevertec.service.CustomerService;
import ru.clevertec.service.dto.CustomerDto;

@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final PagingUtil pagingUtil;
    private final Validator<CustomerDto> validator;

    CustomerDto create(CustomerDto customerDto) {
        Map<Result, List<String>> validationResult = validator.validate(customerDto);
        if (validationResult.containsKey(Result.FAIL)) {
            throw new ValidationException(String.valueOf(validationResult.remove(Result.FAIL)));
        }
        return customerService.create(customerDto);
    }

    List<CustomerDto> findAll(int page, int size) {
        Paging paging = pagingUtil.getPaging(page, size);
        return customerService.findAll(paging);
    }

    CustomerDto findById(Long id) {
        return customerService.findById(id);
    }

    CustomerDto update(CustomerDto customerDto) {
        Map<Result, List<String>> validationResult = validator.validate(customerDto);
        if (validationResult.containsKey(Result.FAIL)) {
            throw new ValidationException(String.valueOf(validationResult.remove(Result.FAIL)));
        }
        return customerService.update(customerDto);
    }

    void deleteById(Long id) {
        customerService.deleteById(id);
    }
}
