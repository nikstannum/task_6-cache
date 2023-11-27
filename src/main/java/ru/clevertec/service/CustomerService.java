package ru.clevertec.service;

import java.util.List;
import ru.clevertec.controller.util.paging.PagingUtil.Paging;
import ru.clevertec.service.dto.CustomerDto;

public interface CustomerService {
    CustomerDto create(CustomerDto customerDto);

    List<CustomerDto> findAll(Paging paging);

    CustomerDto findById(Long id);

    CustomerDto update(CustomerDto customerDto);

    void deleteById(Long id);
}
