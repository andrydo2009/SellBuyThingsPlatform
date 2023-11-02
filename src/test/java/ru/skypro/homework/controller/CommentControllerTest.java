package ru.skypro.homework.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.skypro.homework.dto.account.Role;
import ru.skypro.homework.dto.comment.CreateOrUpdateComment;
import ru.skypro.homework.model.AdEntity;
import ru.skypro.homework.model.CommentEntity;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.repository.CommentRepository;
import ru.skypro.homework.repository.UserRepository;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CommentControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdRepository adRepository;

    @Autowired
    private WebApplicationContext context;

    @AfterEach
    public void resetDb() {
        commentRepository.deleteAll();
        adRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Получить комментарии объявления")
    @WithMockUser(value = "test@mail.com")
    void getCommentsTest() throws Exception {
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail("testEmail@skypromail.com");
        userEntity.setPassword("$2a$12$GSSQABQTjedVF3WrvN53POmGvPZzwfwXQQdPfzunvZDwW944SxVMq");
        userEntity.setFirstName("firstName");
        userEntity.setLastName("lastName");
        userEntity.setPhoneUser("+79995265265");
        userEntity.setRole(Role.USER);
        userEntity.setImagePath("/users/image/" + userEntity.getId());
        userRepository.save(userEntity);
        AdEntity adEntity = new AdEntity();
        adEntity.setDescription("Test text description");
        adEntity.setPrice(100000);
        adEntity.setTitle("Test text tittle");
        adEntity.setImagePath("/ads/image/" + adEntity.getId());
        adEntity.setUserEntity(userEntity);
        adRepository.save(adEntity);
        CommentEntity commentEntity = new CommentEntity();
        commentEntity.setText("Test comment text");
        commentEntity.setCreatedAt(System.currentTimeMillis());
        commentEntity.setUserEntity(userEntity);
        commentEntity.setAdEntity(adEntity);
        commentRepository.save(commentEntity);

        mockMvc.perform(MockMvcRequestBuilders.get("/ads/{id}/comments", commentEntity.getAdEntity().getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.count").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.results[0].author")
                        .value(commentEntity.getUserEntity().getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.results[0].authorFirstName")
                        .value(commentEntity.getUserEntity().getFirstName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.results[0].createdAt")
                        .value(commentEntity.getCreatedAt()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.results[0].text")
                        .value(commentEntity.getText()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Добавление комментария к объявлению")
    void addCommentTest() throws Exception {
        CreateOrUpdateComment orUpdateComment=new CreateOrUpdateComment();
        orUpdateComment.setText("Update comment text. Is ok.");
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail("testEmail@skypromail.com");
        userEntity.setPassword("$2a$12$GSSQABQTjedVF3WrvN53POmGvPZzwfwXQQdPfzunvZDwW944SxVMq");
        userEntity.setFirstName("firstName");
        userEntity.setLastName("lastName");
        userEntity.setPhoneUser("+79995265265");
        userEntity.setRole(Role.USER);
        userEntity.setImagePath("/users/image/" + userEntity.getId());
        AdEntity adEntity = new AdEntity();
        adEntity.setDescription("Test text description");
        adEntity.setPrice(100000);
        adEntity.setTitle("Test text tittle");
        adEntity.setImagePath("/ads/image/" + adEntity.getId());
        adEntity.setUserEntity(userEntity);
        userRepository.save(userEntity);
        adRepository.save(adEntity);

        mockMvc.perform(MockMvcRequestBuilders.post("/ads/{id}/comments",adEntity.getId())
                        .header(HttpHeaders.AUTHORIZATION,
                                "Basic " + HttpHeaders.encodeBasicAuth(
                                        "testEmail@skypromail.com", "passwordtestuser", StandardCharsets.UTF_8))
                        .content(objectMapper.writeValueAsString(orUpdateComment))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.author").value(adEntity.getUserEntity().getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.authorImage").value("/users/image/"
                        + adEntity.getUserEntity().getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.authorFirstName").value("firstName"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt",
                        Matchers.lessThan(System.currentTimeMillis())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.text").value("Update comment text. Is ok."))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Удаление комментария по его id")
    void deleteCommentTest() throws Exception {
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail("testEmail@skypromail.com");
        userEntity.setPassword("$2a$12$GSSQABQTjedVF3WrvN53POmGvPZzwfwXQQdPfzunvZDwW944SxVMq");
        userEntity.setFirstName("firstName");
        userEntity.setLastName("lastName");
        userEntity.setPhoneUser("+79995265265");
        userEntity.setRole(Role.USER);
        userEntity.setImagePath("/users/image/" + userEntity.getId());
        userRepository.save(userEntity);
        AdEntity adEntity = new AdEntity();
        adEntity.setDescription("Test text description");
        adEntity.setPrice(100000);
        adEntity.setTitle("Test text tittle");
        adEntity.setImagePath("/ads/image/" + adEntity.getId());
        adEntity.setUserEntity(userEntity);
        adRepository.save(adEntity);
        CommentEntity commentEntity = new CommentEntity();
        commentEntity.setText("Test comment text");
        commentEntity.setCreatedAt(System.currentTimeMillis());
        commentEntity.setUserEntity(userEntity);
        commentEntity.setAdEntity(adEntity);
        commentRepository.save(commentEntity);

        mockMvc.perform(MockMvcRequestBuilders.delete("/ads/{adId}/comments/{commentId}",
                        commentEntity.getAdEntity().getId(),
                        commentEntity.getId())
                .header(HttpHeaders.AUTHORIZATION,
                        "Basic " + HttpHeaders.encodeBasicAuth(
                                "testEmail@skypromail.com", "passwordtestuser", StandardCharsets.UTF_8)))
                .andExpect(status().isOk());

        assertFalse(commentRepository.findById(commentEntity.getId()).isPresent());
    }

    @Test
    void updateCommentTest() {
    }
}