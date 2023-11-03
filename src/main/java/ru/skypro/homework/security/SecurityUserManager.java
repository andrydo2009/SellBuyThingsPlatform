package ru.skypro.homework.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.SecurityUserMapper;

@Service
public class SecurityUserManager implements UserDetailsManager {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUserMapper securityUserMapper;
    private final SecurityUser securityUser;

    public SecurityUserManager(UserRepository userRepository,
                               PasswordEncoder passwordEncoder,
                               SecurityUserMapper securityUserMapper,
                               SecurityUser securityUser) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.securityUserMapper = securityUserMapper;
        this.securityUser = securityUser;
    }

    @Override
    public void createUser(UserDetails user) {
        //no usages
    }

    @Override
    public void updateUser(UserDetails user) {
        //no usages
    }

    @Override
    public void deleteUser(String username) {
        //no usages
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        if (passwordEncoder.matches(oldPassword, securityUser.getPassword())) {
            UserEntity userEntity = userRepository.findByEmail(securityUser.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            userEntity.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(userEntity);
        }
    }

    @Override
    public boolean userExists(String username) {
        return userRepository.existsByEmail(username);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SecurityUserDto securityUserDto = userRepository.findByEmail(username)
                .map(securityUserMapper::toSecurityUserDto)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        securityUser.setSecurityUserDto(securityUserDto);
        return securityUser;
    }
}

