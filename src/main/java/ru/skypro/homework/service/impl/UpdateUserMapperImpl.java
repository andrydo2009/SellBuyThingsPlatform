package ru.skypro.homework.service.impl;

import ru.skypro.homework.dto.account.UpdateUser;
import ru.skypro.homework.dto.account.User;
import ru.skypro.homework.service.UpdateUserMapper;

public class UpdateUserMapperImpl implements UpdateUserMapper {
    @Override
    public UpdateUser toUpdateUser(User user) {
        UpdateUser updateUser = new UpdateUser();
        updateUser.setFirstName(user.getFirstName());
        updateUser.setLastName(user.getLastName());
        updateUser.setPhone(user.getPhone());
        return updateUser;
    }
}
