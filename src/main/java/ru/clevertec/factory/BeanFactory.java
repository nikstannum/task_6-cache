package ru.clevertec.factory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import ru.clevertec.controller.CustomerController;
import ru.clevertec.controller.MessageAssistant;
import ru.clevertec.controller.util.json_parser.CustomParserAdapter;
import ru.clevertec.controller.util.json_parser.JacksonAdapter;
import ru.clevertec.controller.util.json_parser.JsonParser;
import ru.clevertec.controller.util.paging.PagingUtil;
import ru.clevertec.controller.util.xml_parser.XmlParser;
import ru.clevertec.controller.util.xml_parser.impl.JacksonXmlParser;
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
import ru.clevertec.exception.handler.ExceptionHandler;
import ru.clevertec.service.CustomerService;
import ru.clevertec.service.dto.CustomerDto;
import ru.clevertec.service.impl.CustomerServiceImpl;
import ru.clevertec.service.mapper.CustomerMapper;
import ru.clevertec.service.util.converter.Converter;
import ru.clevertec.service.util.converter.PDFConverter;
import ru.clevertec.service.util.formatter.Formatter;
import ru.clevertec.service.util.formatter.impl.PrettyJSONFormatter;

@Log4j2
public class BeanFactory implements Closeable {

    public final static BeanFactory INSTANCE = new BeanFactory();
    private final Map<Class<?>, Object> beans;
    private final List<Closeable> closeables;

    private BeanFactory() {
        this.beans = new HashMap<>();
        this.closeables = new ArrayList<>();
        try {
            init();
            log.info("factory initialized");
        } catch (Exception e) {
            log.error(e);
        }
    }

    private void init() {
        // data
        ConfigManager configManager = new ConfigManager("/application.yml");
        beans.put(ConfigManager.class, configManager);
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

        // JSON
        String parserType = (String) configManager.getProperty("json-parser");
        JsonParser parser;
        if (parserType.equalsIgnoreCase("jackson")) {
            parser = new JacksonAdapter();
        } else {
            parser = new CustomParserAdapter();
        }
        // service
        CustomerMapper customerMapper = CustomerMapper.INSTANCE;
        @SuppressWarnings("unchecked")
        Map<String, Object> pdfProps = (Map<String, Object>) configManager.getProperty("pdf");
        String templatePath = (String) pdfProps.get("templatePath");
        String fontPath = (String) pdfProps.get("fontPath");
        int fontSize = (int) pdfProps.get("fontSize");
        Converter converter = new PDFConverter(fontSize, templatePath, fontPath);
        Formatter formatter = new PrettyJSONFormatter();
        CustomerService customerService = new CustomerServiceImpl(customerRepository, customerMapper, converter, formatter, parser);

        // controller
        @SuppressWarnings("unchecked")
        Map<String, Integer> pagination = (Map<String, Integer>) configManager.getProperty("pagination");
        int defaultPageSize = pagination.get("defaultSize");
        PagingUtil pagingUtil = new PagingUtil(defaultPageSize);
        Validator<CustomerDto> customerDtoValidator = new CustomerValidator();
        CustomerController customerController = new CustomerController(customerService, pagingUtil, customerDtoValidator);

        // ExcHandler
        ExceptionHandler handler = new ExceptionHandler();
        beans.put(ExceptionHandler.class, handler);

        XmlParser xmlParser = new JacksonXmlParser();
        // MessageAssistant
        MessageAssistant messageAssistant = new MessageAssistant(customerController, parser, handler, xmlParser);
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
                log.error(e);
            }
        }
        log.info("factory closed");
    }
}
