package ru.clevertec.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.clevertec.data.entity.Customer;
import ru.clevertec.service.dto.CustomerDto;

@Mapper
public interface CustomerMapper {
    CustomerMapper INSTANCE = Mappers.getMapper(CustomerMapper.class);

    CustomerDto convert(Customer customer);

    Customer convert(CustomerDto customerDto);
}
