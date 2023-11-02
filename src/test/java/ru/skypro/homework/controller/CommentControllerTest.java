package ru.skypro.homework.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.skypro.homework.dto.comment.CreateOrUpdateComment;
import ru.skypro.homework.model.AdEntity;
import ru.skypro.homework.model.CommentEntity;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.repository.CommentRepository;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.TestService;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

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
    private TestService testService;

    @AfterEach
    public void resetDb() {
        commentRepository.deleteAll();
        adRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Получение комментариев объявления")
    void shouldReturnCommentCollectionWhenGetCommentsCalled() throws Exception {

        CommentEntity commentEntity = testService.createTestComment();

        mockMvc.perform(MockMvcRequestBuilders.get("/ads/{id}/comments", commentEntity.getAdEntity().getId())
                        .header(HttpHeaders.AUTHORIZATION,
                                "Basic " + HttpHeaders.encodeBasicAuth("testEmail@gmail.com",
                                        "testPassword", StandardCharsets.UTF_8))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.count").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.results[0].author").value(commentEntity.getUserEntity().getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.results[0].authorImage").value("/users/image/" + commentEntity.getUserEntity().getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.results[0].authorFirstName").value("testFirstName"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.results[0].createdAt").value(Matchers.lessThan(Instant.now().toEpochMilli())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.results[0].text").value("testText"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Добавление комментария к объявлению")
    void shouldReturnCommentWhenAddCommentCalled() throws Exception {

        CreateOrUpdateComment createOrUpdateComment = new CreateOrUpdateComment();
        createOrUpdateComment.setText("createdTestText");

        AdEntity adEntity = testService.createTestAd();

        mockMvc.perform(MockMvcRequestBuilders.post("/ads/{id}/comments", adEntity.getId())
                        .content(objectMapper.writeValueAsString(createOrUpdateComment))
                        .header(HttpHeaders.AUTHORIZATION,
                                "Basic " + HttpHeaders.encodeBasicAuth("testEmail@gmail.com",
                                        "testPassword", StandardCharsets.UTF_8))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.author").value(adEntity.getUserEntity().getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.authorImage").value("/users/image/" + adEntity.getUserEntity().getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.authorFirstName").value("testFirstName"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").value(Matchers.lessThan(Instant.now().toEpochMilli())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.text").value("createdTestText"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Удаление комментария по его id")
    void shouldReturnOkWhenDeleteCommentCalled() throws Exception {

        CommentEntity commentEntity = testService.createTestComment();

        mockMvc.perform(MockMvcRequestBuilders.delete("/ads/{adId}/comments/{commentId}",
                                commentEntity.getAdEntity().getId(), commentEntity.getId())
                        .header(HttpHeaders.AUTHORIZATION,
                                "Basic " + HttpHeaders.encodeBasicAuth("testEmail@gmail.com",
                                        "testPassword", StandardCharsets.UTF_8)))
                .andExpect(status().isOk());

        Assertions.assertFalse(commentRepository.findById(commentEntity.getId()).isPresent());
    }

    @Test
    @DisplayName("Обновление комментария")
    void shouldReturnUpdatedCommentWhenUpdateCommentCalled() throws Exception {

        CreateOrUpdateComment createOrUpdateComment = new CreateOrUpdateComment();
        createOrUpdateComment.setText("updatedTestText");

        CommentEntity commentEntity = testService.createTestComment();

        mockMvc.perform(MockMvcRequestBuilders.patch("/ads/{adId}/comments/{commentId}",
                commentEntity.getAdEntity().getId(), commentEntity.getId())
                        .header(HttpHeaders.AUTHORIZATION,
                                "Basic " + HttpHeaders.encodeBasicAuth("testEmail@gmail.com",
                                        "testPassword", StandardCharsets.UTF_8))
                .content(objectMapper.writeValueAsString(createOrUpdateComment))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.author").value(commentEntity.getUserEntity().getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.authorImage").value("/users/image/" + commentEntity.getUserEntity().getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.authorFirstName").value("testFirstName"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.createdAt").value(Matchers.lessThan(Instant.now().toEpochMilli())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.text").value("updatedTestText"))
                .andExpect(status().isOk());
    }
}