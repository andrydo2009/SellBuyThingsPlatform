package ru.skypro.homework.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.skypro.homework.dto.account.Register;
import ru.skypro.homework.dto.account.User;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.service.UserMapper;

@Component
public class UserMapperImpl implements UserMapper {
    private final PasswordEncoder passwordEncoder;

    public UserMapperImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserEntity toUserEntity(Register register) {
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(register.getUsername());
        userEntity.setPassword(passwordEncoder.encode(register.getPassword()));
        userEntity.setFirstName(register.getFirstName());
        userEntity.setLastName(register.getLastName());
        userEntity.setPhoneUser(register.getPhone());
        userEntity.setRole(register.getRole());
        return userEntity;
    }

    @Override
    public User toUser(UserEntity userEntity) {
        User user = new User();
        user.setId(userEntity.getId());
        user.setEmail(userEntity.getEmail());
        user.setFirstName(userEntity.getFirstName());
        user.setLastName(userEntity.getLastName());
        user.setPhone(userEntity.getPhoneUser());
        user.setRole(userEntity.getRole());
        user.setImage("/users/image/" + userEntity.getId());
        return user;
    }

    @Override
    public UserEntity updateUserEntity(UserEntity userEntity, User user) {
        userEntity.setFirstName(user.getFirstName());
        userEntity.setLastName(user.getLastName());
        userEntity.setPhoneUser(user.getPhone());
        return userEntity;
    }
}
