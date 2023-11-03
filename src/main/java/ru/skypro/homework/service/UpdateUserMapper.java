package ru.skypro.homework.service;

import ru.skypro.homework.dto.account.UpdateUser;
import ru.skypro.homework.dto.account.User;

public interface UpdateUserMapper {
    UpdateUser toUpdateUser(User user);
}
