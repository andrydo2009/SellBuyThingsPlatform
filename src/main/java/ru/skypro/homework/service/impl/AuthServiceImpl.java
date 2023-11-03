package ru.skypro.homework.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import ru.skypro.homework.dto.account.Register;
import ru.skypro.homework.dto.account.Role;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.AuthService;
import ru.skypro.homework.service.UserMapper;

import java.time.LocalDate;

import static ru.skypro.homework.dto.account.Role.USER;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    private final UserDetailsManager manager;
    private final PasswordEncoder encoder;
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    public AuthServiceImpl(UserDetailsManager manager, PasswordEncoder encoder,
                           UserMapper userMapper, UserRepository userRepository) {
        this.manager = manager;
        this.encoder = encoder;
        this.userMapper = userMapper;
        this.userRepository = userRepository;
    }

    /*
     * Проверяет правильность введенных учетных данных при попытке входа в систему.
     * @param userName Логин пользователя.
     * @param password Пароль пользователя.
     * @return <B>true</B>   если введенные учетные данные действительны, иначе <B>true</B>.
     * @see UserDetailsManager#userExists(String)
     * @see UserDetailsManager#loadUserByUsername(String)
     * @see PasswordEncoder#matches(CharSequence, String)
     * @see UserDetails#getPassword()
     */
    @Override
    public boolean login(String userName, String password) {
        if (!manager.userExists(userName)) {
            log.info("User does not exist. " + LocalDate.now());
            return false;
        }
        UserDetails userDetails = manager.loadUserByUsername(userName);
        log.info("User with username was successfully loaded. " + LocalDate.now());
        return encoder.matches(password, userDetails.getPassword());
    }

    /*
     * Регистрирует нового пользователя в системе.
     * @param register Объект, содержащий информацию о регистрации пользователя.
     * @return <B>true</B>  если регистрация прошла успешно, иначе <B>true</B> .
     * @see UserDetailsManager#userExists(String)
     * @see Register#getUsername()
     * @see Register#getRole()
     * @see Register#setRole(Role)
     * @see UserRepository#save(Object)
     * @see UserMapper#toUserEntity(Register)
     */
    @Override
    public boolean register(Register register) {
        if (manager.userExists(register.getUsername())) {
            log.info("User already exists. " + LocalDate.now());
            return false;
        }
        Role role = (register.getRole() == null) ? USER : register.getRole();
        register.setRole(role);
        userRepository.save(userMapper.toUserEntity(register));
        log.info("User was successfully registered. " + LocalDate.now());
        return true;
    }
}
