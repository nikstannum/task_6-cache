package ru.clevertec.data.cache.impl;

import java.util.Deque;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import ru.clevertec.data.cache.Cache;

/**
 * Implementation of the LRU Cache.
 * To ensure thread safety when used in a multi-threaded environment, thread-safe collections and locks are used.
 */
public class LRUCacheImpl implements Cache {

    private final Map<String, Object> map;
    private final Deque<String> keyList;
    private final Map<String, Timer> timers;
    private final Lock lock = new ReentrantLock(true);
    private final int cacheSize;
    private final int expirationTime;

    public LRUCacheImpl(int cacheSize, int expirationTime) {
        this.cacheSize = cacheSize;
        this.expirationTime = expirationTime;
        this.timers = new ConcurrentHashMap<>(cacheSize);
        this.map = new ConcurrentHashMap<>();
        this.keyList = new ConcurrentLinkedDeque<>();
    }

    /**
     * Checks the presence of an object in the cache by key
     *
     * @param key
     * @param cacheName
     * @return true if existing
     */
    @Override
    public boolean isContains(String key, String cacheName) {
        String compositeId = key + ":" + cacheName;
        return map.containsKey(compositeId);
    }

    /**
     * Method for placing an object in the cache.
     *
     * @param key       computed based on expression language
     * @param cacheName the name given to the cached object. Used when compiling a composite key to access a cached object
     * @param value     the object itself
     */
    @Override
    public void put(String key, String cacheName, Object value) {
        String compositeId = key + ":" + cacheName;
        lock.lock();
        if (map.containsKey(compositeId)) {
            moveToFirst(compositeId, value);
            lock.unlock();
            return;
        }
        if (keyList.size() == cacheSize) {
            String forRemove = keyList.removeLast();
            map.remove(forRemove);
        }
        keyList.addFirst(compositeId);
        map.put(compositeId, value);
        scheduleRemoval(compositeId);
        lock.unlock();
    }

    /**
     * Method for removing an object from the cache.
     *
     * @param key       computed based on expression language
     * @param cacheName the name given to the cached object. Used when compiling a composite key to access a cached object
     */
    @Override
    public void delete(String key, String cacheName) {
        String compositeId = key + ":" + cacheName;
        lock.lock();
        if (map.containsKey(compositeId)) {
            deleteTimer(compositeId);
            keyList.remove(compositeId);
            map.remove(compositeId);
        }
        lock.unlock();
    }


    /**
     * Method for getting an object from the cache.
     *
     * @param key       computed based on expression language
     * @param cacheName the name given to the cached object. Used when compiling a composite key to access a cached object
     * @return the object itself
     */
    @Override
    public Object take(String key, String cacheName) {
        String compositeId = key + ":" + cacheName;
        lock.lock();
        Object value = map.get(compositeId);
        if (value != null) {
            moveToFirst(compositeId, value);
            lock.unlock();
        }
        return value;
    }

    private void moveToFirst(String compositeId, Object value) {
        deleteTimer(compositeId);
        keyList.remove(compositeId);
        keyList.addFirst(compositeId);
        map.put(compositeId, value);
        scheduleRemoval(compositeId);
    }

    private void deleteTimer(String compositeId) {
        Timer timer = timers.get(compositeId);
        if (timer != null) {
            timer.cancel();
            timers.remove(compositeId);
        }
    }

    private void scheduleRemoval(String compositeId) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                map.remove(compositeId);
                timers.remove(compositeId);
                timer.cancel();
            }
        }, expirationTime * 1000L);
        timers.put(compositeId, timer);
    }

    @Override
    public void close() {
        timers.forEach((key, value) -> {
            deleteTimer(key);
            map.remove(key);
            keyList.remove(key);
        });
    }
}
