package ru.clevertec;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import liquibase.command.CommandScope;
import liquibase.exception.CommandExecutionException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import ru.clevertec.data.CustomerRepository;
import ru.clevertec.data.cache.Cache;
import ru.clevertec.data.cache.impl.CacheConditional;
import ru.clevertec.data.cache.impl.LFUCacheImpl;
import ru.clevertec.data.cache.impl.LRUCacheImpl;
import ru.clevertec.data.cache.proxy.ProxyCustomerRepository;
import ru.clevertec.data.impl.CustomerRepositoryImpl;
import ru.clevertec.web.util.json_parser.CustomParserAdapter;
import ru.clevertec.web.util.json_parser.JacksonAdapter;
import ru.clevertec.web.util.json_parser.JsonParser;

@Log4j2
@Configuration
@ComponentScan
@PropertySource(value = {"classpath:application.yml", "classpath:application-${spring.profiles.active}.yml"}, factory = YmlPropSourceFactory.class)
public class ContextConfig {

    @Value("${db.url}")
    private String dbUrl;

    @Value("${db.password}")
    private String dbPassword;

    @Value("${db.user}")
    private String dbUser;

    @Value("${db.pool-size}")
    private String dbPoolSize;

    @Value("${db.driver}")
    private String dbDriver;

    @Value("${cache.type}")
    private String cacheType;

    @Value("${liquibase.init-db}")
    private boolean initDb;

    @Value("${liquibase.changeLogFile}")
    private String changeLogFile;

    @Bean
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(dbUrl);
        dataSource.setPassword(dbPassword);
        dataSource.setUsername(dbUser);
        dataSource.setMaximumPoolSize(Integer.parseInt(dbPoolSize));
        dataSource.setDriverClassName(dbDriver);
        return dataSource;
    }

    @Bean(destroyMethod = "close")
    @Conditional(CacheConditional.class)
    public Cache cache(@Value("${cache.size}") String cacheSize, @Value("${cache.expirationTime}") String expTime) {
        int size = Integer.parseInt(cacheSize);
        int time = Integer.parseInt(expTime);
        if ("LRU".equalsIgnoreCase(cacheType)) {
            return new LRUCacheImpl(size, time);
        } else {
            return new LFUCacheImpl(size, time);
        }
    }

    @Bean
    public CustomerRepository customerRepository(Cache cache) {
        if (initDb) {
            initDb();
        }
        CustomerRepository customerRepository = new CustomerRepositoryImpl(dataSource());
        if (cache != null) {
            return new ProxyCustomerRepository(customerRepository, cache);
        } else {
            return customerRepository;
        }
    }

    @Bean
    public JsonParser jsonParser(@Value("${json-parser}") String parserType) {
        if ("custom".equalsIgnoreCase(parserType)) {
            return new CustomParserAdapter();
        } else {
            return new JacksonAdapter();
        }
    }

    private void initDb() {
        try {
            CommandScope dropAll = new CommandScope("dropAll");
            dropAll.addArgumentValue("url", dbUrl);
            dropAll.addArgumentValue("username", dbUser);
            dropAll.addArgumentValue("password", dbPassword);
            dropAll.execute();
            CommandScope updateCommand = new CommandScope("update");
            updateCommand.addArgumentValue("url", dbUrl);
            updateCommand.addArgumentValue("username", dbUser);
            updateCommand.addArgumentValue("password", dbPassword);
            updateCommand.addArgumentValue("changeLogFile", changeLogFile);
            updateCommand.execute();
        } catch (CommandExecutionException e) {
            log.info("Couldn't init db using liquibase");
        }
    }
}
