package ru.clevertec.service.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class CustomerDto {
    private Long id;
    private String firstName;
    private String lastName;
    private LocalDate dateBirth;
    private String email;
}
