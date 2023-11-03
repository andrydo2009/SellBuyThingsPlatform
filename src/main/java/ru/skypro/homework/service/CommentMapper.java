package ru.skypro.homework.service;

import ru.skypro.homework.dto.comment.Comment;
import ru.skypro.homework.dto.comment.Comments;
import ru.skypro.homework.dto.comment.CreateOrUpdateComment;
import ru.skypro.homework.model.CommentEntity;

import java.util.List;

public interface CommentMapper {
    CommentEntity toCommentEntity(CreateOrUpdateComment createOrUpdateComment, CommentEntity commentEntity);
    Comment toComment(CommentEntity commentEntity);
    Comments toComments(List<CommentEntity> commentEntityList);
}
