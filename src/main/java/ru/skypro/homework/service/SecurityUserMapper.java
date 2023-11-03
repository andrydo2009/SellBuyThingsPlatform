package ru.skypro.homework.service;

import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.security.SecurityUserDto;

public interface SecurityUserMapper {
    SecurityUserDto toSecurityUserDto(UserEntity userEntity);
}

