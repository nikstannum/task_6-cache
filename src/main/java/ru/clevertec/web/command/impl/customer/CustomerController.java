package ru.clevertec.web.command.impl.customer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import ru.clevertec.exception.ValidationException;
import ru.clevertec.service.CustomerService;
import ru.clevertec.service.dto.CustomerDto;
import ru.clevertec.web.command.Command;
import ru.clevertec.web.util.json_parser.JsonParser;
import ru.clevertec.web.util.paging.PagingUtil;
import ru.clevertec.web.util.paging.PagingUtil.Paging;
import ru.clevertec.web.validator.Validator;
import ru.clevertec.web.validator.Validator.Result;

@RequiredArgsConstructor
public class CustomerController implements Command {

    private static final String GET = "GET";
    private static final String DELETE = "DELETE";
    private static final String POST = "POST";
    private static final String PUT = "PUT";
    private static final String URI_DIVIDER = "/";

    private static final int CODE_OK = 200;
    private static final int CODE_CREATED = 201;
    private static final int CODE_NO_CONTENT = 204;
    public static final String HEADER_LOCATION = "Location";

    private final CustomerService customerService;
    private final PagingUtil pagingUtil;
    private final Validator<CustomerDto> validator;
    private final JsonParser jsonParser;

    CustomerDto create(CustomerDto customerDto) {
        Map<Result, List<String>> validationResult = validator.validate(customerDto);
        if (validationResult.containsKey(Result.FAIL)) {
            throw new ValidationException(String.valueOf(validationResult.remove(Result.FAIL)));
        }
        return customerService.create(customerDto);
    }

    List<CustomerDto> findAll(Paging paging) {
        return customerService.findAll(paging);
    }

    CustomerDto findById(Long id) {
        return customerService.findById(id);
    }

    CustomerDto update(CustomerDto customerDto) {
        Map<Result, List<String>> validationResult = validator.validate(customerDto);
        if (validationResult.containsKey(Result.FAIL)) {
            throw new ValidationException(String.valueOf(validationResult.remove(Result.FAIL)));
        }
        return customerService.update(customerDto);
    }

    void deleteById(Long id) {
        customerService.deleteById(id);
    }

    @Override
    public String execute(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String method = req.getMethod();
        switch (method) {
            case POST -> {
                byte[] bytes = req.getInputStream().readAllBytes();
                CustomerDto result = create(jsonParser.read(Arrays.toString(bytes), CustomerDto.class));
                res.setStatus(CODE_CREATED);
                String location = req.getRequestURL().append("/").append(result.getId()).toString();
                res.setHeader(HEADER_LOCATION, location);
                return jsonParser.write(result);
            }
            case PUT -> {
                byte[] bytes = req.getInputStream().readAllBytes();
                CustomerDto result = update(jsonParser.read(Arrays.toString(bytes), CustomerDto.class));
                res.setStatus(CODE_OK);
                return jsonParser.write(result);
            }
            case GET -> {
                String uri = req.getRequestURI().substring(1);
                String[] parts = uri.split(URI_DIVIDER);
                if (parts.length == 2) {
                    String strId = parts[1];
                    res.setStatus(CODE_OK);
                    return jsonParser.write(findById(Long.parseLong(strId)));
                } else {
                    Paging paging = pagingUtil.getPaging(req);
                    res.setStatus(CODE_OK);
                    return jsonParser.write(findAll(paging));
                }
            }
            case DELETE -> {
                String uri = req.getRequestURI().substring(1);
                Long id = Long.parseLong(uri.split(URI_DIVIDER)[1]);
                deleteById(id);
                res.setStatus(CODE_NO_CONTENT);
                return null;
            }
        }
        return null;
    }
}
