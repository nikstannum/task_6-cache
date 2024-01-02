package ru.clevertec.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.clevertec.data.entity.Customer;
import ru.clevertec.service.dto.CustomerDto;

@Mapper
public interface CustomerMapper {
    CustomerDto convert(Customer customer);

    @Mapping(target = "deleted", ignore = true)
    Customer convert(CustomerDto customerDto);
}
