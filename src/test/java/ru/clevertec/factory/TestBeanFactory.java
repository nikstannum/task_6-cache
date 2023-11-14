package ru.clevertec.factory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.clevertec.data.cache.impl.LFUCacheImpl;
import ru.clevertec.data.cache.impl.LRUCacheImpl;
import ru.clevertec.data.connection.ConfigManager;

public class TestBeanFactory implements Closeable {
    public final static TestBeanFactory INSTANCE = new TestBeanFactory();
    private final Map<Class<?>, Object> beans;
    private final List<Closeable> closeables;

    private TestBeanFactory() {
        this.beans = new HashMap<>();
        this.closeables = new ArrayList<>();
        try {
            init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void init() {
        // data
        ConfigManager configManager = new ConfigManager("/application.yml");
        beans.put(ConfigManager.class, configManager);
        @SuppressWarnings("unchecked")
        Map<String, Object> cacheProps = (Map<String, Object>) configManager.getProperty("test-cache");
        int expirationTime = (int) cacheProps.get("expirationTime");
        int cacheSize = (int) cacheProps.get("size");
        LRUCacheImpl lruCache = new LRUCacheImpl(cacheSize, expirationTime);
        closeables.add(lruCache);
        beans.put(LRUCacheImpl.class, lruCache);
        LFUCacheImpl lfuCache = new LFUCacheImpl(cacheSize, expirationTime);
        closeables.add(lfuCache);
        beans.put(LFUCacheImpl.class, lfuCache);
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