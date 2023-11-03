package ru.skypro.homework.service;

import ru.skypro.homework.dto.account.Register;
import ru.skypro.homework.dto.account.User;
import ru.skypro.homework.model.UserEntity;

public interface UserMapper {
    UserEntity toUserEntity(Register register);

    User toUser(UserEntity userEntity);

    UserEntity updateUserEntity(UserEntity userEntity, User user);
}
