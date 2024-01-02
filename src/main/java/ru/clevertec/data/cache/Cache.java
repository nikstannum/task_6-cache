package ru.clevertec.data.cache;

/**
 * Application cache interface. Implementation classes: {@link ru.clevertec.data.cache.impl.LRUCacheImpl},
 * {@link ru.clevertec.data.cache.impl.LFUCacheImpl}
 */
public interface Cache {

    /**
     * Method for placing an object in the cache.
     *
     * @param key       computed based on expression language
     * @param cacheName the name given to the cached object. Used when compiling a composite key to access a cached object
     * @param value     the object itself
     */
    void put(String key, String cacheName, Object value);

    /**
     * Method for removing an object from the cache.
     *
     * @param key       computed based on expression language
     * @param cacheName the name given to the cached object. Used when compiling a composite key to access a cached object
     */
    void delete(String key, String cacheName);

    /**
     * Method for getting an object from the cache.
     *
     * @param key       computed based on expression language
     * @param cacheName the name given to the cached object. Used when compiling a composite key to access a cached object
     * @return the object itself
     */
    Object take(String key, String cacheName);

    boolean isContains(String key, String cacheName);

    void close();
}
