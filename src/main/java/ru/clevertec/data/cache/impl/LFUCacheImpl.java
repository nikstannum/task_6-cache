package ru.clevertec.data.cache.impl;

import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import ru.clevertec.data.cache.Cache;

/**
 * Implementation of the LFU Cache. When performing operations with the cache, the time spent by objects in the cache is checked. If the time spent by objects exceeds the set value, then these objects are removed from the cache.
 */
public class LFUCacheImpl implements Cache {

    private final Map<String, Object> map;
    private final Map<String, Timer> timers;
    private final ConcurrentNavigableMap<CacheElm, String> sortedMap;
    private final Lock lock = new ReentrantLock(true);
    private final int cacheSize;
    private final int expirationTime;

    public LFUCacheImpl(int cacheSize, int expirationTime) {
        this.cacheSize = cacheSize;
        this.expirationTime = expirationTime;
        this.timers = new ConcurrentHashMap<>(this.cacheSize);
        this.map = new ConcurrentHashMap<>(this.cacheSize);
        this.sortedMap = new ConcurrentSkipListMap<>((o1, o2) -> {
            Integer quantity1 = o1.getQuantityUse();
            Integer quantity2 = o2.getQuantityUse();
            if (!quantity1.equals(quantity2)) {
                return quantity1.compareTo(quantity2);
            }
            return o1.toString().compareTo(o2.toString());
        });
    }

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
            setUpdatedCacheInf(compositeId, value);
            lock.unlock();
            return;
        }
        if (cacheSize == map.size()) {
            removeElm();
        }
        putNewCacheElm(compositeId, value);
        lock.unlock();
    }

    private void scheduleRemoval(String compositeId) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                CacheElm elm = (CacheElm) map.remove(compositeId);
                sortedMap.remove(elm);
                timers.remove(compositeId);
                timer.cancel();
            }
        }, expirationTime * 1000L);
        timers.put(compositeId, timer);
    }

    private void setUpdatedCacheInf(String compositeId, Object value) {
        deleteTimer(compositeId);
        CacheElm cacheElm = (CacheElm) map.get(compositeId);
        cacheElm.setQuantityUse(cacheElm.getQuantityUse() + 1);
        cacheElm.setValue(value);
        scheduleRemoval(compositeId);
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
            Object obj = map.remove(compositeId);
            if (obj == null) {
                lock.unlock();
                return;
            }
            CacheElm cacheElm = (CacheElm) obj;
            sortedMap.remove(cacheElm);
        }
        lock.unlock();
    }

    private void deleteTimer(String compositeId) {
        Timer timer = timers.get(compositeId);
        if (timer != null) {
            timer.cancel();
            timers.remove(compositeId);
        }
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
        CacheElm cacheElm = (CacheElm) map.get(compositeId);
        if (Objects.isNull(cacheElm)) {
            lock.unlock();
            return null;
        }
        Object obj = cacheElm.getValue();
        setUpdatedCacheInf(compositeId, obj);
        lock.unlock();
        return obj;
    }

    private void removeElm() {
        CacheElm firstKeyForRemove = sortedMap.firstKey();
        String compositeId = sortedMap.remove(firstKeyForRemove);
        map.remove(compositeId);
        deleteTimer(compositeId);
    }

    private void putNewCacheElm(String compositeId, Object value) {
        CacheElm cacheElm = new CacheElm();
        cacheElm.setQuantityUse(1);
        cacheElm.setValue(value);
        map.put(compositeId, cacheElm);
        sortedMap.put(cacheElm, compositeId);
        scheduleRemoval(compositeId);
    }

    @Override
    public void close() {
        timers.forEach((key, value) -> {
            deleteTimer(key);
            CacheElm cacheElm = (CacheElm) map.remove(key);
            sortedMap.remove(cacheElm);
        });
    }
}