package ru.skypro.homework.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.skypro.homework.dto.account.Role;
import ru.skypro.homework.model.AdEntity;
import ru.skypro.homework.model.CommentEntity;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.repository.CommentRepository;
import ru.skypro.homework.repository.UserRepository;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TestService {
    private final AdRepository adRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public UserEntity createTestUser() {
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail("testEmail@gmail.com");
        userEntity.setPassword("$2a$12$nbPi.P3rcqDsL4xDcVru2OXnqZX81vVvUvaFRONeYDnyaeK4c/TbS");
        userEntity.setFirstName("testFirstName");
        userEntity.setLastName("testLastName");
        userEntity.setPhoneUser("+77777777777");
        userEntity.setImagePath("/users/image/" + userEntity.getId()); // not sure
        userEntity.setRole(Role.USER);
        userRepository.save(userEntity);
        return userEntity;
    }

    public AdEntity createTestAd() {
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail("testEmail@gmail.com");
        userEntity.setPassword("$2a$12$nbPi.P3rcqDsL4xDcVru2OXnqZX81vVvUvaFRONeYDnyaeK4c/TbS");
        userEntity.setFirstName("testFirstName");
        userEntity.setLastName("testLastName");
        userEntity.setPhoneUser("+77777777777");
        userEntity.setImagePath("/users/image/" + userEntity.getId()); // not sure
        userEntity.setRole(Role.USER);
        userRepository.save(userEntity);

        AdEntity adEntity = new AdEntity();
        adEntity.setDescription("testDescription");
        adEntity.setPrice(55555);
        adEntity.setTitle("testTitle");
        adEntity.setImagePath("/ads/image/" + adEntity.getId()); // not sure
        adEntity.setUserEntity(userEntity);
        adRepository.save(adEntity);
        return adEntity;
    }

    public CommentEntity createTestComment() {
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail("testEmail@gmail.com");
        userEntity.setPassword("$2a$12$nbPi.P3rcqDsL4xDcVru2OXnqZX81vVvUvaFRONeYDnyaeK4c/TbS");
        userEntity.setFirstName("testFirstName");
        userEntity.setLastName("testLastName");
        userEntity.setPhoneUser("+77777777777");
        userEntity.setImagePath("/users/image/" + userEntity.getId()); // not sure
        userEntity.setRole(Role.USER);
        userRepository.save(userEntity);

        AdEntity adEntity = new AdEntity();
        adEntity.setDescription("testDescription");
        adEntity.setPrice(55555);
        adEntity.setTitle("testTitle");
        adEntity.setImagePath("/ads/image/" + adEntity.getId()); // not sure
        adEntity.setUserEntity(userEntity);
        adRepository.save(adEntity);

        CommentEntity commentEntity = new CommentEntity();
        commentEntity.setText("testText");
        commentEntity.setCreatedAt(Instant.now().toEpochMilli()); // or we can use System.currentTimeMillis()
        commentEntity.setUserEntity(userEntity);
        commentEntity.setAdEntity(adEntity);
        commentRepository.save(commentEntity);
        return commentEntity;
    }
}
