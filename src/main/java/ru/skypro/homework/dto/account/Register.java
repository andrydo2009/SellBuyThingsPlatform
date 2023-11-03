package ru.skypro.homework.dto.account;

import lombok.Data;

@Data
public class Register {
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;
    private Role role;
}
