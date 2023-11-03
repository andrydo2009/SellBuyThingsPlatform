package ru.skypro.homework.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.account.NewPassword;
import ru.skypro.homework.dto.account.User;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.AccountService;
import ru.skypro.homework.service.FileService;
import ru.skypro.homework.service.UserMapper;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Класс для осуществления операций с базой данных пользователей
 */
@Slf4j
@Service
public class AccountServiceImpl implements AccountService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserDetails userDetails;
    private final UserDetailsManager userDetailsManager;
    private final FileService fileService;

    public AccountServiceImpl(UserRepository userRepository, UserMapper userMapper, UserDetails userDetails, UserDetailsManager userDetailsManager, FileService fileService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.userDetails = userDetails;
        this.userDetailsManager = userDetailsManager;
        this.fileService = fileService;
    }

    private static final String USER_NOT_FOUND = "User not found";

    @Value("${users.avatar.dir.path}")
    private String avatarsDir;

    /**
     * Обновление пароля текущего пользователя. На вход передаётся объект класса
     * <b>NewPassword</b>,
     * происходит проверка корректности нового и текущего паролей,
     * сохраняется новый пароль для текущего пользователя.
     * Используется метод класса {@link UserDetailsManager#changePassword(String, String)}
     * Используются методы класса {@link NewPassword#getNewPassword()}
     * Используются методы класса {@link NewPassword#getCurrentPassword()}
     * @param newPassword объект, содержащий текущий и новый пароли пользователя
     * @return возвращает {@code true}, если пароль успешно изменён, или {@code false}, если новый пароль некорректен
     * @see UserDetails#getUsername()
     */
    @Override
    @Transactional
    public boolean updatePassword(NewPassword newPassword) {
        if (newPassword.getNewPassword() != null &&
                !newPassword.getNewPassword().isEmpty() &&
                !newPassword.getNewPassword().isBlank() &&
                newPassword.getCurrentPassword() != null &&
                !newPassword.getCurrentPassword().isEmpty() &&
                !newPassword.getCurrentPassword().isBlank()) {
            userDetailsManager.changePassword(newPassword.getCurrentPassword(), newPassword.getNewPassword());
            log.info("Password for user: {} was changed successfully.", userDetails.getUsername());
            return true;
        }
        log.warn("New password is incorrect for user: {}.", userDetails.getUsername());
        return false;
    }

    /**
     * Получение информации о пользователе из базы данных путём
     * получения имени пользователя (логина) из объекта класса <b>UserDetails</b>.
     * <br>Используется метод {@link UserDetails#getUsername()}
     * <br>Для получения пользователя используется метод класса
     * {@link UserRepository#findByEmail(String)}
     * @return Пользователь
     * @throws UsernameNotFoundException если пользователь с таким логином не найден в базе данных
     */
    @Override
    @Transactional(readOnly = true)
    public User getInfoAboutUser() {
        String userName = userDetails.getUsername();
        log.info("Information about user: {} was received.", userName);
        return userMapper.toUser(userRepository.findByEmail(userName)
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND)));
    }

    /**
     * Обновление данных пользователя. Логин пользователя получается из объекта
     * класса <b>UserDetails</b> методом {@link UserDetails#getUsername()}
     * <br> Получение пользователя из базы данных происходит в методе
     * {@link UserRepository#findByEmail(String)}
     * <br> Сохранение пользователя происходит в методе {@link UserRepository#save(Object)}
     * @param user Данные пользователя из веб-интерфейса
     * @return обновлённый пользователь
     * @throws UsernameNotFoundException если пользователь с таким логином не найден в базе данных
     * @see UserMapper#updateUserEntity(UserEntity, User)
     */
    @Override
    @Transactional
    public User patchInfoAboutUser(User user) {
        String userName = userDetails.getUsername();
        UserEntity userEntity = userRepository.findByEmail(userName)
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND));
        UserEntity updatedUser = userRepository.save(userMapper.updateUserEntity(userEntity, user));
        log.info("Information about user: {} was updated successfully.", userName);
        return userMapper.toUser(updatedUser);
    }

    /**
     * Обновление аватара пользователя. Используется метод
     * <br> <b>uploadImage(image, filePath)</b>. Логин пользователя получается из объекта
     * класса <b>UserDetails</b> методом {@link UserDetails#getUsername()}
     * <br> Получение пользователя из базы данных происходит в методе
     * {@link UserRepository#findByEmail(String)}
     * @param image файл картинки
     * @return {@code true}, если аватар успешно обновлён
     * @throws IOException ошибка ввода-вывода
     * @throws UsernameNotFoundException если текущий пользователь не найден в базе данных
     * @see Files#deleteIfExists(Path)
     * @see UserEntity#getImagePath()
     * @see Path#of(String, String...)
     * @see StringUtils#getFilenameExtension(String)
     */
    @Override
    @Transactional
    public boolean updateUserAvatar(MultipartFile image) throws IOException {
        String userName = userDetails.getUsername();
        UserEntity userEntity = userRepository.findByEmail(userName)
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND));
        if (userEntity.getImagePath() != null) {
            Files.deleteIfExists(Path.of(userEntity.getImagePath()));
        }
        Path filePath = Path.of(avatarsDir, userEntity.getId() + "."
                + StringUtils.getFilenameExtension(image.getOriginalFilename()));
        fileService.uploadImage(image, filePath);
        userEntity.setImagePath(filePath.toAbsolutePath().toString());
        userRepository.save(userEntity);
        log.info("Avatar for user: {} was updated successfully.", userName);
        return true;
    }

    /**
     * Загрузка аватара из файловой системы по id пользователя. <br> Используется метод
     * {@link UserRepository#findById(Object)} для получения пользователя из базы данных.
     * Для формирования ответа сервера используется метод
     * {@link FileService#downloadImage(String)}
     * @param userId id пользователя
     * @return image - массив байт картинки
     * @throws IOException ошибка ввода-вывода
     * @throws UsernameNotFoundException если пользователь с данным id не найден в базе данных
     * @see #downloadAvatarFromFS(int)
     */
    @Override
    @Transactional
    public byte[] downloadAvatarFromFS(int userId) throws IOException {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND));
        if (userEntity.getImagePath() != null) {
            byte[] image = fileService.downloadImage(userEntity.getImagePath());
            log.info("Download avatar for user: {} method was invoked", userEntity.getEmail());
            return image;
        }
        return new byte[0];
    }

}
