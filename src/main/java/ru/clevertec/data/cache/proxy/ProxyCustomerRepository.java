package ru.clevertec.data.cache.proxy;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import ru.clevertec.data.CustomerRepository;
import ru.clevertec.data.cache.Cache;
import ru.clevertec.data.entity.Customer;

/**
 * Proxy repository connecting the real repository and the cache
 */
@RequiredArgsConstructor
public class ProxyCustomerRepository implements CustomerRepository {
    private final CustomerRepository customerRepository;
    private final Cache cache;


    @Override
    public Customer create(Customer customer) {
        Customer created = customerRepository.create(customer);
        String key = created.getId().toString();
        cache.put(key, Customer.class.getSimpleName(), created);
        return created;
    }

    @Override
    public List<Customer> findAll(int limit, long offset) {
        return customerRepository.findAll(limit, offset);
    }

    @Override
    public Optional<Customer> findById(Long id) {
        String key = id.toString();
        String cacheName = Customer.class.getSimpleName();
        boolean isContains = cache.isContains(key, cacheName);
        if (isContains) {
            return Optional.of((Customer) cache.take(key, cacheName));
        } else {
            Optional<Customer> customerOpt = customerRepository.findById(id);
            customerOpt.ifPresent(customer -> cache.put(key, cacheName, customer));
            return customerOpt;
        }
    }

    @Override
    public Customer update(Customer customer) {
        Customer updated = customerRepository.update(customer);
        String key = updated.getId().toString();
        cache.put(key, Customer.class.getSimpleName(), updated);
        return updated;
    }

    @Override
    public void deleteById(Long id) {
        customerRepository.deleteById(id);
        String key = id.toString();
        String cacheName = Customer.class.getSimpleName();
        cache.delete(key, cacheName);
    }
}
