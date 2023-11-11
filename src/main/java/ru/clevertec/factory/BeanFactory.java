package ru.clevertec.factory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.clevertec.data.CustomerRepository;
import ru.clevertec.data.connection.ConfigManager;
import ru.clevertec.data.connection.DataSource;
import ru.clevertec.data.impl.CustomerRepositoryImpl;

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

        CustomerRepository customerRepository = new CustomerRepositoryImpl(dataSource);
        beans.put(CustomerRepository.class, customerRepository); // FIXME delete

    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<?> clazz) {
        return (T) beans.get(clazz);
    }

    @Override
    public void close() throws IOException {
        for (Closeable closeable : closeables) {
            try {
                closeable.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
