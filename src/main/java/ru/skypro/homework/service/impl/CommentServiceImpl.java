package ru.skypro.homework.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skypro.homework.dto.account.Role;
import ru.skypro.homework.dto.comment.Comment;
import ru.skypro.homework.dto.comment.Comments;
import ru.skypro.homework.dto.comment.CreateOrUpdateComment;
import ru.skypro.homework.model.AdEntity;
import ru.skypro.homework.model.CommentEntity;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.repository.CommentRepository;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.CommentMapper;
import ru.skypro.homework.service.CommentService;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final AdRepository adRepository;
    private final UserRepository userRepository;
    private final UserDetails userDetails;

    private static final String USER_NOT_FOUND = "User not found";

    public CommentServiceImpl(CommentRepository commentRepository,
                              CommentMapper commentMapper,
                              AdRepository adRepository,
                              UserRepository userRepository,
                              UserDetails userDetails) {
        this.commentRepository = commentRepository;
        this.commentMapper = commentMapper;
        this.adRepository = adRepository;
        this.userRepository = userRepository;
        this.userDetails = userDetails;
    }

    /**
     * Получение всех комментариев из БД по id объявления.<br>
     * - Поиск в БД всех комментариев по id объявления {@link CommentRepository#findCommentEntitiesByAdEntity_Id(Integer)}.<br>
     * - Маппинг списка найденных комментариев в объект класса {@link CommentMapper#toComments(List)}.
     *
     * @param id идентификатор объявления в БД
     * @return объект {@link Comments}, содержащий список комментариев к данному объявлению
     */
    @Override
    @Transactional(readOnly = true)
    public Comments getComments(Integer id) {
        List<CommentEntity> commentEntityList = commentRepository
                .findCommentEntitiesByAdEntity_Id(id);
        log.info("List of comments was received successfully. " + LocalDate.now());
        return commentMapper.toComments(commentEntityList);
    }

    /**
     * Создание в БД комментария к выбранному объявлению.<br>
     * - Поиск объявления в БД по id {@link AdRepository#findById(Object)}.<br>
     * - Создание комментария  {@link CommentMapper#toCommentEntity(CreateOrUpdateComment, CommentEntity)}.<br>
     * - Поиск пользователя в БД по данным аутентификации {@link UserDetails#getUsername()}, {@link UserRepository#findByEmail(String)}.<br>
     * - Инициализация  объявлений и пользователей созданному комментарию {@link CommentEntity#setAdEntity(AdEntity)}, {@link CommentEntity#setUserEntity(UserEntity)}.<br>
     * - Сохранение созданного комментария в БД {@link CommentRepository#save(Object)}.<br>
     * - Маппинг созданного комментария в объект  класса {@link CommentMapper#toCommentEntity(CreateOrUpdateComment, CommentEntity)}.
     *
     * @param id                    идентификатор объявления в БД
     * @param createOrUpdateComment объект, содержащий текст комментария
     * @return объект {@link Comment}, содержащий необходимую для пользователя информацию о созданном комментарии
     */
    @Override
    @Transactional
    public Comment addComment(Integer id, CreateOrUpdateComment createOrUpdateComment) {
        UserEntity userEntity = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND));
        AdEntity adEntity = adRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Advertisement not found"));
        CommentEntity commentEntity = commentMapper.toCommentEntity(createOrUpdateComment, new CommentEntity());
        commentEntity.setUserEntity(userEntity);
        commentEntity.setAdEntity(adEntity);
        log.info("A comment was created in database. " + LocalDate.now());
        return commentMapper.toComment(commentRepository.save(commentEntity));
    }

    /**
     * Удаления из БД комментария выбранного объявления.<br>
     * - Поиск пользователя в БД по данным аутентификации {@link UserDetails#getUsername()}, {@link UserRepository#findByEmail(String)}.<br>
     * - Поиск комментария в БД по идентификатору комментария и идентификатору объявления {@link CommentRepository#findByIdAndAdEntity_Id(int, int)}.<br>
     * - Удаление комментария из БД {@link CommentRepository#delete(Object)}.
     *
     * @param adId      идентификатор объявления в БД
     * @param commentId идентификатор комментария в БД
     * @return <B>true</B>, если пользователь авторизован на удаление комментария и комментарий удален.<br>
     * В противном случае <B>false</B>
     */
    @Override
    @Transactional
    public boolean deleteComment(Integer adId, Integer commentId) {
        String userName = userDetails.getUsername();
        UserEntity userEntity = userRepository.findByEmail(userName)
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND));
        CommentEntity commentEntity = commentRepository.findByIdAndAdEntity_Id(commentId, adId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        if (userEntity.getRole() == Role.ADMIN || commentEntity.getUserEntity().equals(userEntity)) {
            commentRepository.delete(commentEntity);
            log.info("Comment was removed from database. " + LocalDate.now());
            return true;
        }
        log.info("It is not possible to delete comment from database. " +
                "Maybe there is no comment with this identification number in database." + LocalDate.now());
        return false;
    }

    /**
     * Обновление комментария выбранного объявления.<br>
     * - Поиск пользователя в БД по данным аутентификации {@link UserDetails#getUsername()}, {@link UserRepository#findByEmail(String)}.<br>
     * - Поиск комментария в БД по идентификатору комментария и идентификатору объявления {@link CommentRepository#findByIdAndAdEntity_Id(int, int)}.<br>
     * - Маппинг найденного комментария и входных данных в обновленный комментарий {@link CommentMapper#toCommentEntity(CreateOrUpdateComment, CommentEntity)}.<br>
     * - Сохранение обновленного комментария в БД {@link CommentRepository#save(Object)}.<br>
     * - Маппинг обновленного комментария в объект возвращаемого класса {@link CommentMapper#toCommentEntity(CreateOrUpdateComment, CommentEntity)}.
     *
     * @param adId                  идентификатор объявления в БД
     * @param commentId             идентификатор комментария в БД
     * @param createOrUpdateComment объект, содержащий текст комментария
     * @return объект {@link Comment}, содержащий необходимую для пользователя информацию об обновленном комментарии, если пользователь авторизован на редактирование комментария и комментарий обновлен.<br>
     * В противном случае <B>null</B>
     */
    @Override
    @Transactional
    public Comment updateComment(Integer adId, Integer commentId, CreateOrUpdateComment createOrUpdateComment) {
        String userName = userDetails.getUsername();
        UserEntity userEntity = userRepository.findByEmail(userName)
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND));
        CommentEntity commentEntity = commentRepository.findByIdAndAdEntity_Id(commentId, adId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        if (userEntity.getRole() == Role.ADMIN || commentEntity.getUserEntity().equals(userEntity)) {
            log.info("Comment successfully updated." + LocalDate.now());
            return commentMapper.toComment(commentRepository.save(
                    commentMapper.toCommentEntity(createOrUpdateComment, commentEntity)));
        }
        log.info("It is not possible to update a comment in database. " + LocalDate.now());
        return null;
    }
}
