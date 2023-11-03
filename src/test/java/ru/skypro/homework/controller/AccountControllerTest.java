package ru.skypro.homework.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.skypro.homework.dto.account.NewPassword;
import ru.skypro.homework.dto.account.UpdateUser;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.TestService;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TestService testService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    public void clearDB() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Обновление пароля")
    void shouldReturnOkWhenUpdatePasswordCalled() throws Exception {

        UserEntity userEntity = testService.createTestUser();

        NewPassword newPassword = new NewPassword();
        newPassword.setCurrentPassword("testPassword");
        newPassword.setNewPassword("newTestPassword");

        mockMvc.perform(MockMvcRequestBuilders.post("/users/set_password")
                .content(objectMapper.writeValueAsString(newPassword))
                        .header(HttpHeaders.AUTHORIZATION,
                                "Basic " + HttpHeaders.encodeBasicAuth("testEmail@gmail.com",
                                        "testPassword", StandardCharsets.UTF_8))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        Assertions.assertTrue(userRepository.findByEmail(userEntity.getEmail()).isPresent());
        Assertions.assertTrue(passwordEncoder.matches("newTestPassword",
                userRepository.findByEmail(userEntity.getEmail()).get().getPassword()));
    }

    @Test
    @DisplayName(value = "Получение информации об авторизованном пользователе")
    void shouldReturnInfoAboutUserWhenCalled() throws Exception {

        UserEntity userEntity = testService.createTestUser();

        mockMvc.perform(MockMvcRequestBuilders.get("/users/me")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Basic " + HttpHeaders.encodeBasicAuth("testEmail@gmail.com",
                                        "testPassword", StandardCharsets.UTF_8)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("testEmail@gmail.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("testFirstName"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("testLastName"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.phone").value("+77777777777"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.image").value("/users/image/" + userEntity.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.role").value(userEntity.getRole().name()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName(value = "Обновление информации об авторизованном пользователе")
    void shouldReturnUpdatedInfoAboutUserWhenCalled() throws Exception {

        testService.createTestUser();

        UpdateUser updatedUser = new UpdateUser();
        updatedUser.setFirstName("updatedFirstName");
        updatedUser.setLastName("updatedLastName");
        updatedUser.setPhone("+79555555555");

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/me")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Basic " + HttpHeaders.encodeBasicAuth("testEmail@gmail.com",
                                        "testPassword", StandardCharsets.UTF_8))
                        .content(objectMapper.writeValueAsString(updatedUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("updatedFirstName"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("updatedLastName"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.phone").value("+79555555555"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName(value = "Обновление аватара авторизованного пользователя")
    void shouldReturnOkWhenUpdateUserAvatarCalled() throws Exception {

        UserEntity userEntity = testService.createTestUser();

        MockMultipartFile image = new MockMultipartFile(
                "image", "image", MediaType.MULTIPART_FORM_DATA_VALUE, "image".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PATCH, "/users/me/image")
                .file(image)
                        .header(HttpHeaders.AUTHORIZATION,
                                "Basic " + HttpHeaders.encodeBasicAuth("testEmail@gmail.com",
                                        "testPassword", StandardCharsets.UTF_8)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        Assertions.assertTrue(userRepository.findById(userEntity.getId()).isPresent());
    }

    @Test // under construction
    @DisplayName(value = "Получение аватара пользователя по его id")
    void shouldReturnImageByteArrayWhenCalled() throws Exception {

        UserEntity userEntity = testService.createTestUser();

        mockMvc.perform(MockMvcRequestBuilders.get("/users/image/{userId}", userEntity.getId())
                        .header(HttpHeaders.AUTHORIZATION,
                                "Basic " + HttpHeaders.encodeBasicAuth("testEmail@gmail.com",
                                        "testPassword", StandardCharsets.UTF_8)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$").value("/users/image/" + userEntity.getId())) // not sure
                .andExpect(status().isOk());
    }
}
