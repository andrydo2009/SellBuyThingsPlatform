package ru.skypro.homework.service.impl;

import org.springframework.stereotype.Component;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.security.SecurityUserDto;
import ru.skypro.homework.service.SecurityUserMapper;

@Component
public class SecurityUserMapperImpl implements SecurityUserMapper {
    @Override
    public SecurityUserDto toSecurityUserDto(UserEntity userEntity) {
        SecurityUserDto securityUserDto = new SecurityUserDto();
        securityUserDto.setId(userEntity.getId());
        securityUserDto.setUserName(userEntity.getEmail());
        securityUserDto.setPassword(userEntity.getPassword());
        securityUserDto.setRole(userEntity.getRole());
        return securityUserDto;
    }
}

