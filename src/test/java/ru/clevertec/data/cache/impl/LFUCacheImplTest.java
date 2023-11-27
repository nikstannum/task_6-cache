package ru.clevertec.data.cache.impl;

import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.clevertec.data.connection.ConfigManager;
import ru.clevertec.data.entity.Customer;
import ru.clevertec.factory.TestBeanFactory;

import static org.assertj.core.api.Assertions.assertThat;

class LFUCacheImplTest {

    public static final String SOME_CACHE_NAME = "cacheName";
    private LFUCacheImpl cache;
    private TestBeanFactory factory;

    @BeforeEach
    void setUp() {
        factory = TestBeanFactory.INSTANCE;
        cache = factory.getBean(LFUCacheImpl.class);
    }

    @AfterEach
    void tearDown() {
        factory.close();
    }

    private Customer getCustomer() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("firstName");
        customer.setLastName("lastName");
        customer.setEmail("testEmail");
        customer.setDateBirth(LocalDate.now());
        customer.setDeleted(false);
        return customer;
    }

    @Test
    void checkIsContainsShouldReturnTrue() {
        // given
        Customer customer1 = getCustomer();
        cache.put(customer1.getId().toString(), SOME_CACHE_NAME, customer1);
        // when
        boolean actual = cache.isContains(customer1.getId().toString(), SOME_CACHE_NAME);
        // then
        assertThat(actual).isTrue();
    }

    @Test
    void checkIsContainsWhenExpirationTimeFinishShouldReturnFalse() throws InterruptedException {
        // given
        Customer customer1 = getCustomer();
        cache.put(customer1.getId().toString(), SOME_CACHE_NAME, customer1);
        ConfigManager configManager = factory.getBean(ConfigManager.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> cacheProps = (Map<String, Object>) configManager.getProperty("test-cache");
        int expirationTime = (int) cacheProps.get("expirationTime");
        long wait = expirationTime * 1100L;
        // when
        Thread.sleep(wait);
        boolean actual = cache.isContains(customer1.getId().toString(), SOME_CACHE_NAME);
        // then
        assertThat(actual).isFalse();
    }

    @Test
    void checkIsContainsShouldReturnFalse() {
        // given
        Customer customer1 = getCustomer();
        // when
        boolean actual = cache.isContains(customer1.getId().toString(), SOME_CACHE_NAME);
        // then
        assertThat(actual).isFalse();
    }

    @Test
    void delete() {
        // given
        Customer customer1 = getCustomer();
        cache.put(customer1.getId().toString(), SOME_CACHE_NAME, customer1);
        // when
        cache.delete(customer1.getId().toString(), SOME_CACHE_NAME);
        // then
        boolean actual = cache.isContains(customer1.getId().toString(), SOME_CACHE_NAME);
        assertThat(actual).isFalse();
    }

    @Test
    void checkTakeWithInvalidKeyShouldReturnNull() {
        // given
        Customer customer1 = getCustomer();
        cache.put(customer1.getId().toString(), SOME_CACHE_NAME, customer1);
        // when
        Object actual = cache.take("invalidKey", SOME_CACHE_NAME);
        // then
        assertThat(actual).isNull();
    }

    @Test
    void checkTakeFromWithOtherCacheNameShouldReturnNull() {
        // given
        Customer customer1 = getCustomer();
        cache.put(customer1.getId().toString(), SOME_CACHE_NAME, customer1);
        // when
        Object actual = cache.take(customer1.getId().toString(), "otherCacheName");
        // then
        assertThat(actual).isNull();
    }

    @Test
    void checkTakeShouldReturnEquals() {
        // given
        Customer customer1 = getCustomer();
        cache.put(customer1.getId().toString(), SOME_CACHE_NAME, customer1);
        // when
        Object actual = cache.take(customer1.getId().toString(), SOME_CACHE_NAME);
        // then
        assertThat(actual).isEqualTo(customer1);
    }

    @Test
    void close() {
        Customer customer1 = getCustomer();
        cache.put(customer1.getId().toString(), SOME_CACHE_NAME, customer1);
        cache.close();
        boolean actual = cache.isContains(customer1.getId().toString(), SOME_CACHE_NAME);
        assertThat(actual).isFalse();
    }
}