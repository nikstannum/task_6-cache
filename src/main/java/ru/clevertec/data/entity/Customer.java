package ru.clevertec.data.entity;

import java.time.LocalDate;
import lombok.Data;

@Data
public class Customer {
    private Long id;
    private String firstName;
    private String lastName;
    private LocalDate dateBirth;
    private String email;
    private boolean deleted;
}
