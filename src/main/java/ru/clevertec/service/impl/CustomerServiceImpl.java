package ru.clevertec.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import ru.clevertec.controller.util.paging.PagingUtil.Paging;
import ru.clevertec.data.CustomerRepository;
import ru.clevertec.data.entity.Customer;
import ru.clevertec.exception.NotFoundException;
import ru.clevertec.service.CustomerService;
import ru.clevertec.service.dto.CustomerDto;
import ru.clevertec.service.mapper.CustomerMapper;

@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    public CustomerDto create(CustomerDto customerDto) {
        Customer customer = customerMapper.convert(customerDto);
        Customer created = customerRepository.create(customer);
        return customerMapper.convert(created);
    }

    @Override
    public List<CustomerDto> findAll(Paging paging) {
        int limit = paging.getLimit();
        long offset = paging.getOffset();
        List<Customer> customers = customerRepository.findAll(limit, offset);
        return customers.stream().map(customerMapper::convert).toList();
    }

    @Override
    public CustomerDto findById(Long id) {
        return customerMapper.convert(customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Wasn't found customer with id = " + id)));
    }

    @Override
    public CustomerDto update(CustomerDto customerDto) {
        Customer customer = customerMapper.convert(customerDto);
        Customer updated = customerRepository.update(customer);
        return customerMapper.convert(updated);
    }

    @Override
    public void deleteById(Long id) {
        customerRepository.deleteById(id);
    }
}
