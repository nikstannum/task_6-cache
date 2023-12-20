package ru.clevertec.factory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import liquibase.command.CommandScope;
import liquibase.exception.CommandExecutionException;
import lombok.extern.log4j.Log4j2;
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
import ru.clevertec.service.util.converter.Converter;
import ru.clevertec.service.util.converter.PDFConverter;
import ru.clevertec.service.util.formatter.Formatter;
import ru.clevertec.service.util.formatter.impl.PrettyJSONFormatter;
import ru.clevertec.web.command.impl.customer.CustomerController;
import ru.clevertec.web.command.impl.error.ErrorCommand;
import ru.clevertec.web.util.json_parser.CustomParserAdapter;
import ru.clevertec.web.util.json_parser.JacksonAdapter;
import ru.clevertec.web.util.json_parser.JsonParser;
import ru.clevertec.web.util.paging.PagingUtil;
import ru.clevertec.web.validator.Validator;
import ru.clevertec.web.validator.impl.CustomerValidator;

@Log4j2
public class BeanFactory implements Closeable {

    public final static BeanFactory INSTANCE = new BeanFactory();
    public static final String CUSTOMERS = "customers";
    public static final String ERROR = "error";
    private final Map<Class<?>, Object> beans;
    private final Map<String, Object> commands;
    private final List<Closeable> closeables;

    private BeanFactory() {
        this.beans = new HashMap<>();
        this.commands = new HashMap<>();
        this.closeables = new ArrayList<>();
        try {
            init();
        } catch (Exception e) {
            log.error(e);
        }
    }

    private void init() throws CommandExecutionException {
// data
        ConfigManager configManager = new ConfigManager("/application.yml");
        beans.put(ConfigManager.class, configManager);
        DataSource dataSource = new DataSource(configManager);
        closeables.add(dataSource);

        // repository, cache
        CustomerRepository customerRepository = new CustomerRepositoryImpl(dataSource);
        @SuppressWarnings("unchecked")
        Map<String, Object> cacheProps = (Map<String, Object>) configManager.getProperty("cache");
        String cacheType = (String) cacheProps.get("type");
        int expirationTime = (int) cacheProps.get("expirationTime");
        int cacheSize = (int) cacheProps.get("size");
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
        JsonParser jsonParser;
        if (parserType.equalsIgnoreCase("jackson")) {
            jsonParser = new JacksonAdapter();
        } else {
            jsonParser = new CustomParserAdapter();
        }

        // liquibase
        @SuppressWarnings("unchecked")
        Map<String, Object> liquibaseProps = (Map<String, Object>) configManager.getProperty("liquibase");
        boolean isRequiredInitialization = (boolean) liquibaseProps.get("init-db");
        if (isRequiredInitialization) {
            initDb(configManager);
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
        CustomerService customerService = new CustomerServiceImpl(customerRepository, customerMapper, converter, formatter, jsonParser);

// web
        @SuppressWarnings("unchecked")
        Map<String, Integer> pagination = (Map<String, Integer>) configManager.getProperty("pagination");
        int defaultPageSize = pagination.get("defaultSize");
        PagingUtil pagingUtil = new PagingUtil(defaultPageSize);
        Validator<CustomerDto> customerDtoValidator = new CustomerValidator();
        CustomerController customerController = new CustomerController(customerService, pagingUtil, customerDtoValidator, jsonParser);
        commands.put(CUSTOMERS, customerController);
        commands.put(ERROR, new ErrorCommand(jsonParser));
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<?> clazz) {
        return (T) beans.get(clazz);
    }

    @SuppressWarnings("unchecked")
    public <T> T getCommand(String name) {
        return (T) commands.get(name);
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

    private void initDb(ConfigManager configManager) throws CommandExecutionException {
        @SuppressWarnings("unchecked")
        Map<String, Object> dbPropsMap = (Map<String, Object>) configManager.getProperty("db");
        String url = (String) dbPropsMap.get("url");
        String userName = (String) dbPropsMap.get("user");
        String password = (String) dbPropsMap.get("password");
        @SuppressWarnings("unchecked")
        Map<String, String> liquibaseProps = (Map<String, String>) configManager.getProperty("liquibase");
        String changeLogFile = liquibaseProps.get("changeLogFile");
        CommandScope dropAll = new CommandScope("dropAll");
        dropAll.addArgumentValue("url", url);
        dropAll.addArgumentValue("username", userName);
        dropAll.addArgumentValue("password", password);
        dropAll.execute();
        CommandScope updateCommand = new CommandScope("update");
        updateCommand.addArgumentValue("url", url);
        updateCommand.addArgumentValue("username", userName);
        updateCommand.addArgumentValue("password", password);
        updateCommand.addArgumentValue("changeLogFile", changeLogFile);
        updateCommand.execute();
    }
}
