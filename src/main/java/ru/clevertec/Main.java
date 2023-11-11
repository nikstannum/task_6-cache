package ru.clevertec;

import ru.clevertec.data.CustomerRepository;
import ru.clevertec.data.entity.Customer;
import ru.clevertec.factory.BeanFactory;

public class Main {
    public static void main(String[] args) {
        BeanFactory factory = BeanFactory.INSTANCE;
        CustomerRepository customerRepo = factory.getBean(CustomerRepository.class);
        Customer customer = customerRepo.findById(1L).orElseThrow();
        System.out.println(customer);
    }
}
