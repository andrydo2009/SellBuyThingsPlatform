package ru.skypro.homework.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.skypro.homework.model.CommentEntity;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<CommentEntity, Integer> {
    List<CommentEntity> findCommentEntitiesByAdEntity_Id(Integer id);
    Optional<CommentEntity> findByIdAndAdEntity_Id(int commentId, int adId);
    void deleteAllByAdEntity_Id(int adId);
}
