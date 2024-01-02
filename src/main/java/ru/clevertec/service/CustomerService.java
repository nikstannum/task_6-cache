package ru.clevertec.service;

import java.util.List;
import ru.clevertec.service.dto.CustomerDto;
import ru.clevertec.web.util.paging.PagingUtil.Paging;

public interface CustomerService {
    CustomerDto create(CustomerDto customerDto);

    List<CustomerDto> findAll(Paging paging);

    CustomerDto findById(Long id);

    CustomerDto update(CustomerDto customerDto);

    void deleteById(Long id);
}
