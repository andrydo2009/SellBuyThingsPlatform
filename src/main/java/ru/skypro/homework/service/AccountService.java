package ru.skypro.homework.service;

import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.account.NewPassword;
import ru.skypro.homework.dto.account.User;

import java.io.IOException;

public interface AccountService {
    /**
     * Обновление пароля пользователя
     * @param newPassword объект, содержащий текущий и новый пароли пользователя
     * @return возвращает {@code true}, если пароль успешно изменён, или {@code false}, если новый пароль некорректен
     */
    boolean updatePassword(NewPassword newPassword);

    /**
     * Получение данных пользователя
     * @return пользователь
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException если пользователь с таким логином не найден в базе данных
     */
    User getInfoAboutUser();

    /**
     * Обновление данных пользователя
     * @param user пользователь
     * @return обновлённый пользователь
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException если пользователь с таким логином не найден в базе данных
     */
    User patchInfoAboutUser(User user);

    /**
     * Обновление аватара пользователя
     * @param image файл картинки
     * @return {@code true}, если аватар успешно обновлён
     * @throws IOException ошибка ввода-вывода
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException если текущий пользователь не найден в базе данных
     */
    boolean updateUserAvatar(MultipartFile image) throws IOException;

    /**
     * Загрузка аватара пользователя из файловой системы по его id
     *
     * @param userId   id пользователя
     * @throws IOException ошибка ввода-вывода
     */
    byte[] downloadAvatarFromFS(int userId) throws IOException;
}
