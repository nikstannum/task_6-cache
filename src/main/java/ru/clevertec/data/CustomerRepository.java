package ru.clevertec.data;

import java.util.List;
import java.util.Optional;
import ru.clevertec.data.entity.Customer;

public interface CustomerRepository {
    Customer create(Customer customer);

    List<Customer> findAll(int limit, long offset);

    Optional<Customer> findById(Long id);

    Customer update(Customer customer);

    void deleteById(Long id);
}
