package ru.clevertec.web.controller.customer;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.clevertec.exception.BadRequestException;
import ru.clevertec.exception.ValidationException;
import ru.clevertec.service.CustomerService;
import ru.clevertec.service.dto.CustomerDto;
import ru.clevertec.web.util.json_parser.JsonParser;
import ru.clevertec.web.util.paging.PagingUtil;
import ru.clevertec.web.util.paging.PagingUtil.Paging;
import ru.clevertec.web.validator.Validator;
import ru.clevertec.web.validator.Validator.Result;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/customers")
public class CustomerController {

    private static final String EXC_MSG_ID_NOT_MATCH = "Incoming id in body doesn't match path";

    private final CustomerService customerService;
    private final Validator<CustomerDto> validator;
    private final PagingUtil pagingUtil;
    private final JsonParser jsonParser;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> create(@RequestBody CustomerDto customerDto) {
        Map<Result, List<String>> validationResult = validator.validate(customerDto);
        if (validationResult.containsKey(Result.FAIL)) {
            throw new ValidationException(String.valueOf(validationResult.remove(Result.FAIL)));
        }
        CustomerDto created = customerService.create(customerDto);
        return buildResponseCreated(created);
    }

    private ResponseEntity<String> buildResponseCreated(CustomerDto created) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(getLocation(created))
                .body(jsonParser.write(created));
    }

    private URI getLocation(CustomerDto created) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path("api/customers/{id}")
                .buildAndExpand(created.getId())
                .toUri();
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> findAll(@RequestParam Integer page, @RequestParam Integer size) {
        Paging paging = pagingUtil.getPaging(page, size);
        List<CustomerDto> list = customerService.findAll(paging);
        return new ResponseEntity<>(jsonParser.write(list), HttpStatus.OK);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> findById(@PathVariable(value = "id") Long id) {
        CustomerDto dto = customerService.findById(id);
        return new ResponseEntity<>(jsonParser.write(dto), HttpStatus.OK);
    }


    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> update(@RequestBody CustomerDto customerDto, @PathVariable Long id) {
        if (!Objects.equals(id, customerDto.getId())) {
            throw new BadRequestException(EXC_MSG_ID_NOT_MATCH);
        }
        Map<Result, List<String>> validationResult = validator.validate(customerDto);
        if (validationResult.containsKey(Result.FAIL)) {
            throw new ValidationException(String.valueOf(validationResult.remove(Result.FAIL)));
        }
        CustomerDto updated = customerService.update(customerDto);
        return new ResponseEntity<>(jsonParser.write(updated), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long id) {
        customerService.deleteById(id);
    }

}
