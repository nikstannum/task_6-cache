package ru.clevertec.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import ru.clevertec.data.CustomerRepository;
import ru.clevertec.data.connection.ConfigManager;
import ru.clevertec.data.entity.Customer;
import ru.clevertec.exception.NotFoundException;
import ru.clevertec.factory.BeanFactory;
import ru.clevertec.service.CustomerService;
import ru.clevertec.service.dto.CustomerDto;
import ru.clevertec.service.mapper.CustomerMapper;
import ru.clevertec.service.util.converter.Converter;
import ru.clevertec.service.util.formatter.Formatter;
import ru.clevertec.web.util.json_parser.JsonParser;
import ru.clevertec.web.util.paging.PagingUtil.Paging;

@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final Converter converter;
    private final Formatter formatter;
    private final JsonParser parser;

    @Override
    public CustomerDto create(CustomerDto customerDto) {
        Customer customer = customerMapper.convert(customerDto);
        Customer created = customerRepository.create(customer);
        return customerMapper.convert(created);
    }

    @Override
    public List<CustomerDto> findAll(Paging paging) {
        int limit = paging.limit();
        long offset = paging.offset();
        List<Customer> customers = customerRepository.findAll(limit, offset);
        return customers.stream().map(customerMapper::convert).toList();
    }

    @Override
    public CustomerDto findById(Long id) {
        CustomerDto dto = customerMapper.convert(customerRepository.findById(id)
                .orElseThrow(() -> {
                    String message = "Wasn't found customer with id = " + id;
                    print(message);
                    return new NotFoundException(message);
                }));
        String content = parser.write(dto);
        String formatted = formatter.format(content);
        print(formatted);
        return dto;
    }

    @Override
    public CustomerDto update(CustomerDto customerDto) {
        Customer customer = customerMapper.convert(customerDto);
        Customer updated = customerRepository.update(customer);
        return customerMapper.convert(updated);
    }

    @Override
    public void deleteById(Long id) {
        customerRepository.deleteById(id);
    }

    private void print(String content) {
        String destinationDir = getDestinationDir();
        String fileName = getFileName();
        FileOutputStream fos = null;
        try {
            File file = getFile(destinationDir, fileName);
            fos = new FileOutputStream(file);
            converter.convert(content, fos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            closeResources(Objects.requireNonNull(fos));
        }
    }

    private String getDestinationDir() {
        ConfigManager manager = BeanFactory.INSTANCE.getBean(ConfigManager.class);
        @SuppressWarnings("unchecked")
        Map<String, String> pdfProps = (Map<String, String>) manager.getProperty("pdf");
        return pdfProps.get("destinationDir");
    }

    private String getFileName() {
        LocalDateTime now = LocalDateTime.now();
        String format = "dd-hh-mm-ss";
        return now.format(DateTimeFormatter.ofPattern(format));
    }

    private File getFile(String destinationDir, String fileName) throws IOException {
        Path dir = Path.of(destinationDir);
        Files.createDirectories(dir);
        Path path = dir.resolve(fileName + ".pdf");
        return path.toFile();
    }

    private void closeResources(OutputStream os) {
        try {
            os.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
