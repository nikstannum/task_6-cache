package ru.clevertec.factory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.clevertec.controller.CustomerController;
import ru.clevertec.controller.MessageAssistant;
import ru.clevertec.controller.util.json_parser.JacksonAdapter;
import ru.clevertec.controller.util.json_parser.JsonParser;
import ru.clevertec.controller.util.paging.PagingUtil;
import ru.clevertec.controller.validator.Validator;
import ru.clevertec.controller.validator.impl.CustomerValidator;
import ru.clevertec.data.CustomerRepository;
import ru.clevertec.data.cache.Cache;
import ru.clevertec.data.cache.impl.LFUCacheImpl;
import ru.clevertec.data.cache.impl.LRUCacheImpl;
import ru.clevertec.data.cache.proxy.ProxyCustomerRepository;
import ru.clevertec.data.connection.ConfigManager;
import ru.clevertec.data.connection.DataSource;
import ru.clevertec.data.impl.CustomerRepositoryImpl;
import ru.clevertec.service.CustomerService;
import ru.clevertec.service.dto.CustomerDto;
import ru.clevertec.service.impl.CustomerServiceImpl;
import ru.clevertec.service.mapper.CustomerMapper;

public class BeanFactory implements Closeable {

    public final static BeanFactory INSTANCE = new BeanFactory();
    private final Map<Class<?>, Object> beans;
    private final List<Closeable> closeables;

    private BeanFactory() {
        this.beans = new HashMap<>();
        this.closeables = new ArrayList<>();

        // data
        ConfigManager configManager = new ConfigManager("/application.yml");
        DataSource dataSource = new DataSource(configManager);
        closeables.add(dataSource);
        @SuppressWarnings("unchecked")
        Map<String, Object> cacheProps = (Map<String, Object>) configManager.getProperty("cache");
        String cacheType = (String) cacheProps.get("type");
        int expirationTime = (int) cacheProps.get("expirationTime");
        int cacheSize = (int) cacheProps.get("size");
        CustomerRepository customerRepository = new CustomerRepositoryImpl(dataSource);
        if ("LRU".equalsIgnoreCase(cacheType)) {
            Cache cache = new LRUCacheImpl(cacheSize, expirationTime);
            customerRepository = new ProxyCustomerRepository(customerRepository, cache);
            closeables.add(cache);
        } else if ("LFU".equalsIgnoreCase(cacheType)) {
            Cache cache = new LFUCacheImpl(cacheSize, expirationTime);
            customerRepository = new ProxyCustomerRepository(customerRepository, cache);
            closeables.add(cache);
        }
        // service
        CustomerMapper customerMapper = CustomerMapper.INSTANCE;
        CustomerService customerService = new CustomerServiceImpl(customerRepository, customerMapper);

        // controller
        @SuppressWarnings("unchecked")
        Map<String, Integer> pagination = (Map<String, Integer>) configManager.getProperty("pagination");
        int defaultPageSize = pagination.get("defaultSize");
        PagingUtil pagingUtil = new PagingUtil(defaultPageSize);
        Validator<CustomerDto> customerDtoValidator = new CustomerValidator();
        CustomerController customerController = new CustomerController(customerService, pagingUtil, customerDtoValidator);
        String parserType = (String) configManager.getProperty("json-parser");
        JsonParser parser;
        if (parserType.equalsIgnoreCase("jackson")) {
            parser = new JacksonAdapter();
        } else {
            throw new RuntimeException("add custom json parser"); // FIXME добавить либу с кастомным парсером и через адаптер инжектнуть
        }


        MessageAssistant messageAssistant = new MessageAssistant(customerController, parser);
        closeables.add(messageAssistant);
        beans.put(MessageAssistant.class, messageAssistant);

    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<?> clazz) {
        return (T) beans.get(clazz);
    }

    @Override
    public void close() {
        for (Closeable closeable : closeables) {
            try {
                closeable.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
