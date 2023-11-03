package ru.skypro.homework.security;

import lombok.Data;
import ru.skypro.homework.dto.account.Role;

@Data
public class SecurityUserDto {
    private int id;
    private String userName;
    private String password;
    private Role role;
}

